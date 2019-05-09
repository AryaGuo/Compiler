package Compiler;

import Compiler.AST.ASTBuilder;
import Compiler.AST.ASTPrinter;
import Compiler.AST.ASTProgram;
import Compiler.BackEnd.IRCorrector;
import Compiler.BackEnd.NaiveAllocator;
import Compiler.BackEnd.SimpleGraphAllocator;
import Compiler.BackEnd.StackFrameBuilder;
import Compiler.IR.IRBuilder;
import Compiler.IR.IRPrinter;
import Compiler.IR.IRProgram;
import Compiler.IR.RegisterSet;
import Compiler.Parser.MxLexer;
import Compiler.Parser.MxParser;
import Compiler.Symbol.ClassScanner;
import Compiler.Symbol.GlobalDeclarator;
import Compiler.Symbol.GlobalSymbolTable;
import Compiler.Symbol.SemanticChecker;
import Compiler.Utility.ErrorRecorder;
import Compiler.Utility.SyntaxErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) throws IOException {
        run();
    }

    private static void run() throws IOException {
        InputStream inputStream = new FileInputStream("program.c");
        CharStream charStream = CharStreams.fromStream(inputStream);
        MxLexer mxLexer = new MxLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(mxLexer);
        MxParser mxParser = new MxParser(tokenStream);
        ErrorRecorder errorRecorder = new ErrorRecorder();
        mxParser.removeErrorListeners();
        mxParser.addErrorListener(new SyntaxErrorListener(errorRecorder));

//        get parse tree, check syntax error
        ParseTree parseTree = mxParser.program();
        if (errorRecorder.errorOccurred()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }

//        build AST
        ASTBuilder astBuilder = new ASTBuilder(errorRecorder);
        parseTree.accept(astBuilder);
        if (errorRecorder.errorOccurred()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }
        ASTProgram program = astBuilder.getProgram();

        if (Config.printAST) {
            ASTPrinter astPrinter = new ASTPrinter();
            program.accept(astPrinter);
            System.out.println(astPrinter.toString());
        }

//       pass 1: scan classes
        GlobalSymbolTable globalSymbolTable = new GlobalSymbolTable();
        ClassScanner classScanner = new ClassScanner(errorRecorder, globalSymbolTable);
        program.accept(classScanner);
        if (errorRecorder.errorOccurred()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }

//       pass 2: global declaration
        GlobalDeclarator globalDeclarator = new GlobalDeclarator(errorRecorder, globalSymbolTable);
        program.accept(globalDeclarator);
        if (errorRecorder.errorOccurred()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }

//       pass 3: semantic check
        SemanticChecker semanticChecker = new SemanticChecker(errorRecorder, globalSymbolTable);
        program.accept(semanticChecker);
        if (errorRecorder.errorOccurred()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }

//        AST to IR
        RegisterSet.init();
        IRBuilder irBuilder = new IRBuilder(globalSymbolTable);
        program.accept(irBuilder);
        IRProgram irProgram = irBuilder.getIrProgram();
        if (irBuilder.failed) {
            exit(0);
        }


        IRCorrector irCorrector = new IRCorrector();
        irCorrector.visit(irProgram);

        if (Config.printIR) {
            IRPrinter irPrinter = new IRPrinter();
            irPrinter.showBlockHint = true;
            irProgram.accept(irPrinter);
//            irPrinter.printTo(new FileOutputStream("IR_corrected.txt"));
            irPrinter.printTo(System.err);
        }

        switch (Config.allocator) {
            case NaiveAllocator: {
                NaiveAllocator naiveAllocator = new NaiveAllocator(irProgram);
                naiveAllocator.run();
                break;
            }

            case SimpleGraphAllocator: {
                SimpleGraphAllocator simpleGraphAllocator = new SimpleGraphAllocator(irProgram);
                simpleGraphAllocator.run();
                break;
            }
        }

        if (Config.printIRAfterAllocator) {
            IRPrinter irPrinter = new IRPrinter();
            irProgram.accept(irPrinter);
//            irPrinter.printTo(new FileOutputStream("IRAfterAllocator.txt"));
            irPrinter.printTo(System.err);
        }

        StackFrameBuilder stackFrameBuilder = new StackFrameBuilder(irProgram);
        stackFrameBuilder.run();

        if (Config.printIRWithFrame) {
            IRPrinter irPrinter = new IRPrinter();
            irProgram.accept(irPrinter);
//            irPrinter.printTo(new FileOutputStream("IRWithFrame.txt"));
            irPrinter.printTo(System.err);
        }

        if (Config.printToAsmFile) {
            IRPrinter irPrinter = new IRPrinter();
            irPrinter.showNasm = true;
            irPrinter.showHeader = true;
            irProgram.accept(irPrinter);
            irPrinter.printTo(new FileOutputStream("program.asm"));
        }
    }

}

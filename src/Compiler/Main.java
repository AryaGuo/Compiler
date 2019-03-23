package Compiler;

import Compiler.AST.ASTBuilder;
import Compiler.AST.ASTPrinter;
import Compiler.AST.ASTProgram;
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
import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.exit;

public class Main {


    public static void main(String[] args) throws IOException {
        run(args[0]);
    }

    private static void run(String input) throws IOException {
//        System.out.println("compiling " + input);
//        InputStream inputStream = new FileInputStream(input);
        InputStream inputStream = new FileInputStream("program.txt");
        CharStream charStream = CharStreams.fromStream(inputStream);
        MxLexer mxLexer = new MxLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(mxLexer);
        MxParser mxParser = new MxParser(tokenStream);
        ErrorRecorder errorRecorder = new ErrorRecorder();

        //get parse tree, check syntax error
        mxParser.removeErrorListeners();
        mxParser.addErrorListener(new SyntaxErrorListener(errorRecorder));

        ParseTree parseTree = mxParser.program();
        if (errorRecorder.errorOccurred()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }

        //build AST
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

        //scan classes
        GlobalSymbolTable globalSymbolTable = new GlobalSymbolTable();
        ClassScanner classScanner = new ClassScanner(errorRecorder, globalSymbolTable);
        program.accept(classScanner);
        if (errorRecorder.errorOccurred()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }

        //global declaration
        GlobalDeclarator globalDeclarator = new GlobalDeclarator(errorRecorder, globalSymbolTable);
        program.accept(globalDeclarator);
        if (errorRecorder.errorOccurred()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }

//        semantic check
        SemanticChecker semanticChecker = new SemanticChecker(errorRecorder, globalSymbolTable);
        program.accept(semanticChecker);
        if (errorRecorder.errorOccurred()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }
    }

}

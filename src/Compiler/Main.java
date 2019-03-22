package Compiler;

import Compiler.AST.ASTBuilder;
import Compiler.AST.ASTPrinter;
import Compiler.AST.ASTProgram;
import Compiler.Parser.MxLexer;
import Compiler.Parser.MxParser;
import Compiler.Symbol.SymbolScanner;
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
        run();
    }

    private static void run() throws IOException {
        InputStream inputStream = new FileInputStream("testdata/3.txt");
        CharStream charStream = CharStreams.fromStream(inputStream);
        MxLexer mxLexer = new MxLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(mxLexer);
        MxParser mxParser = new MxParser(tokenStream);
        ErrorRecorder errorRecorder = new ErrorRecorder();

        //get parse tree, check syntax error
        ParseTree parseTree = mxParser.program();
        mxParser.removeErrorListeners();
        mxParser.addErrorListener(new SyntaxErrorListener(errorRecorder));
        if (errorRecorder.errorOccured()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }

        //build AST
        ASTBuilder astBuilder = new ASTBuilder(errorRecorder);
        parseTree.accept(astBuilder);
        if(errorRecorder.errorOccured()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }
        ASTProgram program = astBuilder.getProgram();

        if (Config.printAST) {
            ASTPrinter astPrinter = new ASTPrinter();
            program.accept(astPrinter);
            System.out.println(astPrinter.toString());
        }

        //build symbol table
        SymbolScanner symbolScanner = new SymbolScanner(errorRecorder);
        program.accept(symbolScanner);
        if(errorRecorder.errorOccured()) {
            errorRecorder.printTo(System.err);
            exit(1);
        }


    }

}

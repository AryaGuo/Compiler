package Compiler.Symbol;

public class ClassSymbol extends Symbol {

    SymbolTable symbolTable;

    public ClassSymbol(String name) {
        this.name = name;
    }
}

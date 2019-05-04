package Compiler.Symbol;

import Compiler.Config;

public class ClassSymbol extends Symbol implements Type {

    public SymbolTable symbolTable;

    public ClassSymbol(String name) {
        this.name = name;
    }

    @Override
    public boolean match(Type type) {
        if (type instanceof ClassSymbol) {
            if (((ClassSymbol) type).name.equals("null")) return true;
            return this.name.equals(((ClassSymbol) type).name);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int getBytes() {
        return Config.REGISTER_WIDTH * symbolTable.variableTable.size();
    }

}

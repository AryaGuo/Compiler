package Compiler.Symbol;

public class ClassType extends Type {
    public String name;
    public ClassSymbol symbol;

    public ClassType(String name, ClassSymbol symbol) {
        this.name = name;
        this.symbol = symbol;
    }
}

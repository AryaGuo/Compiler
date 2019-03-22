package Compiler.Symbol;

public class PrimitiveType extends Type {
    public String name;
    public PrimitiveSymbol symbol;

    public PrimitiveType(String name, PrimitiveSymbol symbol) {
        this.name = name;
        this.symbol = symbol;
    }
}

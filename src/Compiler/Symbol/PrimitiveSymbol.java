package Compiler.Symbol;

public class PrimitiveSymbol extends Symbol implements Type {

    public PrimitiveSymbol(String name) {
        this.name = name;
    }

    @Override
    public boolean match(Type type) {
        if (type instanceof PrimitiveSymbol) {
            return this.name.equals(((PrimitiveSymbol) type).name);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
}

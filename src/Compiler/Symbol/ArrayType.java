package Compiler.Symbol;

public class ArrayType implements Type {
    public Type baseType;

    public ArrayType(Type baseType) {
        this.baseType = baseType;
    }

    @Override
    public boolean match(Type type) {
        if (type instanceof ClassSymbol && ((ClassSymbol) type).name.equals("null"))
            return true;
        if (!(type instanceof ArrayType)) {
            return false;
        } else {
            return baseType.match(((ArrayType) type).baseType);
        }
    }

    @Override
    public String toString() {
        return this.baseType.toString() + "[]";
    }
}

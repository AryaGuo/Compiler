package Compiler.Symbol;

public class VariableSymbol extends Symbol {
    public Type type;
    public boolean isGlobal;

    public VariableSymbol(String name) {
        this.name = name;
    }
}

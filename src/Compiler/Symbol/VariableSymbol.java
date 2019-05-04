package Compiler.Symbol;

import Compiler.IR.Operand.VirtualRegister;

public class VariableSymbol extends Symbol {
    public Type type;
    public boolean isGlobal;
    public boolean isClassField;
    public VirtualRegister virtualRegister;

    public VariableSymbol(String name) {
        this.name = name;
    }
}

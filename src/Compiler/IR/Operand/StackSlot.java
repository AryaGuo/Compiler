package Compiler.IR.Operand;

import Compiler.IR.IRVisitor;

public class StackSlot extends Memory {
    public String hint;

    private static int counter = 0;
    public int id;

    public StackSlot(String hint) {
        this.hint = hint;
        id = counter++;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

package Compiler.IR.Operand;

import Compiler.IR.IRVisitor;

public class Immediate extends Constant {
    public int value;

    public Immediate(int value) {
        this.value = value;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

package Compiler.IR.Operand;

import Compiler.IR.IRVisitor;

public class PhysicalRegister extends Register {
    public String name;

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

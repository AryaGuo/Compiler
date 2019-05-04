package Compiler.IR.Operand;

import Compiler.IR.IRVisitor;

public class VirtualRegister extends Register {
    public String hint;
    public PhysicalRegister allocatedPhysicalRegister;
    public Memory spillPlace;

    private static int counter = 0;
    public int id;

    public VirtualRegister(String hint) {
        this.hint = hint;
        allocatedPhysicalRegister = null;
        this.id = counter++;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

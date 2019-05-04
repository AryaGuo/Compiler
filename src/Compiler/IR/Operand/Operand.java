package Compiler.IR.Operand;

import Compiler.IR.IRVisitor;

public abstract class Operand {
    public abstract void accept(IRVisitor irVisitor);
}

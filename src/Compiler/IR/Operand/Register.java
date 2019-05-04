package Compiler.IR.Operand;

import Compiler.IR.IRVisitor;

public abstract class Register extends Address {
    public abstract void accept(IRVisitor visitor);
}

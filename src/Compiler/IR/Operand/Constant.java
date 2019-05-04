package Compiler.IR.Operand;

import Compiler.IR.IRVisitor;

public abstract class Constant extends Operand {

    public abstract void accept(IRVisitor visitor);
}

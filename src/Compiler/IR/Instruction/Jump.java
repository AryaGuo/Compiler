package Compiler.IR.Instruction;

import Compiler.IR.BasicBlock;
import Compiler.IR.IRVisitor;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.StackSlot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Jump extends IRInstruction {
    public BasicBlock targetBB;

    public Jump(BasicBlock bb, BasicBlock targetBB) {
        super(bb);
        this.targetBB = targetBB;
    }

    @Override
    public List<Register> usedRegs() {
        return new LinkedList<>();
    }

    @Override
    public List<Register> storeRegs() {
        return new LinkedList<>();
    }

    @Override
    public void renameRegs(Map<Register, Register> map) {
    }

    @Override
    public List<StackSlot> getStackSlot() {
        return new LinkedList<>();
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

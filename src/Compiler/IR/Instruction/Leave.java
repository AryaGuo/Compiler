package Compiler.IR.Instruction;

import Compiler.IR.BasicBlock;
import Compiler.IR.IRVisitor;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.StackSlot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Leave extends IRInstruction {
    public Leave(BasicBlock bb) {
        super(bb);
    }

    @Override
    public List<Register> useRegs() {
        return new LinkedList<>();
    }

    @Override
    public List<Register> defRegs() {
        return new LinkedList<>();
    }

    @Override
    public void renameRegs(Map<Register, Register> map) {
    }

    @Override
    public List<StackSlot> getStackSlot() {
        return new LinkedList<>();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

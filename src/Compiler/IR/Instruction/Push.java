package Compiler.IR.Instruction;

import Compiler.IR.BasicBlock;
import Compiler.IR.IRVisitor;
import Compiler.IR.Operand.Memory;
import Compiler.IR.Operand.Operand;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.StackSlot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Push extends IRInstruction {
    public Operand src;

    public Push(BasicBlock bb, Operand src) {
        super(bb);
        this.src = src;
    }

    @Override
    public List<Register> useRegs() {
        List<Register> regs = new LinkedList<>();
        if (src instanceof Memory) {
            regs.addAll(((Memory) src).usedRegs());
        }
        return regs;
    }

    @Override
    public List<Register> defRegs() {
        LinkedList<Register> regs = new LinkedList<>();
        if (src instanceof Register)
            regs.add((Register) src);
        return regs;
    }
    @Override
    public void renameRegs(Map<Register, Register> map) {
        if (src instanceof Register && map.containsKey(src)) {
            src = map.get(src);
        } else if (src instanceof Memory) {
            src = ((Memory) src).copy();
            ((Memory) src).renameRegs(map);
        }
    }

    @Override
    public List<StackSlot> getStackSlot() {
        return defaultGetStackSlots(src);
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

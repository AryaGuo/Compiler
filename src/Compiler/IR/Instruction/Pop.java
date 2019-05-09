package Compiler.IR.Instruction;

import Compiler.IR.BasicBlock;
import Compiler.IR.IRVisitor;
import Compiler.IR.Operand.Address;
import Compiler.IR.Operand.Memory;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.StackSlot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Pop extends IRInstruction {
    public Address dest;

    public Pop(BasicBlock bb, Address dest) {
        super(bb);
        this.dest = dest;
    }

    @Override
    public List<Register> useRegs() {
        List<Register> regs = new LinkedList<>();
        if (dest instanceof Memory) {
            regs.addAll(((Memory) dest).usedRegs());
        }
        return regs;
    }

    @Override
    public List<Register> defRegs() {
        LinkedList<Register> regs = new LinkedList<>();
        if (dest instanceof Register)
            regs.add((Register) dest);
        return regs;
    }

    @Override
    public void renameRegs(Map<Register, Register> map) {
        if (dest instanceof Register && map.containsKey(dest)) {
            dest = map.get(dest);
        } else if (dest instanceof Memory) {
            dest = ((Memory) dest).copy();
            ((Memory) dest).renameRegs(map);
        }
    }

    @Override
    public List<StackSlot> getStackSlot() {
        return defaultGetStackSlots(dest);
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

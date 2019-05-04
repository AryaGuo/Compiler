package Compiler.IR.Instruction;

import Compiler.IR.BasicBlock;
import Compiler.IR.IRVisitor;
import Compiler.IR.Operand.Memory;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.StackSlot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Lea extends IRInstruction {
    public Register dest;
    public Memory src;

    public Lea(BasicBlock bb, Register dest, Memory src) {
        super(bb);
        this.dest = dest;
        this.src = src;
    }

    @Override
    public List<Register> usedRegs() {
        List<Register> regs = new LinkedList<>();
        regs.add(dest);
        regs.addAll(src.usedRegs());
        return regs;
    }

    @Override
    public List<Register> storeRegs() {
        List<Register> regs = new LinkedList<>();
        regs.add(dest);
        return regs;
    }

    @Override
    public void renameRegs(Map<Register, Register> map) {
        if (map.containsKey(dest)) {
            dest = map.get(dest);
        }
        src = src.copy();
        src.renameRegs(map);
    }

    @Override
    public List<StackSlot> getStackSlot() {
        return defaultGetStackSlots(dest, src);
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

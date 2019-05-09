package Compiler.IR.Instruction;

import Compiler.IR.BasicBlock;
import Compiler.IR.IRVisitor;
import Compiler.IR.Operand.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Move extends IRInstruction {
    public Address dest;
    public Operand src;

    public Move(BasicBlock bb, Address dest, Operand src) {
        super(bb);
        this.dest = dest;
        this.src = src;
    }

    @Override
    public List<Register> useRegs() {
        List<Register> regs = new LinkedList<>();
        if (dest instanceof Memory) {
            regs.addAll(((Memory) dest).usedRegs());
        }
        if (src instanceof Register) {
            regs.add((Register) src);
        } else if (src instanceof Memory) {
            regs.addAll(((Memory) src).usedRegs());
        }
        return regs;
    }

    @Override
    public List<Register> defRegs() {
        List<Register> regs = new LinkedList<>();
        if (dest instanceof Register) {
            regs.add((Register) dest);
        }
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
        if (src instanceof Register && map.containsKey(src)) {
            src = map.get(src);
        } else if (src instanceof Memory) {
            src = ((Memory) src).copy();
            ((Memory) src).renameRegs(map);
        }

    }

    @Override
    public List<StackSlot> getStackSlot() {
        return defaultGetStackSlots(dest, src);
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

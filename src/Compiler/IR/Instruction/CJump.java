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

public class CJump extends IRInstruction {
    public enum Op {
        E, NE, G, GE, L, LE;

        @Override
        public String toString() {
            switch (this) {
                case E:
                    return "e";
                case NE:
                    return "ne";
                case G:
                    return "g";
                case GE:
                    return "ge";
                case L:
                    return "l";
                case LE:
                    return "le";
                default:
                    return null;
            }
        }
    }

    public Op op;
    public BasicBlock thenBB;
    public BasicBlock elseBB;
    public Operand src1;
    public Operand src2;

    public CJump(BasicBlock bb, Op op, BasicBlock thenBB, BasicBlock elseBB, Operand src1, Operand src2) {
        super(bb);
        this.op = op;
        this.thenBB = thenBB;
        this.elseBB = elseBB;
        this.src1 = src1;
        this.src2 = src2;
    }

    @Override
    public List<Register> usedRegs() {
        List<Register> regs = new LinkedList<>();
        if (src1 instanceof Register) {
            regs.add((Register) src1);
        } else if (src1 instanceof Memory) {
            regs.addAll(((Memory) src1).usedRegs());
        }
        if (src2 instanceof Register) {
            regs.add((Register) src2);
        } else if (src2 instanceof Memory) {
            regs.addAll(((Memory) src2).usedRegs());
        }
        return regs;
    }

    @Override
    public List<Register> storeRegs() {
        return new LinkedList<>();
    }

    @Override
    public void renameRegs(Map<Register, Register> map) {
        if (src1 instanceof Register && map.containsKey(src1)) {
            src1 = map.get(src1);
        } else if (src1 instanceof Memory) {
            ((Memory) src1).copy();
            ((Memory) src1).renameRegs(map);
        }
        if (src2 instanceof Register && map.containsKey(src2)) {
            src2 = map.get(src2);
        } else if (src2 instanceof Memory) {
            ((Memory) src2).copy();
            ((Memory) src2).renameRegs(map);
        }
    }

    @Override
    public List<StackSlot> getStackSlot() {
        return defaultGetStackSlots(src1, src2);
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

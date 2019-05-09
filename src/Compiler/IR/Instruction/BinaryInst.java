package Compiler.IR.Instruction;

import Compiler.IR.BasicBlock;
import Compiler.IR.IRVisitor;
import Compiler.IR.Operand.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static Compiler.IR.Instruction.BinaryInst.Op.*;
import static Compiler.IR.RegisterSet.vrax;
import static Compiler.IR.RegisterSet.vrdx;

public class BinaryInst extends IRInstruction {
    public enum Op {
        ADD, SUB, MUL, DIV, MOD, SAL, SAR, AND, OR, XOR;

        @Override
        public String toString() {
            switch (this) {
                case ADD:
                    return "add";
                case SUB:
                    return "sub";
                case MUL:
                    return "mul";
                case DIV:
                    return "div";
                case MOD:
                    return "mod";
                case SAL:
                    return "shl";
                case SAR:
                    return "shr";
                case AND:
                    return "and";
                case OR:
                    return "or";
                case XOR:
                    return "xor";
                default:
                    return null;
            }
        }
    }

    public Op op;
    public Address dest;
    public Operand src;

    public BinaryInst(BasicBlock bb, Op op, Address dest, Operand src) {
        super(bb);
        this.op = op;
        this.dest = dest;
        this.src = src;
    }

    @Override
    public List<Register> useRegs() {
        List<Register> regs = new LinkedList<>();
        if (dest instanceof Register) {
            regs.add((Register) dest);
        } else if (dest instanceof Memory) {
            regs.addAll(((Memory) dest).usedRegs());
        }
        if (src instanceof Register) {
            regs.add((Register) src);
        } else if (src instanceof Memory) {
            regs.addAll(((Memory) src).usedRegs());
        }
        if (op == MUL) {
            if (!regs.contains(vrax))
                regs.add(vrax);
        } else if (op == DIV || op == MOD) {
            if (!regs.contains(vrax))
                regs.add(vrax);
            if (!regs.contains(vrdx))
                regs.add(vrdx);
        }
        return regs;
    }

    @Override
    public List<Register> defRegs() {
        List<Register> regs = new LinkedList<>();
        if (dest instanceof Register) {
            regs.add((Register) dest);
        }
        if (op == MUL || op == DIV || op == MOD) {
            if (!regs.contains(vrax))
                regs.add(vrax);
            if (!regs.contains(vrdx))
                regs.add(vrdx);
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

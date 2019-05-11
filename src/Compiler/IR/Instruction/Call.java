package Compiler.IR.Instruction;

import Compiler.IR.BasicBlock;
import Compiler.IR.Function;
import Compiler.IR.IRVisitor;
import Compiler.IR.Operand.*;
import Compiler.IR.RegisterSet;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Call extends IRInstruction {
    public Address dest;
    public Function function;
    public LinkedList<Operand> args;

    public Call(BasicBlock bb, Address dest, Function function, LinkedList<Operand> args) {
        super(bb);
        this.dest = dest;
        this.function = function;
        this.args = args;
        bb.function.callee.add(function);
    }

    public Call(BasicBlock bb, Address dest, Function function, Operand... args) {
        super(bb);
        this.dest = dest;
        this.function = function;
        this.args = new LinkedList<>(Arrays.asList(args));
        bb.function.callee.add(function);
    }

    public List<Register> callArgs() {
        List<Register> regs = new LinkedList<>();
        for (Operand operand : args) {
            if (operand instanceof Memory) {
                regs.addAll(((Memory) operand).usedRegs());
            } else if (operand instanceof VirtualRegister) {
                regs.add((Register) operand);
            }
        }
        return regs;
    }

    public List<Register> callUseRegs() {
        return new LinkedList<>(RegisterSet.args.subList(0, Integer.min(6, args.size())));
    }

    public List<Register> callDefRegs() {
        return new LinkedList<>(RegisterSet.callerSave);
    }

    @Override
    public List<Register> useRegs() {
        return new LinkedList<>(RegisterSet.vargs.subList(0, Integer.min(6, args.size())));
    }

    @Override
    public List<Register> defRegs() {
        return new LinkedList<>(RegisterSet.vcallerSave);
    }

    @Override
    public void renameRegs(Map<Register, Register> map) {
        if (dest instanceof Register && map.containsKey(dest)) {
            dest = map.get(dest);
        }
        for (int i = 0; i < args.size(); ++i) {
            Operand operand = args.get(i);
            if (operand instanceof Register && map.containsKey(operand)) {
                args.set(i, map.get(operand));
            } else if (operand instanceof Memory) {
                Memory memory = ((Memory) operand).copy();
                memory.renameRegs(map);
                args.set(i, memory);
            }
        }
    }

    @Override
    public List<StackSlot> getStackSlot() {
        List<StackSlot> slots = defaultGetStackSlots(dest);
        for (Operand operand : args) {
            if (operand instanceof StackSlot)
                slots.add((StackSlot) operand);
        }
        return slots;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
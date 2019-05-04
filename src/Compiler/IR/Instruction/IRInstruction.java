package Compiler.IR.Instruction;

import Compiler.IR.BasicBlock;
import Compiler.IR.IRVisitor;
import Compiler.IR.Operand.Operand;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.StackSlot;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class IRInstruction {
    public BasicBlock bb;
    public IRInstruction pre, nxt;

    public IRInstruction(BasicBlock bb) {
        this.bb = bb;
    }

    public void append(IRInstruction instruction) {
        if (nxt == null) {
            nxt = instruction;
            instruction.pre = this;
            bb.tail = instruction;
        } else {
            instruction.pre = this;
            instruction.nxt = nxt;
            nxt.pre = instruction;
            nxt = instruction;
        }
    }

    public void prepend(IRInstruction instruction) {
        if (pre == null) {
            pre = instruction;
            instruction.nxt = this;
            bb.head = instruction;
        } else {
            instruction.nxt = this;
            instruction.pre = pre;
            pre.nxt = instruction;
            pre = instruction;
        }
    }

    public void remove() {
        if (pre == null && nxt == null) {
            bb.head = bb.tail = null;
        } else if (pre == null) {
            bb.head = nxt;
            nxt.pre = null;
        } else if (nxt == null) {
            bb.tail = pre;
            pre.nxt = null;
        } else {
            pre.nxt = nxt;
            nxt.pre = pre;
        }
    }

    public abstract List<Register> usedRegs();

    public abstract List<Register> storeRegs();

    public abstract void renameRegs(Map<Register, Register> map);

    public abstract void accept(IRVisitor visitor);

    public abstract List<StackSlot> getStackSlot();

    LinkedList<StackSlot> defaultGetStackSlots(Operand... operands) {
        LinkedList<StackSlot> slots = new LinkedList<>();
        for (Operand operand : operands)
            if (operand instanceof StackSlot)
                slots.add((StackSlot) operand);
        return slots;
    }

}

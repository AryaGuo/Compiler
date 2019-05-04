package Compiler.BackEnd;

import Compiler.Config;
import Compiler.IR.BasicBlock;
import Compiler.IR.Function;
import Compiler.IR.IRProgram;
import Compiler.IR.Instruction.*;
import Compiler.IR.Operand.Immediate;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.StackSlot;

import java.util.*;

import static Compiler.IR.RegisterSet.*;

public class StackFrameBuilder {

    class Frame {
        public LinkedList<StackSlot> parameters;
        public LinkedList<StackSlot> temporaries;

        public Frame() {
            parameters = new LinkedList<>();
            temporaries = new LinkedList<>();
        }

        public int getFrameSize() {
            int bytes = Config.REGISTER_WIDTH * (parameters.size() + temporaries.size());
            bytes = (bytes + 16 - 1) / 16 * 16; //  round up to a multiply of 16
            return bytes;
        }
    }

    private IRProgram irProgram;
    public Map<Function, Frame> frameMap;

    public StackFrameBuilder(IRProgram irProgram) {
        this.irProgram = irProgram;
        frameMap = new HashMap<>();
    }

    public void run() {
        for (Function function : irProgram.functionList) {
            process(function);
        }
    }

    private void process(Function function) {
        Frame frame = new Frame();
        for (int i = 6; i < function.parameterList.size(); ++i) {
            frame.parameters.add((StackSlot) function.parameterList.get(i).spillPlace);
        }
        Set<StackSlot> stackSlots = new HashSet<>();
        for (BasicBlock basicBlock : function.basicBlockList)
            for (IRInstruction instruction = basicBlock.head; instruction != null; instruction = instruction.nxt) {
                List<StackSlot> slots = (instruction.getStackSlot());
                for (StackSlot stackSlot : slots) {
                    if (!frame.parameters.contains(stackSlot)) {
                        stackSlots.add(stackSlot);
                    }
                }
            }
        frame.temporaries.addAll(stackSlots);

        for (int i = 0; i < frame.parameters.size(); ++i) {
            StackSlot stackSlot = frame.parameters.get(i);
            stackSlot.base = rbp;
            stackSlot.displacement = new Immediate(16 + 8 * i); //skip return address
        }
        for (int i = 0; i < frame.temporaries.size(); ++i) {
            StackSlot stackSlot = frame.temporaries.get(i);
            stackSlot.base = rbp;
            stackSlot.displacement = new Immediate(-8 - 8 * i);
        }

//        prologue
        IRInstruction instruction = function.enterBB.head;
        instruction.prepend(new Push(function.enterBB, rbp));
        instruction.prepend(new Move(function.enterBB, rbp, rsp));
        instruction.prepend(new BinaryInst(function.enterBB, BinaryInst.Op.SUB, rsp, new Immediate(frame.getFrameSize())));
//        push callee-saved regs
        instruction = instruction.pre;  // after sub
        List<Register> usedCalleeSaved = new LinkedList<>(calleeSave); //todo: function.usedCalleeSavedRegs();
        for (Register register : usedCalleeSaved) {
            instruction.append(new Push(function.enterBB, register));
        }

        instruction = function.leaveBB.tail;
//        pop saved-regs
        for (Register register : usedCalleeSaved) {
            instruction.prepend(new Pop(function.leaveBB, register));
        }
        instruction.prepend(new Leave(function.leaveBB));

        frameMap.put(function, frame);
    }
}

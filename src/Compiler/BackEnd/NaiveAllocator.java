package Compiler.BackEnd;

import Compiler.IR.BasicBlock;
import Compiler.IR.Function;
import Compiler.IR.IRProgram;
import Compiler.IR.Instruction.IRInstruction;
import Compiler.IR.Instruction.Move;
import Compiler.IR.Operand.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static Compiler.IR.RegisterSet.*;

public class NaiveAllocator {

    private IRProgram irProgram;
    private List<PhysicalRegister> generalRegs = new LinkedList<>();

    public NaiveAllocator(IRProgram irProgram) {
        this.irProgram = irProgram;
        this.generalRegs.add(rbx);
        this.generalRegs.add(r10);
        this.generalRegs.add(r11);
        this.generalRegs.add(r12);
        this.generalRegs.add(r13);
        this.generalRegs.add(r14);
        this.generalRegs.add(r15);
    }

    private PhysicalRegister getPhysical(Operand v) {
        if (v instanceof VirtualRegister) {
            return ((VirtualRegister) v).allocatedPhysicalRegister;
        } else {
            return null;
        }
    }

    public void run() {
        for (Function function : irProgram.functionList) {
            for (BasicBlock basicBlock : function.basicBlockList) {
                for (IRInstruction inst = basicBlock.head; inst != null; inst = inst.nxt) {
                    List<Register> usedRegs = inst.usedRegs();
                    List<Register> storeRegs = inst.storeRegs();
                    List<Register> allRegs = new LinkedList<>();
                    allRegs.addAll(usedRegs);
                    allRegs.addAll(storeRegs);
                    Map<Register, Register> renameMap = new HashMap<>();

                    for (Register register : allRegs) {
                        assert register instanceof VirtualRegister;
                        VirtualRegister vr = (VirtualRegister) register;
                        if (vr.allocatedPhysicalRegister != null) {
                            renameMap.put(vr, vr.allocatedPhysicalRegister);
                            continue;
                        }
                        if (vr.spillPlace == null) {
                            vr.spillPlace = new StackSlot("");
                        }
                    }

                    if (inst instanceof Move) {
                        Move move = (Move) inst;
                        Address dest = move.dest;
                        Operand src = move.src;
                        PhysicalRegister pdest = getPhysical(dest);
                        PhysicalRegister psrc = getPhysical(src);
                        if (pdest != null && psrc != null) {
                            move.dest = pdest;
                            move.src = psrc;
                            continue;
                        } else if (pdest != null) {
                            move.dest = pdest;
                            if (move.src instanceof VirtualRegister) {
                                move.src = ((VirtualRegister) move.src).spillPlace;
                            } else if (move.src instanceof Constant) {
                            } else {
                                assert false;
                            }
                            continue;
                        } else if (psrc != null) {
                            move.src = psrc;
                            if (move.dest instanceof VirtualRegister) {
                                move.dest = ((VirtualRegister) move.dest).spillPlace;
                            } else {
                                assert false;
                            }
                            continue;
                        }
                    }

                    int cnt = 0;
                    for (Register register : allRegs) {
                        VirtualRegister vr = (VirtualRegister) register;
                        if (!renameMap.containsKey(vr)) {
                            if (cnt == generalRegs.size()) {
                                assert false;
                                break;
                            } else {
                                renameMap.put(vr, generalRegs.get(cnt++));
                            }
                        }
                    }

                    inst.renameRegs(renameMap);

                    for (Register register : usedRegs) {
                        VirtualRegister vr = (VirtualRegister) register;
                        if (vr.allocatedPhysicalRegister == null) {
                            inst.prepend(new Move(inst.bb, renameMap.get(vr), vr.spillPlace));
                        }
                    }

                    for (Register register : storeRegs) {
                        VirtualRegister vr = (VirtualRegister) register;
                        if (vr.allocatedPhysicalRegister == null) {
                            inst.append(new Move(inst.bb, vr.spillPlace, renameMap.get(vr)));
                            inst = inst.nxt;
                        }
                    }
                }
            }
        }
    }
}

/*
                curBB.append(new Move(curBB, curFunction.parameterList.get(i), RegisterSet.vargs.get(i)));
                curBB.append(new Move(curBB, vrax, new Immediate(0)));
            curBB.append(new Move(curBB, variableSymbol.virtualRegister, variableSymbol.virtualRegister.spillPlace));

 */
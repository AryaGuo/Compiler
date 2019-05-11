package Compiler.BackEnd;

import Compiler.IR.BasicBlock;
import Compiler.IR.Function;
import Compiler.IR.IRProgram;
import Compiler.IR.Instruction.IRInstruction;
import Compiler.IR.Instruction.Move;
import Compiler.IR.Operand.PhysicalRegister;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.VirtualRegister;

import java.util.*;

import static Compiler.IR.RegisterSet.*;

public class AdvancedGraphAllocator {

    private IRProgram irProgram;
    private List<PhysicalRegister> generalRegs = new LinkedList<>();
    private int K;
    private Function curFunction;

    private LivenessAnalyzer livenessAnalyzer;
    private LivenessAnalyzer.Graph graph;
    private LivenessAnalyzer.Graph oldGraph;

    private Set<VirtualRegister> simplifyList;
    private Set<VirtualRegister> freezeList;
    private Set<VirtualRegister> spillList;
    private Set<VirtualRegister> spilledNodes;
    private Set<VirtualRegister> coalescedNodes;
    private LinkedList<VirtualRegister> stack;

    private Set<Move> coalescedMoves;
    private Set<Move> constrainedMoves;
    private Set<Move> frozenMoves;
    private Set<Move> worklistMoves;
    private Set<Move> activeMoves;

    private Map<VirtualRegister, List<Move>> moveList;
    private Map<VirtualRegister, PhysicalRegister> colorMap;
    private Map<VirtualRegister, VirtualRegister> alias;

    public AdvancedGraphAllocator(IRProgram irProgram) {
        this.irProgram = irProgram;
        this.generalRegs.add(rdx);
        this.generalRegs.add(rbx);
        this.generalRegs.add(rcx);
        this.generalRegs.add(rsi);
        this.generalRegs.add(rdi);
        this.generalRegs.add(r8);
        this.generalRegs.add(r9);
        this.generalRegs.add(r10);
        this.generalRegs.add(r11);
        this.generalRegs.add(r12);
        this.generalRegs.add(r13);
        this.generalRegs.add(r14);
        this.generalRegs.add(r15);
        K = generalRegs.size();
        livenessAnalyzer = new LivenessAnalyzer();
    }

    public void run() {
        for (Function function : irProgram.functionList) {
            curFunction = function;
            process();
        }
    }

    private void process() {
        graph = livenessAnalyzer.getInterferenceGraph(curFunction);
        build();
    }

    private void build() {
        for (BasicBlock basicBlock : curFunction.basicBlockList) {
            Set<VirtualRegister> liveNow = new HashSet<>(basicBlock.liveOut);
            for (IRInstruction irInstruction = basicBlock.tail; irInstruction != null; irInstruction = irInstruction.pre) {
                List<VirtualRegister> defRegs = toVR(irInstruction.defRegs());
                List<VirtualRegister> useRegs = toVR(irInstruction.useRegs());
                List<VirtualRegister> allRegs = new LinkedList<>();
                allRegs.addAll(defRegs);
                allRegs.addAll(useRegs);

                if (irInstruction instanceof Move) {
                    liveNow.removeAll(useRegs);
                    for (VirtualRegister vr : allRegs) {
                        if (moveList.containsKey(vr)) {
                            moveList.get(vr).add((Move) irInstruction);
                        } else {
                            List<Move> list = new LinkedList<>();
                            list.add((Move) irInstruction);
                            moveList.put(vr, list);
                        }
                    }
                    worklistMoves.add((Move) irInstruction);
                }
                liveNow.removeAll(toVR(irInstruction.defRegs()));
                liveNow.addAll(toVR(irInstruction.useRegs()));
            }
        }
    }

    private List<VirtualRegister> toVR(List<Register> regs) {
        List<VirtualRegister> list = new LinkedList<>();
        for (Register reg : regs) {
            if (reg instanceof VirtualRegister) {
                list.add((VirtualRegister) reg);
            }
        }
        return list;
    }
}

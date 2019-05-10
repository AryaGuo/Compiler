package Compiler.BackEnd;

import Compiler.BackEnd.LivenessAnalyzer.Graph;
import Compiler.IR.BasicBlock;
import Compiler.IR.Function;
import Compiler.IR.IRProgram;
import Compiler.IR.Instruction.IRInstruction;
import Compiler.IR.Instruction.Move;
import Compiler.IR.Operand.*;

import java.util.*;

import static Compiler.IR.RegisterSet.*;

public class SimpleGraphAllocator {

    private IRProgram irProgram;
    private List<PhysicalRegister> generalRegs = new LinkedList<>();
    private int K;
    private Function curFunction;

    private LivenessAnalyzer livenessAnalyzer;
    private Graph graph;
    private Graph oldGraph;

    private Set<VirtualRegister> simplifyList;
    private Set<VirtualRegister> spillList;
    private Set<VirtualRegister> realSpill;
    private LinkedList<VirtualRegister> stack;
    private Map<VirtualRegister, PhysicalRegister> colorMap;

    public SimpleGraphAllocator(IRProgram irProgram) {
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
        while (true) {
            graph = livenessAnalyzer.getInterferenceGraph(curFunction);
            oldGraph = new Graph(graph);

            simplifyList = new HashSet<>();
            spillList = new HashSet<>();
            realSpill = new HashSet<>();
            stack = new LinkedList<>();
            colorMap = new HashMap<>();

            for (VirtualRegister virtualRegister : graph.neighbors.keySet()) {
                if (graph.degree(virtualRegister) < K) {
                    simplifyList.add(virtualRegister);
                } else {
                    spillList.add(virtualRegister);
                }
            }
            while (!simplifyList.isEmpty() || !spillList.isEmpty()) {
                if (!simplifyList.isEmpty()) {
                    simplify();
                } else {
                    potentialSpill();
                }
            }
            assignColors();
            if (realSpill.isEmpty()) {
                renameRegs();
                break;
            } else {
                rewriteFunction();
            }
        }
    }

    private void renameRegs() {
        Map<Register, Register> renameMap = new HashMap<>(colorMap);
        for (BasicBlock basicBlock : curFunction.basicBlockList) {
            for (IRInstruction instruction = basicBlock.head; instruction != null; instruction = instruction.nxt) {
                instruction.renameRegs(renameMap);
            }
        }
    }

    private void rewriteFunction() {
        Map<VirtualRegister, Memory> spillMap = new HashMap<>();
        for (VirtualRegister vr : realSpill) {
            if (vr.spillPlace != null) {
                spillMap.put(vr, vr.spillPlace);
            } else {
                spillMap.put(vr, new StackSlot(vr.hint));
            }
        }

        for (BasicBlock basicBlock : curFunction.basicBlockList) {
            for (IRInstruction instruction = basicBlock.head; instruction != null; instruction = instruction.nxt) {
                List<Register> useRegs = instruction.useRegs();
                List<Register> defRegs = instruction.defRegs();
                Map<Register, Register> renameMap = new HashMap<>();

                useRegs.retainAll(realSpill);
                defRegs.retainAll(realSpill);
                for (Register vr : useRegs) {
                    if (!renameMap.containsKey((vr))) {
                        renameMap.put(vr, new VirtualRegister(""));
                    }
                }
                for (Register vr : defRegs) {
                    if (!renameMap.containsKey((vr))) {
                        renameMap.put(vr, new VirtualRegister(""));
                    }
                }
                instruction.renameRegs(renameMap);
                for (Register vr : useRegs) {
                    instruction.prepend(new Move(instruction.bb, renameMap.get(vr), spillMap.get(vr)));
                }
                for (Register vr : defRegs) {
                    instruction.append(new Move(instruction.bb, spillMap.get(vr), renameMap.get(vr)));
                }
            }
        }

    }

    private void assignColors() {
        for (VirtualRegister vr : stack) {
            if (vr.allocatedPhysicalRegister != null) {
                colorMap.put(vr, vr.allocatedPhysicalRegister);
            }
        }
        for (VirtualRegister vr : stack) {
            if (vr.allocatedPhysicalRegister == null) {
                Set<PhysicalRegister> available = new HashSet<>(generalRegs);
                for (VirtualRegister nb : oldGraph.neighbors.get(vr)) {
                    if (colorMap.containsKey(nb)) {
                        available.remove(colorMap.get(nb));
                    }
                }
                if (available.isEmpty()) {
                    realSpill.add(vr);
                } else {
                    PhysicalRegister pr = null;
                    for (PhysicalRegister reg : callerSave) {
                        if (available.contains(reg)) {
                            pr = reg;
                            break;
                        }
                    }
                    if (pr == null) {
                        pr = available.iterator().next();
                    }
                    colorMap.put(vr, pr);
                }
            }
        }
    }

    private void potentialSpill() {
        int maxDeg = -2;
        VirtualRegister maxVr = null;
        for (VirtualRegister vr : spillList) {
            int deg = graph.degree(vr);
            if (vr.allocatedPhysicalRegister != null)
                deg = -1;
            if (deg > maxDeg) {
                maxDeg = deg;
                maxVr = vr;
            }
        }
        stack.addFirst(maxVr);
        graph.remove(maxVr);
        spillList.remove(maxVr);
    }

    private void simplify() {
        List<VirtualRegister> origin = new LinkedList<>(simplifyList);
        List<VirtualRegister> added = new LinkedList<>();
        for (VirtualRegister vr : simplifyList) {
            Set<VirtualRegister> nbs = graph.neighbors.get(vr);
            stack.addFirst(vr);
            graph.remove(vr);
            for (VirtualRegister nb : nbs) {
                if (spillList.contains(nb) && graph.degree(nb) < K) {
                    spillList.remove(nb);
                    added.add(nb);
                }
            }
        }
        simplifyList.removeAll(origin);
        simplifyList.addAll(added);
    }
}

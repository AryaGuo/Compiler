package Compiler.BackEnd;

import Compiler.BackEnd.LivenessAnalyzer.Graph;
import Compiler.IR.BasicBlock;
import Compiler.IR.Function;
import Compiler.IR.IRProgram;
import Compiler.IR.Instruction.IRInstruction;
import Compiler.IR.Instruction.Move;
import Compiler.IR.Operand.*;
import org.antlr.v4.runtime.misc.Pair;

import java.util.*;

import static Compiler.IR.RegisterSet.*;

public class AdvancedGraphAllocator {

    private IRProgram irProgram;
    private List<PhysicalRegister> generalRegs = new LinkedList<>();
    private int K;
    private Function curFunction;

    private LivenessAnalyzer livenessAnalyzer;
    private Graph graph;

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

    private Set<Pair<VirtualRegister, VirtualRegister>> adjSet;

    private Map<VirtualRegister, List<Move>> moveList;
    private Map<VirtualRegister, PhysicalRegister> colorMap;
    private Map<VirtualRegister, VirtualRegister> alias;
    private Map<VirtualRegister, Integer> degree;

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
//        this.generalRegs.add(r12);
//        this.generalRegs.add(r13);
//        this.generalRegs.add(r14);
//        this.generalRegs.add(r15);
        K = generalRegs.size();
        livenessAnalyzer = new LivenessAnalyzer();
    }

    public void run() {
        for (Function function : irProgram.functionList) {
            curFunction = function;
            process();
        }
    }

    private void init() {
        graph = new Graph();
        simplifyList = new HashSet<>();
        freezeList = new HashSet<>();
        spillList = new HashSet<>();
        spilledNodes = new HashSet<>();
        coalescedNodes = new HashSet<>();
        stack = new LinkedList<>();

        coalescedMoves = new HashSet<>();
        constrainedMoves = new HashSet<>();
        frozenMoves = new HashSet<>();
        worklistMoves = new HashSet<>();
        activeMoves = new HashSet<>();

        adjSet = new HashSet<>();

        moveList = new HashMap<>();
        colorMap = new HashMap<>();
        alias = new HashMap<>();
        degree = new HashMap<>();
    }

    private void process() {
        while (true) {
            init();
            livenessAnalyzer.getLiveOut(curFunction, false);
            build();
            makeWorkList();
            do {
                if (!simplifyList.isEmpty()) {
                    simplify();
                } else if (!worklistMoves.isEmpty()) {
                    coalesce();
                } else if (!freezeList.isEmpty()) {
                    freeze();
                } else if (!spillList.isEmpty()) {
                    selectSpill();
                }
            } while (!simplifyList.isEmpty() || !worklistMoves.isEmpty() || !freezeList.isEmpty() || !spillList.isEmpty());
            assignColors();
            if (spilledNodes.isEmpty()) {
                renameRegs();
                break;
            } else {
                rewriteFunction();
            }
        }
    }

    private void build() {
        HashSet<Register> initial = new HashSet<>();
        for (BasicBlock basicBlock : curFunction.basicBlockList) {
            for (IRInstruction irInstruction = basicBlock.head; irInstruction != null; irInstruction = irInstruction.nxt) {
//                graph.addNodes(irInstruction.useRegs());
//                graph.addNodes(irInstruction.defRegs());
                initial.addAll(irInstruction.useRegs());
                initial.addAll(irInstruction.defRegs());
            }
        }
        for (Register vr : initial) {
            moveList.put((VirtualRegister) vr, new LinkedList<>());
            degree.put((VirtualRegister) vr, 0);
        }
        initial.removeAll(vallRegs);
        graph.addNodes(new LinkedList<>(initial));
        for (BasicBlock basicBlock : curFunction.basicBlockList) {
            Set<VirtualRegister> liveNow = new HashSet<>(basicBlock.liveOut);
            for (IRInstruction irInstruction = basicBlock.tail; irInstruction != null; irInstruction = irInstruction.pre) {
                List<VirtualRegister> defRegs = toVR(irInstruction.defRegs());
                List<VirtualRegister> useRegs = toVR(irInstruction.useRegs());
                List<VirtualRegister> allRegs = new LinkedList<>();
                allRegs.addAll(defRegs);
                allRegs.addAll(useRegs);

                if (validMove(irInstruction)) {
//                    liveNow.removeAll(useRegs);
                    for (VirtualRegister vr : allRegs) {
                        moveList.get(vr).add((Move) irInstruction);
                    }
                    worklistMoves.add((Move) irInstruction);
                }
//                liveNow.addAll(defRegs);// TODO
                for (VirtualRegister vr1 : liveNow) {
                    for (VirtualRegister vr2 : defRegs) {
                        addEdge(vr1, vr2);
                    }
                }
                liveNow.removeAll(defRegs);
                liveNow.addAll(useRegs);
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
        for (VirtualRegister vr : spilledNodes) {
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

                useRegs.retainAll(spilledNodes);
                defRegs.retainAll(spilledNodes);
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
        for (VirtualRegister vr : vallRegs) {
            colorMap.put(vr, vr.allocatedPhysicalRegister);
        }
        for (VirtualRegister vr : stack) {
            if (vr.allocatedPhysicalRegister == null) {
                Set<PhysicalRegister> available = new HashSet<>(generalRegs);
                for (VirtualRegister nb : graph.neighbors.get(vr)) {
                    if (colorMap.containsKey(getAlias(nb))) {
                        available.remove(colorMap.get(getAlias(nb)));
                    }
                }
                if (available.isEmpty()) {
                    spilledNodes.add(vr);
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
        topsort();
        for (VirtualRegister vr : coalescedNodes) {
            colorMap.put(vr, colorMap.get(getAlias(vr)));
        }
    }

    private void topsort() {
        Map<VirtualRegister, Integer> outDeg = new HashMap<>();
        LinkedList<VirtualRegister> available = new LinkedList<>();
        Map<VirtualRegister, LinkedList<VirtualRegister>> edge = new HashMap<>();
        for (VirtualRegister vr : alias.keySet()) {
            VirtualRegister val = alias.get(vr);
            int deg = outDeg.getOrDefault(vr, 0) + 1;
            outDeg.put(vr, deg);
            LinkedList<VirtualRegister> list = edge.getOrDefault(val, new LinkedList<>());
            list.add(vr);
            edge.put(val, list);
        }
        for (VirtualRegister vr : alias.values()) {
            if (outDeg.getOrDefault(vr, 0) == 0) {
                available.add(vr);
            }
        }
        while (!available.isEmpty()) {
            VirtualRegister node = available.getFirst();
            available.removeFirst();
            LinkedList<VirtualRegister> list = edge.getOrDefault(node, new LinkedList<>());
            for (VirtualRegister vr : list) {
                int deg = outDeg.get(vr) - 1;
                outDeg.put(node, deg);
                if (deg == 0) {
                    available.add(vr);
                    VirtualRegister pa = getAlias(vr);
                    alias.put(vr, alias.getOrDefault(pa, pa));
                }
            }
        }
    }

    private void selectSpill() {
        int maxDeg = -2;
        VirtualRegister x = null;
        for (VirtualRegister vr : spillList) {
            int deg = degree.get(vr);
            if (vr.allocatedPhysicalRegister != null) {
                deg = -1;
            }
            if (deg > maxDeg) {
                maxDeg = deg;
                x = vr;
            }
        }
        spillList.remove(x);
//        graph.remove(x); todo
        simplifyList.add(x);
        freezeMoves(x);
    }

    private void freeze() {
        VirtualRegister u = freezeList.iterator().next();
        freezeList.remove(u);
        simplifyList.add(u);
        freezeMoves(u);
    }

    private void freezeMoves(VirtualRegister u) {
        for (Move move : getNodeMoves(u)) {
            VirtualRegister y = (VirtualRegister) move.src;
            VirtualRegister x = (VirtualRegister) move.dest;  //todo
            VirtualRegister v;
            if (getAlias(y) == getAlias(u)) {
                v = getAlias(x);
            } else {
                v = getAlias(y);
            }
            activeMoves.remove(move);
            frozenMoves.add(move);
            if (freezeList.contains(v) && getNodeMoves(v).isEmpty()) {
                freezeList.remove(v);
                simplifyList.add(v);
            }
        }
    }

    private void coalesce() {
        Move move = worklistMoves.iterator().next();
        VirtualRegister y = getAlias((VirtualRegister) move.src);
        VirtualRegister x = getAlias((VirtualRegister) move.dest);  //todo
        VirtualRegister u, v;
        if (y.allocatedPhysicalRegister != null) {
            u = y;
            v = x;
        } else {
            u = x;
            v = y;
        }
        worklistMoves.remove(move);
        if (u == v) {
            coalescedMoves.add(move);
            addWorkList(u);
        } else if (v.allocatedPhysicalRegister != null || isAdj(u, v)) {
            constrainedMoves.add(move);
            addWorkList(u);
            addWorkList(v);
        } else if (u.allocatedPhysicalRegister != null && check(u, v) ||
                u.allocatedPhysicalRegister == null && conservative(getAdj(u), getAdj(v))) {
            coalescedMoves.add(move);
            combine(u, v);
            addWorkList(u);
        } else {
            activeMoves.add(move);
        }
    }

    private boolean check(VirtualRegister u, VirtualRegister v) {
        for (VirtualRegister t : getAdj(v)) {
            if (!OK(t, u)) {
                return false;
            }
        }
        return true;
    }

    private void combine(VirtualRegister u, VirtualRegister v) {
        if (freezeList.contains(v)) {
            freezeList.remove(v);
        } else {
            spillList.remove(v);
        }
        coalescedNodes.add(v);
        alias.put(v, u);    // TODO
        moveList.get(u).addAll(moveList.get(v));
        enableMoves(v);
        for (VirtualRegister t : getAdj(v)) {
            addEdge(t, u);
            decDegree(t);
        }
        if (degree.get(u) >= K && freezeList.contains(u)) {
            freezeList.remove(u);
            spillList.add(u);
        }
    }

    private boolean conservative(HashSet<VirtualRegister>... adj) {
        int k = 0;
        for (HashSet<VirtualRegister> set : adj) {
            for (VirtualRegister vr : set) {
                if (degree.get(vr) >= K) {
                    if (++k >= K) {
                        return false;
                    }
                }
            }
        }
        return k < K;
    }

    private boolean OK(VirtualRegister t, VirtualRegister r) {
        return degree.get(t) < K || t.allocatedPhysicalRegister != null || isAdj(t, r);
    }

    private boolean isAdj(VirtualRegister u, VirtualRegister v) {
        return adjSet.contains(new Pair<>(u, v));
//        return graph.neighbors.get(u).contains(v) || graph.neighbors.get(v).contains(u);
    }

    private void addAdj(VirtualRegister u, VirtualRegister v) {
        adjSet.add(new Pair<>(u, v));
        adjSet.add(new Pair<>(v, u));
    }

    private void addWorkList(VirtualRegister u) {
        if (u.allocatedPhysicalRegister == null && !isMoveRelated(u) && degree.get(u) < K) {
            freezeList.remove(u);
            simplifyList.add(u);
        }
    }

    private VirtualRegister getAlias(VirtualRegister vr) {
        if (!alias.containsKey(vr)) {
            return vr;
        }
        return getAlias(alias.get(vr));
//        return alias.getOrDefault(vr, vr);
    }

    private HashSet<VirtualRegister> getAdj(VirtualRegister vr) {
        HashSet<VirtualRegister> ret = new HashSet<>(graph.neighbors.get(vr));
        ret.removeAll(stack);
        ret.removeAll(coalescedNodes);
        return ret;
    }

    private HashSet<Move> getNodeMoves(VirtualRegister vr) {
        HashSet<Move> ret = new HashSet<>(activeMoves);
        ret.addAll(worklistMoves);
        ret.retainAll(moveList.get(vr));
        return ret;
    }

    private boolean isMoveRelated(VirtualRegister vr) {
        return !getNodeMoves(vr).isEmpty();
    }

    private void simplify() {
        VirtualRegister vr = simplifyList.iterator().next();
        simplifyList.remove(vr);
//        graph.remove(vr); TODO: 2019-05-13
        stack.addFirst(vr);
        for (VirtualRegister nb : getAdj(vr)) {
            decDegree(nb);
        }
    }

    private void decDegree(VirtualRegister vr) {
        int deg = degree.get(vr);
        degree.put(vr, deg - 1);
        if (deg == K) {
            HashSet<VirtualRegister> set = getAdj(vr);
            set.add(vr);
            enableMoves(set);
            spillList.remove(vr);
            if (isMoveRelated(vr)) {
                freezeList.add(vr);
            } else {
                simplifyList.add(vr);
            }
        }
    }

    private void enableMoves(VirtualRegister vr) {
        for (Move move : getNodeMoves(vr)) {
            if (activeMoves.contains(move)) {
                activeMoves.remove(move);
                worklistMoves.add(move);
            }
        }
    }

    private void enableMoves(HashSet<VirtualRegister> set) {
        for (VirtualRegister vr : set) {
            enableMoves(vr);
        }
    }

    private void makeWorkList() {
        for (VirtualRegister vr : graph.neighbors.keySet()) {
            if (degree.get(vr) >= K) {
                spillList.add(vr);
            } else if (isMoveRelated(vr)) {
                freezeList.add(vr);
            } else {
                simplifyList.add(vr);
            }
        }
    }

    private boolean validMove(IRInstruction inst) {
        if (inst instanceof Move) {
            return ((Move) inst).dest instanceof VirtualRegister && ((Move) inst).src instanceof VirtualRegister;
        }
        return false;
    }

    private void addEdge(VirtualRegister vr1, VirtualRegister vr2) {
        if (vr1 == vr2 || isAdj(vr1, vr2)) return;
        addAdj(vr1, vr2);
        if (vr1.allocatedPhysicalRegister == null) {
            degree.put(vr1, degree.get(vr1) + 1);
            graph.neighbors.get(vr1).add(vr2);
        }
        if (vr2.allocatedPhysicalRegister == null) {
            degree.put(vr2, degree.get(vr2) + 1);
            graph.neighbors.get(vr2).add(vr1);
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

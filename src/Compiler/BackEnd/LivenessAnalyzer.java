package Compiler.BackEnd;

import Compiler.IR.BasicBlock;
import Compiler.IR.Function;
import Compiler.IR.Instruction.Call;
import Compiler.IR.Instruction.IRInstruction;
import Compiler.IR.Instruction.Move;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.VirtualRegister;

import java.util.*;

public class LivenessAnalyzer {
    public static class Graph {
        public Map<VirtualRegister, Set<VirtualRegister>> neighbors;

        public Graph() {
            neighbors = new HashMap<>();
        }

        public Graph(Graph graph) {
            neighbors = new HashMap<>();
            for (VirtualRegister vr : graph.neighbors.keySet()) {
                neighbors.put(vr, new HashSet<>(graph.neighbors.get(vr)));
            }
        }

        public void addNodes(List<Register> list) {
            for (Register register : list) {
                neighbors.put((VirtualRegister) register, new HashSet<>());
            }
        }

        public void addEdge(VirtualRegister vr1, VirtualRegister vr2) {
            if (vr1 == vr2) return;
            neighbors.get(vr1).add(vr2);
            neighbors.get(vr2).add(vr1);
        }

        public int size() {
            return neighbors.size();
        }

        public void remove(VirtualRegister virtualRegister) {
            Set<VirtualRegister> nbs = neighbors.get(virtualRegister);
            for (VirtualRegister vr : nbs) {
                neighbors.get(vr).remove(virtualRegister);
            }
            neighbors.remove(virtualRegister);
        }

        public int degree(VirtualRegister virtualRegister) {
            return neighbors.get(virtualRegister).size();
        }
    }

    public void getLiveout(Function function, boolean flag) {
        for (BasicBlock basicBlock : function.basicBlockList) {
            init(basicBlock, flag);
        }
        calLiveOut(function);
    }

    private void calLiveOut(Function function) {
        boolean flag = true;
        while (flag) {
            flag = false;
            for (BasicBlock basicBlock : function.reversePostOrderOnReverseCFG) {
                Set<VirtualRegister> newLiveOut = calLiveOut(basicBlock);
                if (newLiveOut.size() != basicBlock.liveOut.size()) {
                    flag = true;
                    basicBlock.liveOut = newLiveOut;
                }
            }
        }
    }

    public Graph getInterferenceGraph(Function function) {
        for (BasicBlock basicBlock : function.basicBlockList) {
            init(basicBlock, false);
        }
        calLiveOut(function);
        Graph graph = new Graph();
        process(graph, function);
        return graph;
    }

    private void process(Graph graph, Function function) {
        for (BasicBlock basicBlock : function.basicBlockList) {
            for (IRInstruction irInstruction = basicBlock.head; irInstruction != null; irInstruction = irInstruction.nxt) {
                graph.addNodes(irInstruction.useRegs());
                graph.addNodes(irInstruction.defRegs());
            }
        }
        for (BasicBlock basicBlock : function.basicBlockList) {
            Set<VirtualRegister> liveNow = new HashSet<>(basicBlock.liveOut);
            for (IRInstruction irInstruction = basicBlock.tail; irInstruction != null; irInstruction = irInstruction.pre) {
                boolean flag = irInstruction instanceof Move && ((Move) irInstruction).src instanceof Register
                        && ((Move) irInstruction).dest instanceof Register;
                for (VirtualRegister vr1 : liveNow) {
                    for (Register vr2 : irInstruction.defRegs()) {
                        if (flag && vr1 == ((Move) irInstruction).src) {
                            continue;
                        }
                        graph.addEdge(vr1, (VirtualRegister) vr2);
                    }
                }
                liveNow.removeAll(toVR(irInstruction.defRegs()));
                liveNow.addAll(toVR(irInstruction.useRegs()));
            }
        }
    }

    private void init(BasicBlock basicBlock, boolean flag) {
        Set<VirtualRegister> gen = new HashSet<>();
        Set<VirtualRegister> kill = new HashSet<>();
        for (IRInstruction irInstruction = basicBlock.head; irInstruction != null; irInstruction = irInstruction.nxt) {
            List<Register> useRegs = (irInstruction instanceof Call && flag) ? ((Call) irInstruction).callArgs() : irInstruction.useRegs();
            List<Register> defRegs = irInstruction.defRegs();
            for (Register register : useRegs) {
                assert register instanceof VirtualRegister;
                if (!kill.contains(register)) {
                    gen.add((VirtualRegister) register);
                }
            }
            for (Register register : defRegs) {
                assert register instanceof VirtualRegister;
                kill.add((VirtualRegister) register);
            }
        }
        basicBlock.gen = gen;
        basicBlock.kill = kill;
        basicBlock.liveOut = new HashSet<>();
    }

    private Set<VirtualRegister> calLiveOut(BasicBlock basicBlock) {
        Set<VirtualRegister> ret = new HashSet<>();
        for (BasicBlock succ : basicBlock.successors) {
            Set<VirtualRegister> liveIn = new HashSet<>(succ.liveOut);
            liveIn.removeAll(succ.kill);
            liveIn.addAll(succ.gen);
            ret.addAll(liveIn);
        }
        return ret;
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

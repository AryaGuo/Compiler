package Compiler.IR;

import Compiler.IR.Instruction.CJump;
import Compiler.IR.Instruction.Call;
import Compiler.IR.Instruction.IRInstruction;
import Compiler.IR.Instruction.Jump;
import Compiler.IR.Operand.PhysicalRegister;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.VirtualRegister;
import Compiler.Symbol.VariableSymbol;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static Compiler.IR.RegisterSet.calleeSave;

public class Function {

    public enum Type {
        External, Library, UserDefined;
    }

    public String name;
    public boolean hasReturnValue;
    public Type type;

    public BasicBlock enterBB;
    public BasicBlock leaveBB;

    public List<BasicBlock> basicBlockList;
    public List<VirtualRegister> parameterList;

    public LinkedList<BasicBlock> reversePostOrder;

    public Set<VariableSymbol> usedGlobalVariables;
    public Set<VariableSymbol> recursiveUsedGlobalVariables;
    public Set<Function> callee;
    private Set<BasicBlock> visitedBasicBlocks;

    private Set<Function> visitedFunctions;

    public Function(String name, boolean hasReturnValue, Type type) {
        this.name = name;
        this.hasReturnValue = hasReturnValue;
        this.type = type;
        basicBlockList = new LinkedList<>();
        parameterList = new LinkedList<>();
        reversePostOrder = new LinkedList<>();
        usedGlobalVariables = new HashSet<>();
        recursiveUsedGlobalVariables = new HashSet<>();
        callee = new HashSet<>();
        visitedFunctions = new HashSet<>();
        visitedBasicBlocks = new HashSet<>();
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }

    public void finishBuild() {
        BasicBlock target;

        for (BasicBlock basicBlock : basicBlockList) {
            for (IRInstruction instruction = basicBlock.head; instruction != null; instruction = instruction.nxt) {
                if (instruction instanceof Jump) {
                    for (IRInstruction inst = instruction.nxt; inst != null; inst = inst.nxt) {
                        inst.remove();
                    }
                }
            }
        }

        for (BasicBlock basicBlock : basicBlockList) {
            if (basicBlock.tail instanceof Jump) {
                target = ((Jump) basicBlock.tail).targetBB;
                basicBlock.successors.add(target);
                target.frontiers.add(basicBlock);
            } else if (basicBlock.tail instanceof CJump) {
                target = ((CJump) basicBlock.tail).thenBB;
                basicBlock.successors.add(target);
                target.frontiers.add(basicBlock);
                target = ((CJump) basicBlock.tail).elseBB;
                basicBlock.successors.add(target);
                target.frontiers.add(basicBlock);
            }
        }

        visitedFunctions.clear();
        recursiveUsedGlobalVariables.clear();
        dfsRecursiveUsedGlobalVariables(this);

        visitedBasicBlocks.clear();
        reversePostOrder.clear();
        dfsReversePostOrder(enterBB);
    }

    private void dfsRecursiveUsedGlobalVariables(Function node) {
        if (visitedFunctions.contains(node)) return;
        visitedFunctions.add(node);
        for (Function func : node.callee)
            dfsRecursiveUsedGlobalVariables(func);
        recursiveUsedGlobalVariables.addAll(node.usedGlobalVariables);
    }

    private void dfsReversePostOrder(BasicBlock node) {
        if (visitedBasicBlocks.contains(node)) return;
        visitedBasicBlocks.add(node);
        for (BasicBlock bb : node.successors)
            dfsReversePostOrder(bb);
        reversePostOrder.addFirst(node);
    }


    public List<Register> usedCalleeSavedRegs() {
        List<Register> regs = new LinkedList<>();
        List<Register> ret = new LinkedList<>();
        for (BasicBlock basicBlock : this.basicBlockList)
            for (IRInstruction instruction = basicBlock.head; instruction != null; instruction = instruction.nxt) {
                regs.addAll(instruction instanceof Call ? ((Call) instruction).callUsedRegs() : instruction.usedRegs());
                regs.addAll(instruction instanceof Call ? ((Call) instruction).callStoreRegs() : instruction.storeRegs());
            }
        for (Register register : regs) {
            assert register instanceof PhysicalRegister;
            if (calleeSave.contains(register) && !ret.contains(register)) {
                ret.add(register);
            }
        }
        return ret;
    }
}

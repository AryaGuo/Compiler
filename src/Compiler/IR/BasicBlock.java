package Compiler.IR;

import Compiler.IR.Instruction.IRInstruction;

import java.util.LinkedList;
import java.util.List;

public class BasicBlock {
    public String hint;
    public Function function;
    public IRInstruction head, tail;

    public List<BasicBlock> frontiers;
    public List<BasicBlock> successors;

    private static int counter = 0;
    public int id;

    public BasicBlock(String hint, Function function) {
        this.function = function;
        this.hint = hint;
        frontiers = new LinkedList<>();
        successors = new LinkedList<>();
        function.basicBlockList.add(this);
        this.id = counter++;
    }

    public void append(IRInstruction instruction) {
        if (head == null) {
            head = tail = instruction;
            instruction.pre = instruction.nxt = null;
        } else {
            tail.append(instruction);
        }
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

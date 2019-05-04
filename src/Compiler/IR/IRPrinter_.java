package Compiler.IR;

import Compiler.IR.Instruction.*;
import Compiler.IR.Operand.*;

import java.io.FileOutputStream;
import java.io.PrintStream;

public class IRPrinter_ implements IRVisitor {
    private StringBuilder stringBuilder;
    private String curIndent;
    private String incIndent;

    public IRPrinter_() {
        stringBuilder = new StringBuilder();
        curIndent = "";
        incIndent = "  ";
    }

    public String toString() {
        return stringBuilder.toString();
    }

    public void printTo(FileOutputStream out) {
        PrintStream ps = new PrintStream(out);
        ps.println(toString());
    }

    private void indent() {
        curIndent = curIndent + incIndent;
    }

    private void unindent() {
        curIndent = curIndent.substring(0, curIndent.length() - incIndent.length());
    }

    private void appendNewLine(String s) {
        if (stringBuilder.length() != 0)
            stringBuilder.append("\n");
        stringBuilder.append(curIndent);
        stringBuilder.append(s);
    }

    private void appendCurrentLine(String s) {
        stringBuilder.append(s);
    }

    @Override
    public void visit(IRProgram program) {
        appendNewLine("IRProgram");
        appendNewLine("functionList");
        for (Function function : program.functionList) {
            indent();
            function.accept(this);
            unindent();
        }
        appendNewLine("dataList");
        for (StaticData staticData : program.dataList) {
            appendNewLine("");
            indent();
            staticData.accept(this);
            unindent();
        }
    }

    @Override
    public void visit(Function function) {
        appendNewLine("function " + function.name);
        indent();
        if (function.basicBlockList.size() > 0) {
            appendNewLine("basicBlockList");
            for (BasicBlock basicBlock : function.basicBlockList) {
                indent();
                basicBlock.accept(this);
                unindent();
            }
        }
        if (function.parameterList.size() > 0) {
            appendNewLine("parameterList");
            indent();
            for (VirtualRegister virtualRegister : function.parameterList) {
                appendNewLine("");
                indent();
                virtualRegister.accept(this);
                unindent();
            }
            unindent();
        }
        unindent();
    }

    @Override
    public void visit(BasicBlock basicBlock) {
        appendNewLine("BB " + basicBlock.hint);
        for (IRInstruction instruction = basicBlock.head; instruction != null; instruction = instruction.nxt) {
            indent();
            instruction.accept(this);
            unindent();
        }
    }

    @Override
    public void visit(VirtualRegister operand) {
        if (operand.id < 16) {
            appendCurrentLine(operand.hint);
        } else {
            appendCurrentLine("v" + (operand.id - 16));
            if (!operand.hint.equals("")) {
                appendCurrentLine("(" + operand.hint + ")");
            }
        }
    }

    @Override
    public void visit(PhysicalRegister operand) {
        appendCurrentLine(operand.name);
    }

    @Override
    public void visit(Memory operand) {
        appendCurrentLine("Memory (");
        boolean first = true;
        if (operand.base != null) {
            first = false;
            operand.base.accept(this);
        }
        if (operand.index != null) {
            if (!first) appendCurrentLine(" + ");
            first = false;
            operand.index.accept(this);
            appendCurrentLine(" * " + operand.scale);
        }
        if (operand.displacement != null) {
            if (!first) appendCurrentLine(" + ");
            operand.displacement.accept(this);
        }
        appendCurrentLine(")");
    }

    @Override
    public void visit(StackSlot operand) {
        appendCurrentLine("stack(" + operand.id + ") ");
        boolean first = true;
        if (operand.base != null) {
            first = false;
            operand.base.accept(this);
        }
        if (operand.index != null) {
            if (!first) appendCurrentLine(" + ");
            first = false;
            operand.index.accept(this);
            appendCurrentLine(" * " + operand.scale);
        }
        if (operand.displacement != null) {
            if (!first) appendCurrentLine(" + ");
            operand.displacement.accept(this);
        }
    }

    @Override
    public void visit(Immediate operand) {
        appendCurrentLine("" + operand.value);
    }

    @Override
    public void visit(StaticData operand) {
        appendCurrentLine("StaticData " + operand.hint);
        if (operand.init != null) {
            appendCurrentLine(" init = " + operand.init);
        }
    }

    @Override
    public void visit(BinaryInst inst) {
        appendNewLine("BinaryInst " + inst.op.toString() + " ");
        inst.dest.accept(this);
        appendCurrentLine(" ");
        inst.src.accept(this);
    }

    @Override
    public void visit(UnaryInst inst) {
        appendNewLine("UnaryInst " + inst.op.toString() + " ");
        inst.dest.accept(this);
    }

    @Override
    public void visit(Move inst) {
        appendNewLine("Move ");
        inst.dest.accept(this);
        appendCurrentLine(" ");
        inst.src.accept(this);
    }

    @Override
    public void visit(Push inst) {
        appendNewLine("Push ");
        inst.dest.accept(this);

    }

    @Override
    public void visit(Pop inst) {
        appendNewLine("Pop ");
        inst.dest.accept(this);
    }

    @Override
    public void visit(CJump inst) {
        appendNewLine("CJump " + inst.op.toString() + " ");
        inst.src1.accept(this);
        appendCurrentLine(" ");
        inst.src2.accept(this);
        appendCurrentLine(" ");
        appendCurrentLine("thenBB: " + inst.thenBB.hint);
        appendCurrentLine(" elseBB: " + inst.elseBB.hint);
    }

    @Override
    public void visit(Jump inst) {
        appendNewLine("Jump " + inst.targetBB.hint);
    }

    @Override
    public void visit(Lea inst) {
        appendNewLine("Lea ");
        inst.dest.accept(this);
        appendCurrentLine(" ");
        inst.src.accept(this);
    }

    @Override
    public void visit(Return inst) {
        appendNewLine("Return");

    }

    @Override
    public void visit(Call inst) {
        appendNewLine("Call ");
        inst.dest.accept(this);
        appendCurrentLine(" " + inst.function.name);
        indent();
        appendNewLine("args: ");
        for (Operand operand : inst.args) {
            operand.accept(this);
            appendCurrentLine(" ");
        }
        unindent();
    }

    @Override
    public void visit(Leave inst) {

    }

}

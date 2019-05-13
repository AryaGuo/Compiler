package Compiler.IR;

import Compiler.IR.Instruction.*;
import Compiler.IR.Operand.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

import static java.lang.System.exit;

public class IRPrinter implements IRVisitor {
    private StringBuilder stringBuilder;
    private HashMap<BasicBlock, String> bbNames;
    private HashMap<VirtualRegister, String> varNames;
    private HashMap<StackSlot, String> ssNames;
    private HashMap<StaticData, String> sdNames;

    private BasicBlock nextBasicBlock = null;

    private boolean inLeaInst;
    private int bbCount = 0;
    private int varCount = 0;
    private int ssCount = 0;
    private int sdCount = 0;

    public boolean showId = false;
    public boolean showBlockHint = false;
    public boolean showNasm = false;
    public boolean showHeader = false;

    public IRPrinter() {
        init();
    }

    public void init() {
        this.stringBuilder = new StringBuilder();
        this.bbNames = new HashMap<>();
        this.varNames = new HashMap<>();
        this.ssNames = new HashMap<>();
        this.sdNames = new HashMap<>();
        this.inLeaInst = false;
    }

    public String toString() {
        return stringBuilder.toString();
    }

    public void printTo(PrintStream out) {
        out.print(toString());
    }

    public void printTo(FileOutputStream out) {
        PrintStream ps = new PrintStream(out);
        ps.println(toString());
    }

    private void append(String s) {
        stringBuilder.append(s);
    }

    private String getBasicBlockName(BasicBlock bb) {
        if (!bbNames.containsKey(bb))
            bbNames.put(bb, "b" + bbCount++ + (showId ? "(" + bb.id + ")" : ""));
        return bbNames.get(bb);
    }

    private String getVirtualRegisterName(VirtualRegister virtualRegister) {
        if (!varNames.containsKey(virtualRegister))
            varNames.put(virtualRegister, "v" + varCount++ + (showId ? "(" + virtualRegister.id + ")" : ""));
        return varNames.get(virtualRegister);
    }

    private String getStackSlotName(StackSlot ss) {
        if (!ssNames.containsKey(ss))
            ssNames.put(ss, "stack[" + ssCount++ + "]");
        return ssNames.get(ss);
    }

    private String getStaticDataName(StaticData sd) {
        if (!sdNames.containsKey(sd))
            sdNames.put(sd, "g_" + sdCount++);
        return sdNames.get(sd);
    }

    @Override
    public void visit(IRProgram program) {
        if (showNasm && showHeader) {
            try {
                BufferedReader br = new BufferedReader(new FileReader("lib/lib.asm"));
                String line;
                while ((line = br.readLine()) != null) append(line + "\n");
                append(";=====================================================================\n");
                append("\t section .text\n");
            } catch (IOException e) {
                e.printStackTrace();
                exit(0);
            }
        }
        for (Function function : program.functionList)
            function.accept(this);

        if (showNasm) {
            append("\tsection .data\n");
            for (StaticData staticData : program.dataList) {
                append(getStaticDataName(staticData) + ":\n");
                if (staticData.init != null) {
                    append("\tdq " + staticData.init.length() + "\n");
                    append("\tdb ");
                    for (int i = 0; i < staticData.init.length(); i++) {
                        Formatter formatter = new Formatter();
                        formatter.format("%02XH, ", (int) staticData.init.charAt(i));
                        append(formatter.toString());
                    }
                    append("00H\n");
                } else {
                    append("\tdb ");
                    for (int i = 0; i < staticData.bytes; i++) {
                        if (i != 0)
                            append(", ");
                        append("00H");
                    }
                    append("\n");
                }
            }
        } else {
            for (StaticData staticData : program.dataList) {
                append(getStaticDataName(staticData) + ": " + staticData.bytes + " bytes");
                if (staticData.init != null)
                    append(" init: " + staticData.init);
                append("\n");
            }
        }
    }

    private String getNasmFunctionName(Function function) {
        switch (function.type) {
            case Library:
                return "__" + function.name;
            case External:
                return function.name;
            case UserDefined:
                return "_" + function.name;
            default:
                return null;
        }
    }

    @Override
    public void visit(Function function) {
        if (showNasm) {
            append(getNasmFunctionName(function) + ":\n");
        } else {
            append("define " + function.name + " ");
            append("(");
            boolean first = true;
            for (VirtualRegister vr : function.parameterList) {
                if (first)
                    first = false;
                else
                    append(",");
                vr.accept(this);
            }
            append(") {\n");
        }
        ArrayList<BasicBlock> reversePostOrder = new ArrayList<>(function.reversePostOrder);
        for (int i = 0; i < reversePostOrder.size(); i++) {
            BasicBlock bb = reversePostOrder.get(i);
            nextBasicBlock = (i + 1 == reversePostOrder.size()) ? null : reversePostOrder.get(i + 1);
            bb.accept(this);
        }
        if (!showNasm)
            append("}\n");
    }

    @Override
    public void visit(BasicBlock basicBlock) {
        append("  " + getBasicBlockName(basicBlock) + (showBlockHint && !showNasm ? "(" + basicBlock.hint + ")" : "") + ":\n");
        for (IRInstruction inst = basicBlock.head; inst != null; inst = inst.nxt)
            inst.accept(this);
    }

    @Override
    public void visit(VirtualRegister operand) {
        if (operand.allocatedPhysicalRegister != null) {
            visit(operand.allocatedPhysicalRegister);
            varNames.put(operand, operand.allocatedPhysicalRegister.name);
        } else
            append(getVirtualRegisterName(operand));
    }

    @Override
    public void visit(PhysicalRegister operand) {
        append(operand.name);
    }

    @Override
    public void visit(Memory operand) {
        boolean occur = false;
        if (!inLeaInst)
            append("qword ");
        append("[");
        if (operand.base != null) {
            operand.base.accept(this);
            occur = true;
        }
        if (operand.index != null) {
            if (occur)
                append(" + ");
            operand.index.accept(this);
            if (operand.scale != 1)
                append(" * " + operand.scale);
            occur = true;
        }
        if (operand.displacement != null) {
            Constant constant = operand.displacement;
            if (constant instanceof StaticData) {
                if (occur)
                    append(" + ");
                constant.accept(this);
            } else if (constant instanceof Immediate) {
                int value = ((Immediate) constant).value;
                if (occur) {
                    if (value > 0)
                        append(" + " + value);
                    else if (value < 0)
                        append(" - " + -value);
                } else {
                    append(String.valueOf(value));
                }
            }
        }
        append("]");
    }

    @Override
    public void visit(StackSlot operand) {
        if (operand.base != null || operand.index != null || operand.displacement != null) {
            visit((Memory) operand);
        } else {
            append(getStackSlotName(operand));
        }
    }

    @Override
    public void visit(Immediate operand) {
        append(String.valueOf(operand.value));
    }

    @Override
    public void visit(StaticData operand) {
        append(getStaticDataName(operand));
    }

    @Override
    public void visit(BinaryInst inst) {
        String op = inst.op.toString();
        if ((inst.op == BinaryInst.Op.ADD || inst.op == BinaryInst.Op.SUB) && inst.src instanceof Immediate && ((Immediate) inst.src).value == 0)
            return;
        if (inst.op == BinaryInst.Op.MUL) {
            append("\timul ");
            inst.src.accept(this);
            append("\n");
            return;
        }
        if (inst.op == BinaryInst.Op.DIV || inst.op == BinaryInst.Op.MOD) {
            append("\tidiv ");
            inst.src.accept(this);
            append("\n");
            return;
        }
        if (inst.op == BinaryInst.Op.SAL || inst.op == BinaryInst.Op.SAR) {
            append("\t" + op + " ");
            inst.dest.accept(this);
            append(", cl\n");
            return;
        }
        append("\t" + op + " ");
        inst.dest.accept(this);
        append(", ");
        inst.src.accept(this);
        append("\n");
    }

    @Override
    public void visit(UnaryInst inst) {
        String op = inst.op.toString();
        append("\t" + op + " ");
        inst.dest.accept(this);
        append("\n");
    }

    @Override
    public void visit(Move inst) {
        if (inst.src == inst.dest)
            return;
        append("\tmov ");
        inst.dest.accept(this);
        append(", ");
        inst.src.accept(this);
        append("\n");
    }

    @Override
    public void visit(Push inst) {
        append("\tpush ");
        inst.src.accept(this);
        append("\n");
    }

    @Override
    public void visit(Pop inst) {
        append("\tpop ");
        inst.dest.accept(this);
        append("\n");
    }

    @Override
    public void visit(CJump inst) {
        String op = "j" + inst.op.toString();
        if (showNasm) {
            append("\tcmp ");
            inst.src1.accept(this);
            append(", ");
            inst.src2.accept(this);
            append("\n");
            append("\t" + op + " " + getBasicBlockName(inst.thenBB) + "\n");
            if (inst.elseBB != nextBasicBlock)
                append("\tjmp" + " " + getBasicBlockName(inst.elseBB) + "\n");
        } else {
            append("\t" + op + " ");
            inst.src1.accept(this);
            append(", ");
            inst.src2.accept(this);
            append(", " + getBasicBlockName(inst.thenBB) + ", " + getBasicBlockName(inst.elseBB) + "\n");
        }
    }

    @Override
    public void visit(Jump inst) {
        if (inst.targetBB != nextBasicBlock)
            append("\tjmp " + getBasicBlockName(inst.targetBB) + "\n");
    }

    @Override
    public void visit(Lea inst) {
        inLeaInst = true;
        append("\tlea ");
        inst.dest.accept(this);
        append(", ");
        inst.src.accept(this);
        append("\n");
        inLeaInst = false;
    }

    @Override
    public void visit(Return inst) {
        append("\tret \n");
    }

    @Override
    public void visit(Call inst) {
        append("\tcall ");

        if (!showNasm && inst.dest != null) {
            inst.dest.accept(this);
            append(" = ");
        }

        if (showNasm)
            append(getNasmFunctionName(inst.function));
        else
            append(inst.function.name);

        if (!showNasm && inst.args != null) {
            for (Operand operand : inst.args) {
                append(", ");
                operand.accept(this);
            }
        }
        append("\n");
    }

    @Override
    public void visit(Leave inst) {
        append("\tleave\n");
    }

    @Override
    public void visit(Cdq inst) {
        append("\tcdq\n");
    }

//    @Override
//    public void visit(FunctionAddress operand) {
//        append(getNasmFunctionName(operand.function));
//    }
}

package Compiler.IR;

import Compiler.IR.Operand.StaticData;

import java.util.LinkedList;
import java.util.List;

public class IRProgram {
    //    correspond to 'text' section & 'data' section in NASM
    public List<Function> functionList;
    public List<StaticData> dataList;

    public IRProgram() {
        functionList = new LinkedList<>();
        dataList = new LinkedList<>();
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

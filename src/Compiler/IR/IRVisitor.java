package Compiler.IR;

import Compiler.IR.Instruction.*;
import Compiler.IR.Operand.*;

public interface IRVisitor {
    void visit(IRProgram program);

    void visit(Function function);

    void visit(BasicBlock basicBlock);

    void visit(VirtualRegister operand);

    void visit(PhysicalRegister operand);

    void visit(Memory operand);

    void visit(StackSlot operand);

    void visit(Immediate operand);

    void visit(StaticData operand);

    void visit(BinaryInst inst);

    void visit(UnaryInst inst);

    void visit(Move inst);

    void visit(Push inst);

    void visit(Pop inst);

    void visit(CJump inst);

    void visit(Jump inst);

    void visit(Lea inst);

    void visit(Return inst);

    void visit(Call inst);

    void visit(Leave inst);

    void visit(Cdq inst);
//    void visit(FunctionAddress operand);
}

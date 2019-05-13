package Compiler.Optimization;

import Compiler.BackEnd.LivenessAnalyzer;
import Compiler.IR.BasicBlock;
import Compiler.IR.Function;
import Compiler.IR.IRProgram;
import Compiler.IR.Instruction.*;
import Compiler.IR.Operand.Register;
import Compiler.IR.Operand.VirtualRegister;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
/*
    [A variable that is defined and not LIVE OUT is DEAD]
    do {
      computeLiveness()
      foreach instruction I {
        if (defs.contains(I) && !out.contains(I)) remove(I)
      }
    } while(changed)
 */

public class DeadCodeElimination {
    private IRProgram irProgram;
    private LivenessAnalyzer livenessAnalyzer;

    public DeadCodeElimination(IRProgram irProgram) {
        this.irProgram = irProgram;
        livenessAnalyzer = new LivenessAnalyzer();
    }

    public void run() {
        for (Function function : irProgram.functionList) {
            process(function);
        }
    }

    private void process(Function function) {
        livenessAnalyzer.getLiveOut(function, true);
        for (BasicBlock basicBlock : function.basicBlockList) {
            Set<VirtualRegister> liveNow = new HashSet<>(basicBlock.liveOut);
            for (IRInstruction irInstruction = basicBlock.tail; irInstruction != null; irInstruction = irInstruction.pre) {
                List<VirtualRegister> defRegs = toVR(irInstruction.defRegs());
                List<VirtualRegister> useRegs = toVR((irInstruction instanceof Call) ? ((Call) irInstruction).callArgs() : irInstruction.useRegs());
                boolean flag = false;
                if (defRegs.isEmpty()) {
                    flag = true;
                } else {
                    for (VirtualRegister vr : defRegs) {
                        if (liveNow.contains(vr) || vr.spillPlace != null) {
                            flag = true;
                            break;
                        }
                    }
                }
                if (!flag && isRemovable(irInstruction)) {
                    irInstruction.remove();
                }
                liveNow.removeAll(defRegs);
                liveNow.addAll(useRegs);
            }
        }
    }

    private boolean isRemovable(IRInstruction inst) {
        return !(inst instanceof Return || inst instanceof Leave || inst instanceof Call || inst instanceof Cdq
                || inst instanceof Push || inst instanceof Pop || inst instanceof Jump || inst instanceof CJump);
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

package Compiler.IR.Operand;

import Compiler.IR.IRVisitor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Memory extends Address {
    public Register base;
    public Register index;
    public int scale = 0;
    public Constant displacement;

    public Memory() {
    }

    //    [ number ]
    public Memory(Constant displacement) {
        this.displacement = displacement;
    }

    //    [ reg ]
    public Memory(Register base) {
        this.base = base;
    }

    //    [ reg + reg*scale ]  scale is 1, 2, 4, or 8 only
    public Memory(Register base, Register index, int scale) {
        this.base = base;
        this.index = index;
        this.scale = scale;
    }

    //    [ reg + number ]
    public Memory(Register base, Constant displacement) {
        this.base = base;
        this.displacement = displacement;
    }

    //    [ reg + reg*scale + number ]
    public Memory(Register base, Register index, int scale, Constant displacement) {
        this.base = base;
        this.index = index;
        this.scale = scale;
        this.displacement = displacement;
    }

    public Memory(Register index, int scale, Constant displacement) {
        this.index = index;
        this.scale = scale;
        this.displacement = displacement;
    }

    public List<Register> usedRegs() {
        List<Register> regs = new LinkedList<>();
        if (base != null) {
            regs.add(base);
        }
        if (index != null) {
            regs.add(index);
        }
        return regs;
    }

    public void renameRegs(Map<Register, Register> map) {
        if (base != null && map.containsKey(base)) {
            base = map.get(base);
        }
        if (index != null && map.containsKey(index)) {
            index = map.get(index);
        }
    }

    public Memory copy() {
        if (this instanceof StackSlot) {
            return this;
        } else {
            return new Memory(base, index, scale, displacement);
        }
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}

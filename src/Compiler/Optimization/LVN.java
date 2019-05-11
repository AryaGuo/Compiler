package Compiler.Optimization;

import Compiler.IR.BasicBlock;
import Compiler.IR.Function;
import Compiler.IR.IRProgram;
import Compiler.IR.Instruction.BinaryInst;
import Compiler.IR.Instruction.IRInstruction;
import Compiler.IR.Instruction.Move;
import Compiler.IR.Instruction.UnaryInst;
import Compiler.IR.Operand.Address;
import Compiler.IR.Operand.Operand;

import java.util.HashMap;
import java.util.LinkedList;

public class LVN {
    public class Item {
        public int x;
        public int y;
        public Operand operand;
        public BinaryInst.Op bop;
        public UnaryInst.Op uop;

        public Item(Operand operand) {
            this.operand = operand;
        }

        public Item(int x, int y, BinaryInst.Op bop, UnaryInst.Op uop) {
            this.x = x;
            this.y = y;
            this.bop = bop;
            this.uop = uop;
        }
    }

    private IRProgram irProgram;
    private int cnt = 0;
    private HashMap<Item, Integer> IDMap;
    private HashMap<Integer, LinkedList<Operand>> IDtoOperand;

    public LVN(IRProgram irProgram) {
        this.irProgram = irProgram;
        this.IDMap = new HashMap<>();
        this.IDtoOperand = new HashMap<>();
    }

    public void run() {
        for (Function function : irProgram.functionList) {
            for (BasicBlock basicBlock : function.basicBlockList) {
                process(basicBlock);
            }
        }
    }

    private void process(BasicBlock basicBlock) {
        for (IRInstruction inst = basicBlock.head; inst != null; inst = inst.nxt) {
            int now;
            Address def;
            if (inst instanceof BinaryInst) {
                def = ((BinaryInst) inst).dest;
                int src = getID(((BinaryInst) inst).src);
                int dest = getID(def);
                now = getID(Integer.min(src, dest), Integer.max(src, dest), ((BinaryInst) inst).op, null);

            } else if (inst instanceof UnaryInst) {
                def = ((UnaryInst) inst).dest;
                int dest = getID(def);
                now = getID(dest, 0, null, ((UnaryInst) inst).op);
            } else {
                continue;
            }
            if (now != cnt) {
                inst.prepend(new Move(inst.bb, def, IDtoOperand.get(now).getFirst()));
                inst = inst.pre;
                inst.nxt.remove();
            }
            update(def, now);
        }
    }

    private void update(Operand operand, int now) {
        Item item = new Item(operand);
        if (IDMap.containsKey(item)) {
            int old = IDMap.get(item);
            LinkedList<Operand> list = IDtoOperand.get(old);
            if (list.size() == 1) {
                IDMap.remove(item);
                IDtoOperand.remove(old);
            } else {
                IDtoOperand.get(old).remove(operand);
            }
        }
        IDMap.put(item, now);
        if (IDtoOperand.containsKey(now)) {
            IDtoOperand.get(now).addLast(operand);
        } else {
            LinkedList<Operand> list = new LinkedList<>();
            list.addLast(operand);
            IDtoOperand.put(cnt, list);
        }
    }

    private int getID(Operand operand) {
        Item item = new Item(operand);
        if (IDMap.containsKey(item)) {
            int id = IDMap.get(item);
            IDtoOperand.get(id).addLast(operand);
            return id;
        }
        IDMap.put(item, ++cnt);
        LinkedList<Operand> list = new LinkedList<>();
        list.addLast(operand);
        IDtoOperand.put(cnt, list);
        return cnt;
    }

    private int getID(int x, int y, BinaryInst.Op bop, UnaryInst.Op uop) {
        Item item = new Item(x, y, bop, uop);
        if (IDMap.containsKey(item)) {
            return IDMap.get(item);
        }
        IDMap.put(item, ++cnt);
        return cnt;
    }
}

package Compiler.IR;

import Compiler.AST.ASTProgram;
import Compiler.AST.ASTVisitor;
import Compiler.AST.Declaration.ClassDeclaration;
import Compiler.AST.Declaration.Declaration;
import Compiler.AST.Declaration.FunctionDeclaration;
import Compiler.AST.Declaration.VariableDeclaration;
import Compiler.AST.Expression.*;
import Compiler.AST.Statement.*;
import Compiler.AST.TypeNode.ArrayTypeNode;
import Compiler.AST.TypeNode.ClassTypeNode;
import Compiler.AST.TypeNode.PrimitiveTypeNode;
import Compiler.AST.TypeNode.TypeNode;
import Compiler.Config;
import Compiler.IR.Instruction.*;
import Compiler.IR.Operand.*;
import Compiler.Symbol.*;

import java.util.*;

import static Compiler.IR.RegisterSet.vrax;

public class IRBuilder implements ASTVisitor {

    private IRProgram irProgram;

    private GlobalSymbolTable globalSymbolTable;
    private BasicBlock curBB;
    private Function curFunction;
    private ClassSymbol curClassSymbol;
    private VirtualRegister curThisPointer;
    private boolean isParameter;

    private Map<String, Function> functionMap;
    private Map<Expression, BasicBlock> trueList;
    private Map<Expression, BasicBlock> falseList;
    /*
        not using fall-through

        Backpatching [Dragon Book Chapter 6.7]
        for a pair (expr, bb) in true/false List:
            after translation of expr, calculate the value of expr
            if true: goto bb in trueList
            else: goto bb in falseList
    */

    private Stack<BasicBlock> continueList;
    private Stack<BasicBlock> breakList;

    private Map<Expression, Operand> exprToOperand;

    private static Function global_print;
    private static Function global_println;
    private static Function global_getString;
    private static Function global_getInt;
    private static Function global_toString;

    private static Function string_length;
    private static Function string_substring;
    private static Function string_parseInt;
    private static Function string_ord;
    private static Function stringConcat;
    private static Function stringCompare;
    private static Function external_malloc;
    private static Function init;


    public IRBuilder(GlobalSymbolTable globalSymbolTable) {
        this.globalSymbolTable = globalSymbolTable;
        irProgram = new IRProgram();
        functionMap = new HashMap<>();
        trueList = new HashMap<>();
        falseList = new HashMap<>();
        continueList = new Stack<>();
        breakList = new Stack<>();
        exprToOperand = new HashMap<>();
        ir_init();
    }

    private void ir_init() {
        global_print = new Function("print", false, Function.Type.Library);
        functionMap.put("print", global_print);
        global_println = new Function("println", false, Function.Type.Library);
        functionMap.put("println", global_println);
        global_getString = new Function("getString", true, Function.Type.Library);
        functionMap.put("getString", global_getString);
        global_getInt = new Function("getInt", true, Function.Type.Library);
        functionMap.put("getInt", global_getInt);
        global_toString = new Function("toString", true, Function.Type.Library);
        functionMap.put("toString", global_toString);
        string_length = new Function("string_length", true, Function.Type.Library);
        functionMap.put("string.length", string_length);
        string_substring = new Function("string_substring", true, Function.Type.Library);
        functionMap.put("string.substring", string_substring);
        string_parseInt = new Function("string_parseInt", true, Function.Type.Library);
        functionMap.put("string.parseInt", string_parseInt);
        string_ord = new Function("string_ord", true, Function.Type.Library);
        functionMap.put("string.ord", string_ord);

        stringConcat = new Function("stringConcat", true, Function.Type.Library);
        stringCompare = new Function("stringCompare", true, Function.Type.Library);
        external_malloc = new Function("malloc", true, Function.Type.External);

        init = new Function("init", false, Function.Type.Library);
    }

    public IRProgram getIrProgram() {
        return irProgram;
    }

    private void build_init(ASTProgram node) {
        curFunction = init;
        curBB = new BasicBlock("enter " + curFunction.name, curFunction);
        curFunction.enterBB = curBB;
        for (VariableDeclaration variableDeclaration : node.variableList) {
            if (variableDeclaration.init != null) {
                assign(variableDeclaration.variableSymbol.virtualRegister, variableDeclaration.init);
            }
        }
        curBB.append(new Call(curBB, vrax, functionMap.get("main")));
        curBB.append(new Return(curBB));
        curFunction.leaveBB = curBB;
        irProgram.functionList.add(curFunction);
        curFunction.finishBuild();
    }

    @Override
    public void visit(ASTProgram node) {
        /*
            for global vars: add to data section
            for classes: take out functions
            for functions: visit functions
         */
        for (VariableDeclaration variableDeclaration : node.variableList) {
            StaticData data = new StaticData(variableDeclaration.name, Config.REGISTER_WIDTH);
            VirtualRegister vr = new VirtualRegister(variableDeclaration.name);
            vr.spillPlace = new Memory(data);
            irProgram.dataList.add(data);
            variableDeclaration.variableSymbol.virtualRegister = vr;
        }

        LinkedList<FunctionDeclaration> functionDeclarations = new LinkedList<>(node.functionList);
        for (ClassDeclaration classDeclaration : node.classList) {
            functionDeclarations.addAll(classDeclaration.functionList);
            if (classDeclaration.constructor != null) {
                functionDeclarations.add(classDeclaration.constructor);
            }
        }

        for (FunctionDeclaration functionDeclaration : functionDeclarations) {
            functionMap.put(functionDeclaration.functionSymbol.name, new Function(functionDeclaration.functionSymbol.name,
                    functionDeclaration.returnType != null, Function.Type.UserDefined));
        }

        for (FunctionDeclaration functionDeclaration : node.functionList) {
            functionDeclaration.accept(this);
        }
        for (ClassDeclaration classDeclaration : node.classList) {
            classDeclaration.accept(this);
        }

        for (Function function : functionMap.values()) {
            if (function.type == Function.Type.UserDefined) {
                function.finishBuild();
            }
        }

        build_init(node);
    }

    @Override
    public void visit(Declaration node) {
        assert false;
    }

    @Override
    public void visit(ClassDeclaration node) {
        curClassSymbol = node.classSymbol;
        if (node.constructor != null) {
            node.constructor.accept(this);
        }
        for (FunctionDeclaration functionDeclaration : node.functionList) {
            functionDeclaration.accept(this);
        }
        curClassSymbol = null;
    }

    @Override
    public void visit(FunctionDeclaration node) {
        curFunction = functionMap.get(node.functionSymbol.name);
        curBB = new BasicBlock("enter " + curFunction.name, curFunction);
        curFunction.enterBB = curBB;

        if (curClassSymbol != null) {
            VirtualRegister vthis = new VirtualRegister("");
            curFunction.parameterList.add(vthis);
            curThisPointer = vthis;
        }

        isParameter = true;
        for (VariableDeclaration variableDeclaration : node.parameterList) {
            variableDeclaration.accept(this);
        }
        isParameter = false;

        //store parameters (make place for regs)
        for (int i = 0; i < curFunction.parameterList.size(); i++) {
            if (i < 6) {
                curBB.append(new Move(curBB, curFunction.parameterList.get(i), RegisterSet.vargs.get(i)));
            } else {
                curBB.append(new Move(curBB, curFunction.parameterList.get(i), curFunction.parameterList.get(i).spillPlace));
            }
        }

        for (VariableSymbol variableSymbol : node.functionSymbol.usedGlobals) {
            curBB.append(new Move(curBB, variableSymbol.virtualRegister, variableSymbol.virtualRegister.spillPlace));
        }

        for (Statement statement : node.body) {
            statement.accept(this);
        }
        if (!(curBB.tail instanceof Return)) {
            if (isVoid(node.functionSymbol.returnType)) {
                curBB.append(new Return(curBB));
            } else {
                curBB.append(new Move(curBB, vrax, new Immediate(0)));
                curBB.append(new Return(curBB));
            }
        }

        List<Return> returnList = new LinkedList<>();
        for (BasicBlock basicBlock : curFunction.basicBlockList) {
            for (IRInstruction instruction = basicBlock.head; instruction != null; instruction = instruction.nxt) {
                if (instruction instanceof Return) {
                    returnList.add((Return) instruction);
                }
            }
        }

        BasicBlock leaveBB = new BasicBlock("leaveBB", curFunction);
        for (Return ret : returnList) {
            ret.prepend(new Jump(ret.bb, leaveBB));
            ret.remove();
        }
        leaveBB.append(new Return(leaveBB));
        curFunction.leaveBB = leaveBB;

        IRInstruction ret = curFunction.leaveBB.tail;
        for (VariableSymbol variableSymbol : node.functionSymbol.usedGlobals) {
            ret.prepend(new Move(ret.bb, variableSymbol.virtualRegister.spillPlace, variableSymbol.virtualRegister));
        }

        functionMap.put(node.name, curFunction);
        irProgram.functionList.add(curFunction);
    }

    @Override
    public void visit(VariableDeclaration node) {
        VirtualRegister vr = new VirtualRegister(node.name);
        node.variableSymbol.virtualRegister = vr;
        if (isParameter) {
            if (curFunction.parameterList.size() >= 6) {
                vr.spillPlace = new StackSlot(vr.hint);
            }
            curFunction.parameterList.add(vr);
        }
        if (node.init != null) {
            assign(vr, node.init);
        }
    }

    @Override
    public void visit(TypeNode node) {
        assert false;
    }

    @Override
    public void visit(ClassTypeNode node) {
        assert false;
    }

    @Override
    public void visit(ArrayTypeNode node) {
        assert false;
    }

    @Override
    public void visit(PrimitiveTypeNode node) {
        assert false;
    }

    @Override
    public void visit(Statement node) {
        assert false;
    }

    @Override
    public void visit(BlockStatement node) {
        for (Statement statement : node.statementList) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(BreakStatement node) {
        curBB.append(new Jump(curBB, breakList.peek()));
    }

    @Override
    public void visit(ContinueStatement node) {
        curBB.append(new Jump(curBB, continueList.peek()));
    }

    @Override
    public void visit(EmptyStatement node) {
    }

    @Override
    public void visit(ExprStatement node) {
        if (isBoolean(node.expression.type)) {
            createBBForBool(node.expression);
        } else {
            node.expression.accept(this);
        }
    }

    @Override
    public void visit(ForStatement node) {
        BasicBlock bodyBB = new BasicBlock("forBody", curFunction);
        BasicBlock stepBB = new BasicBlock("forStep", curFunction);
        BasicBlock condBB = new BasicBlock("forCond", curFunction);
        BasicBlock doneBB = new BasicBlock("forDone", curFunction);

        if (node.init != null) {
            node.init.accept(this);
        }
        curBB.append(new Jump(curBB, condBB));

        continueList.push(stepBB);
        breakList.push(doneBB);
        curBB = bodyBB;
        node.body.accept(this);
        continueList.pop();
        breakList.pop();

        if (node.step != null) {
            curBB = stepBB;
            node.step.accept(this);
        }

        if (node.condition != null) {
            curBB = condBB;
            trueList.put(node.condition, bodyBB);
            falseList.put(node.condition, doneBB);
            node.condition.accept(this);
        }

        curBB = doneBB;
    }

    @Override
    public void visit(IfStatement node) {
        BasicBlock thenBB = new BasicBlock("ifThen", curFunction);
        BasicBlock DoneBB = new BasicBlock("ifDone", curFunction);
        BasicBlock elseBB = node.falseStatement != null ? new BasicBlock("ifElse", curFunction) : DoneBB;

        trueList.put(node.condition, thenBB);
        falseList.put(node.condition, elseBB);
        node.condition.accept(this);

        curBB = thenBB;
        node.trueStatement.accept(this);
        curBB.append(new Jump(curBB, DoneBB));

        if (node.falseStatement != null) {
            curBB = elseBB;
            node.falseStatement.accept(this);
            curBB.append(new Jump(curBB, DoneBB));
        }

        curBB = DoneBB;
    }

    @Override
    public void visit(ReturnStatement node) {
        if (node.expression != null) {
            assign(vrax, node.expression);
        }
        curBB.append(new Return(curBB));
    }

    @Override
    public void visit(VarDeclStatement node) {
        for (VariableDeclaration variableDeclaration : node.declaration) {
            variableDeclaration.accept(this);
        }
    }

    @Override
    public void visit(WhileStatement node) {
        BasicBlock condBB = new BasicBlock("whileCond", curFunction);
        BasicBlock bodyBB = new BasicBlock("whileBody", curFunction);
        BasicBlock doneBB = new BasicBlock("whileDone", curFunction);

        curBB.append(new Jump(curBB, condBB));

        continueList.push(condBB);
        breakList.push(doneBB);
        curBB = bodyBB;
        node.body.accept(this);
        continueList.pop();
        breakList.pop();

        curBB = condBB;
        trueList.put(node.condition, bodyBB);
        falseList.put(node.condition, doneBB);
        node.condition.accept(this);

        curBB = doneBB;
    }

    @Override
    public void visit(Expression node) {
        assert false;
    }

    @Override
    public void visit(ArrayExpression node) {
        node.array.accept(this);
        Operand base = exprToOperand.get(node.array);
        node.index.accept(this);
        Operand index = exprToOperand.get(node.index);

        VirtualRegister vr;
        if (base instanceof Register) {
            vr = (VirtualRegister) base;
        } else {
            vr = new VirtualRegister("");
            curBB.append(new Move(curBB, vr, base));
        }

        Memory memory = null;
        if (index instanceof Immediate) {
            memory = new Memory(vr, new Immediate(((Immediate) index).value * Config.REGISTER_WIDTH + Config.REGISTER_WIDTH));
        } else if (index instanceof Register) {
            memory = new Memory(vr, (Register) index, Config.REGISTER_WIDTH, new Immediate(Config.REGISTER_WIDTH));
        } else if (index instanceof Memory) {
            VirtualRegister vindex = new VirtualRegister("");
            curBB.append(new Move(curBB, vindex, index));
            memory = new Memory(vr, vindex, Config.REGISTER_WIDTH, new Immediate(Config.REGISTER_WIDTH));
        }
        if (trueList.containsKey(node)) {
            curBB.append(new CJump(curBB, CJump.Op.NE, trueList.get(node), falseList.get(node), memory, new Immediate(0)));
        } else {
            exprToOperand.put(node, memory);
        }
//        the first 8 bytes are used to store size of the array (array size < 2^32)
    }

    @Override
    public void visit(AssignExpression node) {
        node.lhs.accept(this);
        Operand left = exprToOperand.get(node.lhs);
        assert left instanceof Address;
        assign((Address) left, node.rhs);
    }

    @Override
    public void visit(BinaryExpression node) {
        if (isArithmeticOp(node.op)) {
            if (isString(node.lhs.type) && node.op.equals("+")) {
                exprToOperand.put(node, doStringConcat(node.lhs, node.rhs));
            } else {
                exprToOperand.put(node, doBinaryArithmetic(node.lhs, node.rhs, node.op));
            }
        } else if (isRelationalOp(node.op)) {
            doRelationalBinary(node.lhs, node.rhs, node.op, trueList.get(node), falseList.get(node));
        } else if (isLogicalOp(node.op)) {
            doLogicalBinary(node.lhs, node.rhs, node.op, trueList.get(node), falseList.get(node));
        }
    }

    @Override
    public void visit(FuncCallExpression node) {
        LinkedList<Operand> args = new LinkedList<>();
        if (!node.functionSymbol.isGlobal) {
            args.add(curThisPointer);
        }
        for (Expression parameter : node.parameterList) {
            if (isBoolean(parameter.type)) {
                createBBForBool(parameter);
            } else {
                parameter.accept(this);
            }
            args.add(exprToOperand.get(parameter));
        }
        curBB.append(new Call(curBB, vrax, functionMap.get(node.functionSymbol.name), args));

        if (node.functionSymbol.returnType != null) {
            VirtualRegister ret = new VirtualRegister("");
            curBB.append(new Move(curBB, ret, vrax));
            if (trueList.containsKey(node)) {
                curBB.append(new CJump(curBB, CJump.Op.NE, trueList.get(node), falseList.get(node), ret, new Immediate(0)));
            } else {
                exprToOperand.put(node, ret);
            }
        }
    }


    @Override
    public void visit(Identifier node) {
        Operand operand;
        if (node.name.equals("this")) {
            operand = curThisPointer;
        } else if (node.symbol.isClassField) {
            int offset = curClassSymbol.symbolTable.getOffset(node.name);
            operand = new Memory(curThisPointer, new Immediate(offset));
        } else {
            operand = node.symbol.virtualRegister;
        }
        if (node.symbol.isGlobal) {
            curFunction.usedGlobalVariables.add(node.symbol);
        }
        if (trueList.containsKey(node)) {
            curBB.append(new CJump(curBB, CJump.Op.NE, trueList.get(node), falseList.get(node), operand, new Immediate(0)));
        } else {
            exprToOperand.put(node, operand);
        }
    }

    @Override
    public void visit(LiteralExpression node) {
        Operand operand = null;
        switch (node.typeName) {
            case "int":
                operand = new Immediate(Integer.valueOf(node.value));
                break;
            case "bool":
                operand = new Immediate(node.value.equals("true") ? 1 : 0);
                curBB.append(new Jump(curBB, node.value.equals("true") ? trueList.get(node) : falseList.get(node)));
                break;
            case "null":
                operand = new Immediate(0);
                break;
            case "string":
                StaticData data = new StaticData("static_string", node.value);
                operand = data;
                irProgram.dataList.add(data);
                break;
            default:
                assert false;
        }
        exprToOperand.put(node, operand);
    }

    private void createBBForBool(Expression expr) {
        BasicBlock trueBB = new BasicBlock("trueBB", curFunction);
        BasicBlock falseBB = new BasicBlock("falseBB", curFunction);
        BasicBlock afterBB = new BasicBlock("afterBB", curFunction);
        trueList.put(expr, trueBB);
        falseList.put(expr, falseBB);
        expr.accept(this);
        trueBB.append(new Jump(trueBB, afterBB));
        falseBB.append(new Jump(falseBB, afterBB));
        curBB = afterBB;
    }

    public boolean failed = false;

    @Override
    public void visit(MemberExpression node) {
        if (isNull(node.lhs.type)) {
            failed = true;
            return;
        }
        node.lhs.accept(this);
        if (failed) {
            return;
        }
        VirtualRegister base = new VirtualRegister("");
        curBB.append(new Move(curBB, base, exprToOperand.get(node.lhs)));

        if (node.lhs.type instanceof ArrayType) {
            exprToOperand.put(node, new Memory(base));  //array.size
        } else {
            Operand operand = null;
            assert node.lhs.type instanceof ClassSymbol;
            if (node.identifier != null) {
                int offset = ((ClassSymbol) node.lhs.type).symbolTable.getOffset(node.identifier.name);
                operand = new Memory(base, new Immediate(offset));
            } else {
                LinkedList<Operand> args = new LinkedList<>();
                args.add(base);
                for (Expression parameter : node.functionCall.parameterList) {
                    parameter.accept(this);
                    args.add(exprToOperand.get(parameter));
                }
                curBB.append(new Call(curBB, vrax, functionMap.get(node.functionCall.functionSymbol.name), args));
                if (node.functionCall.functionSymbol.returnType != null) {
                    VirtualRegister ret = new VirtualRegister("");
                    curBB.append(new Move(curBB, ret, vrax));
                    operand = ret;
                }
            }
            if (trueList.containsKey(node)) {
                curBB.append(new CJump(curBB, CJump.Op.NE, trueList.get(node), falseList.get(node), operand, new Immediate(0)));
            } else {
                exprToOperand.put(node, operand);
            }
        }
    }

    @Override
    public void visit(NewExpression node) {
        List<Operand> dims = new LinkedList<>();
        for (Expression expression : node.dimExpr) {
            expression.accept(this);
            dims.add(exprToOperand.get(expression));
        }

        Operand pointer = null;
        if (node.type instanceof ArrayType) {
            // eg. new int[3][4]; new A[]
            pointer = allocateMemory(dims, Config.REGISTER_WIDTH, null, false);
        } else if (node.type instanceof ClassSymbol) {
            // eg. new A()
            Function constructor = null;
            ClassSymbol classSymbol = (ClassSymbol) node.type;
            if (classSymbol.symbolTable.containFunction(classSymbol.name)) {
                constructor = functionMap.get(classSymbol.symbolTable.getFunction(classSymbol.name).name);
            }
            int bytes;
            if (!isString(classSymbol)) {
                bytes = classSymbol.getBytes();
            } else {
                bytes = Config.REGISTER_WIDTH * 2;
                // first byte: length of string
                // second byte: pointer to string
            }
            pointer = allocateMemory(dims, bytes, constructor, true);
        }
        exprToOperand.put(node, pointer);
    }

    private Operand allocateMemory(List<Operand> dims, int baseBytes, Function constructor, boolean isClass) {
        VirtualRegister ret = new VirtualRegister("");
        if (dims.size() > 0) {
            // array
            VirtualRegister size = new VirtualRegister("");
            VirtualRegister bytes = new VirtualRegister("");
            curBB.append(new Move(curBB, size, dims.get(0)));
            // calculate size * [baseBytes / sizeof(void*)] + RW
            curBB.append(new Lea(curBB, bytes, new Memory(size, Config.REGISTER_WIDTH, new Immediate(Config.REGISTER_WIDTH))));
            // allocate dims[0] pointers
            curBB.append(new Call(curBB, vrax, external_malloc, bytes));
            curBB.append(new Move(curBB, ret, vrax));
            // save the size of array
            curBB.append(new Move(curBB, new Memory(ret), size));

            /*
                while(size > 0) {
                    M[ret + size * 8] = pointer;
                    size--;
                }

                        jump condBB
                bodyBB: allocateMemory(nxt level)
                        dec size
                condBB: cjump(size > 0 ? bodyBB : doneBB)
                doneBB:
             */
            BasicBlock condBB = new BasicBlock("allocCond", curFunction);
            BasicBlock bodyBB = new BasicBlock("allocBody", curFunction);
            BasicBlock doneBB = new BasicBlock("allocDone", curFunction);

            curBB.append(new Jump(curBB, condBB));
            curBB = bodyBB;

            dims.remove(0);
            Operand pointer = allocateMemory(dims, baseBytes, constructor, isClass);
            curBB.append(new Move(curBB, new Memory(ret, size, Config.REGISTER_WIDTH), pointer));
            curBB.append(new UnaryInst(curBB, UnaryInst.Op.DEC, size));

            curBB = condBB;
            curBB.append(new CJump(curBB, CJump.Op.G, bodyBB, doneBB, size, new Immediate(0)));

            curBB = doneBB;
        } else {
            if (!isClass) {
                return new Immediate(0);
            }
            curBB.append(new Call(curBB, vrax, external_malloc, new Immediate(baseBytes)));
            curBB.append(new Move(curBB, ret, vrax));
            if (constructor != null) {
                curBB.append(new Call(curBB, vrax, constructor, ret));
            } else {
                if (baseBytes == Config.REGISTER_WIDTH) {
                    curBB.append(new Move(curBB, new Memory(ret), new Immediate(0)));
                } else if (baseBytes == Config.REGISTER_WIDTH * 2) {
                    curBB.append(new BinaryInst(curBB, BinaryInst.Op.ADD, ret, new Immediate(Config.REGISTER_WIDTH)));
                    curBB.append(new Move(curBB, new Memory(ret), new Immediate(0)));
                    curBB.append(new BinaryInst(curBB, BinaryInst.Op.SUB, ret, new Immediate(Config.REGISTER_WIDTH)));
                }
            }
        }
        return ret;
    }

    @Override
    public void visit(UnaryExpression node) {
        if (node.op.equals("!")) {
            trueList.put(node.expression, falseList.get(node));
            falseList.put(node.expression, trueList.get(node));
            node.expression.accept(this);
        } else {
            exprToOperand.put(node, doUnaryArithmetic(node.expression, node.op));
        }
    }

    private boolean isBoolean(Type type) {
        return type.match(globalSymbolTable.boolType);
    }

    private boolean isString(Type type) {
        return type.match(globalSymbolTable.stringType);
    }

    private boolean isVoid(Type type) {
        return type.match(globalSymbolTable.voidType);
    }

    private boolean isNull(Type type) {
        return type instanceof ClassSymbol && ((ClassSymbol) type).name.equals("null");
    }

    private boolean isRelationalOp(String op) {
        return op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=") || op.equals("==") || op.equals("!=");
    }

    private boolean isArithmeticOp(String op) {
        return op.equals("*") || op.equals("/") || op.equals("%") || op.equals("+") || op.equals("-") ||
                op.equals("<<") || op.equals(">>") || op.equals("&") || op.equals("^") || op.equals("|");
    }

    private boolean isLogicalOp(String op) {
        return op.equals("&&") || op.equals("||");
    }

    private void assign(Address dest, Expression expr) {
        if (isBoolean(expr.type)) {
            boolAssign(dest, expr);
        } else {
            expr.accept(this);
            Operand result = exprToOperand.get(expr);
            if (dest != result) {
                curBB.append(new Move(curBB, dest, result));
            }
            exprToOperand.put(expr, dest);
        }
    }

    private void boolAssign(Address dest, Expression expr) {
        BasicBlock trueBB = new BasicBlock("trueBB", curFunction);
        BasicBlock falseBB = new BasicBlock("falseBB", curFunction);
        BasicBlock afterBB = new BasicBlock("afterBB", curFunction);
        trueList.put(expr, trueBB);
        falseList.put(expr, falseBB);
        expr.accept(this);
        trueBB.append(new Move(trueBB, dest, new Immediate(1)));
        trueBB.append(new Jump(trueBB, afterBB));
        falseBB.append(new Move(falseBB, dest, new Immediate(0)));
        falseBB.append(new Jump(falseBB, afterBB));
        curBB = afterBB;
    }

    private Operand doStringConcat(Expression lhs, Expression rhs) {
        lhs.accept(this);
        rhs.accept(this);
        Operand olhs = exprToOperand.get(lhs);
        Operand orhs = exprToOperand.get(rhs);
        VirtualRegister res = new VirtualRegister("");
        if (olhs instanceof Memory && !(olhs instanceof StackSlot)) {
            VirtualRegister vr = new VirtualRegister("");
            curBB.append(new Move(curBB, vr, olhs));
            olhs = vr;
        }
        if (orhs instanceof Memory && !(orhs instanceof StackSlot)) {
            VirtualRegister vr = new VirtualRegister("");
            curBB.append(new Move(curBB, vr, orhs));
            orhs = vr;
        }
        curBB.append(new Call(curBB, vrax, stringConcat, olhs, orhs));
        curBB.append(new Move(curBB, res, vrax));
        return res;
    }

    private Operand doUnaryArithmetic(Expression expression, String op) {
        Operand operand;
        expression.accept(this);
        operand = exprToOperand.get(expression);
        switch (op) {
            case "+":
                return operand;
            case "-":
            case "~":
                VirtualRegister vr = new VirtualRegister("");
                curBB.append(new Move(curBB, vr, operand));
                curBB.append(new UnaryInst(curBB, op.equals("-") ? UnaryInst.Op.NEG : UnaryInst.Op.NOT, vr));
                return vr;
            case "++x":
            case "--x":
                assert operand instanceof Address;
                curBB.append(new UnaryInst(curBB, op.equals("++x") ? UnaryInst.Op.INC : UnaryInst.Op.DEC, (Address) operand));
                return operand;
            case "x++":
            case "x--":
                assert operand instanceof Address;
                VirtualRegister old = new VirtualRegister("");
                curBB.append(new Move(curBB, old, operand));
                curBB.append(new UnaryInst(curBB, op.equals("x++") ? UnaryInst.Op.INC : UnaryInst.Op.DEC, (Address) operand));
                return old;
            default:
                assert false;
                return null;
        }
    }

    private Operand doBinaryArithmetic(Expression lhs, Expression rhs, String op) {
        BinaryInst.Op bop = null;
        switch (op) {
            case "*":
                bop = BinaryInst.Op.MUL;
                break;
            case "/":
                bop = BinaryInst.Op.DIV;
                break;
            case "%":
                bop = BinaryInst.Op.MOD;
                break;
            case "+":
                bop = BinaryInst.Op.ADD;
                break;
            case "-":
                bop = BinaryInst.Op.SUB;
                break;
            case "<<":
                bop = BinaryInst.Op.SHL;
                break;
            case ">>":
                bop = BinaryInst.Op.SHR;
                break;
            case "&":
                bop = BinaryInst.Op.AND;
                break;
            case "^":
                bop = BinaryInst.Op.XOR;
                break;
            case "|":
                bop = BinaryInst.Op.OR;
                break;
        }
        lhs.accept(this);
        rhs.accept(this);
        Operand olhs = exprToOperand.get(lhs);
        Operand orhs = exprToOperand.get(rhs);
        VirtualRegister vr = new VirtualRegister("");
        curBB.append(new Move(curBB, vr, olhs));
        curBB.append(new BinaryInst(curBB, bop, vr, orhs));
        return vr;
    }

    private void doRelationalBinary(Expression lhs, Expression rhs, String op, BasicBlock trueBB, BasicBlock falseBB) {
        CJump.Op cop = null;
        switch (op) {
            case ">":
                cop = CJump.Op.G;
                break;
            case "<":
                cop = CJump.Op.L;
                break;
            case ">=":
                cop = CJump.Op.GE;
                break;
            case "<=":
                cop = CJump.Op.LE;
                break;
            case "==":
                cop = CJump.Op.E;
                break;
            case "!=":
                cop = CJump.Op.NE;
                break;
        }
        lhs.accept(this);
        rhs.accept(this);
        Operand olhs = exprToOperand.get(lhs);
        Operand orhs = exprToOperand.get(rhs);
        if (isString(lhs.type)) {
            VirtualRegister vr = new VirtualRegister("");
            curBB.append(new Call(curBB, vrax, stringCompare, olhs, orhs));
            curBB.append(new Move(curBB, vr, vrax));
            curBB.append(new CJump(curBB, cop, trueBB, falseBB, vr, new Immediate(0)));
        } else {
            if (olhs instanceof Memory && orhs instanceof Memory) {
                VirtualRegister vr = new VirtualRegister("");
                curBB.append(new Move(curBB, vr, olhs));
                olhs = vr;
            }
            curBB.append(new CJump(curBB, cop, trueBB, falseBB, olhs, orhs));
        }
    }

    private void doLogicalBinary(Expression lhs, Expression rhs, String op, BasicBlock trueBB, BasicBlock falseBB) {
        BasicBlock secondBB = new BasicBlock("rhs of logical expr", curFunction);
        if (op.equals("&&")) {
            trueList.put(lhs, secondBB);
            falseList.put(lhs, falseBB);
        } else {
            trueList.put(lhs, trueBB);
            falseList.put(lhs, secondBB);
        }
        lhs.accept(this);
        curBB = secondBB;
        trueList.put(rhs, trueBB);
        falseList.put(rhs, falseBB);
        rhs.accept(this);
    }
}

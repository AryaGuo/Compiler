package Compiler.Symbol;

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
import Compiler.Utility.ErrorRecorder;

public class SemanticChecker implements ASTVisitor {

    private ErrorRecorder errorRecorder;
    private GlobalSymbolTable global;
    private SymbolTable currentScope;
    private FunctionSymbol currentFunction;
    private int loopCount;

    private PrimitiveSymbol intType;
    private PrimitiveSymbol boolType;
    private PrimitiveSymbol voidType;
    private ClassSymbol stringType;
    private ClassSymbol nullType;

    public SemanticChecker(ErrorRecorder errorRecorder, GlobalSymbolTable global) {
        this.errorRecorder = errorRecorder;
        this.global = global;
        this.currentScope = global;
        intType = global.intType;
        boolType = global.boolType;
        voidType = global.voidType;
        stringType = global.stringType;
        nullType = global.nullType;
    }

    private Type getArrayType(Type baseType, int dimension) {
        ArrayType arrayType = new ArrayType(baseType);
        for (int i = 1; i < dimension; ++i) {
            arrayType = new ArrayType(arrayType);
        }
        return arrayType;
    }

    private Type resolveType(TypeNode type) {
        if (type instanceof PrimitiveTypeNode) {
            return global.getPrimitive(((PrimitiveTypeNode) type).name);
        } else if (type instanceof ClassTypeNode) {
            String name = ((ClassTypeNode) type).name;
            if (global.containClass(name)) {
                return global.getClass(name);
            } else {
                errorRecorder.addRecord(type.location, "unknown typeNode name '" + name + "'");
                return null;
            }
        } else if (type instanceof ArrayTypeNode) {
            Type baseType = resolveType(((ArrayTypeNode) type).baseType);
            return getArrayType(baseType, ((ArrayTypeNode) type).dimension);
        } else return null;
    }

    private VariableSymbol resolveVariable(SymbolTable scope, String name) {
        while (scope != null) {
            if (scope.containVariable(name)) {
                return scope.getVariable(name);
            } else {
                scope = scope.parent;
            }
        }
        return null;
    }

    private FunctionSymbol resolveFunction(SymbolTable scope, String name) {
        while (scope != null) {
            if (scope.containFunction(name)) {
                return scope.getFunction(name);
            } else {
                scope = scope.parent;
            }
        }
        return null;
    }

    private void defineVariable(VariableDeclaration variableDeclaration) {
        VariableSymbol variableSymbol = new VariableSymbol(variableDeclaration.name);
        variableSymbol.location = variableDeclaration.location;
        variableSymbol.isGlobal = (currentScope instanceof GlobalSymbolTable);
        variableSymbol.type = resolveType(variableDeclaration.type);

        if (currentScope.containVariable(variableSymbol.name)) {
            errorRecorder.addRecord(variableSymbol.location, "redefinition of variable '" + variableSymbol.name + "'");
        } else if (currentScope.containFunction(variableSymbol.name)) {
            errorRecorder.addRecord(variableSymbol.location, "redefinition of '" + variableSymbol.name + "'; previously as a function");
        } else if (variableSymbol.isGlobal && global.containClass(variableSymbol.name)) {
            errorRecorder.addRecord(variableSymbol.location, "redefinition of '" + variableSymbol.name + "'; previously as a class");
        } else {
            currentScope.addVariable(variableSymbol.name, variableSymbol);
            variableDeclaration.variableSymbol = variableSymbol;
        }
    }

    private void enterScope(SymbolTable symbolTable) {
        currentScope = symbolTable;
    }

    private void exitScope() {
        currentScope = currentScope.parent;
    }

    private boolean equalityOp(String op) {
        return op.equals("==") || op.equals("!=");
    }

    private boolean compareOp(String op) {
        return op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=");
    }

    private boolean arithmeticOp(String op) {
        return op.equals("*") || op.equals("/") || op.equals("%") || op.equals("+") || op.equals("-") ||
                op.equals("<<") || op.equals(">>") || op.equals("&") || op.equals("^") || op.equals("|");
    }

    private boolean boolOp(String op) {
        return op.equals("&&") || op.equals("||");
    }

    //++x: isLeft
    private boolean selfIncPrefixOp(String op) {
        return op.equals("++x") || op.equals("--x");
    }

    //x++: notLeft
    private boolean selfIncSuffixOp(String op) {
        return op.equals("x++") || op.equals("x--");
    }

    private boolean unaryAriOp(String op) {
        return op.equals("+") || op.equals("-") || op.equals("~");
    }

    @Override
    public void visit(ASTProgram node) {
        node.declarationList.forEach(declaration -> declaration.accept(this));

        FunctionSymbol main = resolveFunction(currentScope, "main");
        if (main == null) {
            errorRecorder.addRecord(node.location, "'main' function not found");
        } else if (!main.returnType.match(intType)) {
            errorRecorder.addRecord(main.location, "'main' must return 'int'");
        } else if (main.parameterTypeList.size() != 0) {
            errorRecorder.addRecord(main.location, "'main' should not have any parameters");
        }
    }

    @Override
    public void visit(Declaration node) {
        assert false;
    }

    @Override
    public void visit(ClassDeclaration node) {
        enterScope(node.classSymbol.symbolTable);
//        System.out.println("class " + node.classSymbol.name);
        node.functionList.forEach(this::visit);
        if (node.constructor != null) {
            visit(node.constructor);
        }
        exitScope();
    }

    @Override
    public void visit(FunctionDeclaration node) {
        enterScope(node.functionSymbol.symbolTable);
//        System.out.println("function " + node.functionSymbol.name);
        currentFunction = node.functionSymbol;
        node.body.forEach(statement -> statement.accept(this));
        currentFunction = null;
        exitScope();
        node.functionSymbol.finish();
    }

    @Override
    public void visit(VariableDeclaration node) {
        if (node.init != null) {
            node.init.accept(this);
            if (node.init.type == null) return;
        }
        defineVariable(node);
        if (node.variableSymbol == null) return;
        if (node.init != null) {
            if (!node.variableSymbol.type.match(node.init.type)) {
                errorRecorder.addRecord(node.location, "can not initialize '" + node.variableSymbol.type.toString() + "' with '" + node.init.type.toString() + "'");
            }
        }
    }

    @Override
    public void visit(TypeNode node) {

    }

    @Override
    public void visit(ClassTypeNode node) {

    }

    @Override
    public void visit(ArrayTypeNode node) {

    }

    @Override
    public void visit(PrimitiveTypeNode node) {

    }

    @Override
    public void visit(Statement node) {
        assert false;
    }

    @Override
    public void visit(BlockStatement node) {
        enterScope(new SymbolTable(currentScope));
        node.statementList.forEach(statement -> statement.accept(this));
        exitScope();
    }

    @Override
    public void visit(BreakStatement node) {
        if (loopCount == 0) {
            errorRecorder.addRecord(node.location, "'break' statement not in loop statement");
        }
    }

    @Override
    public void visit(ContinueStatement node) {
        if (loopCount == 0) {
            errorRecorder.addRecord(node.location, "'continue' statement not in loop statement");
        }
    }

    @Override
    public void visit(EmptyStatement node) {

    }

    @Override
    public void visit(ExprStatement node) {
        node.expression.accept(this);
    }

    @Override
    public void visit(ForStatement node) {
        if (node.condition != null) {
            node.condition.accept(this);
            if (node.condition.type == null) {
                return;
            }
            if (!node.condition.type.match(boolType)) {
                errorRecorder.addRecord(node.location, "condition expression in a for-loop should be bool");
            }
        }
        if (node.init != null) {
            node.init.accept(this);
        }
        if (node.step != null) {
            node.step.accept(this);
        }
        if (node.body != null) {
            enterScope(new SymbolTable(currentScope));
            ++loopCount;
            node.body.accept(this);
            --loopCount;
            exitScope();
        }
    }

    @Override
    public void visit(IfStatement node) {
        if (node.condition == null) {
            errorRecorder.addRecord(node.location, "expected expression for a 'if' condition");
        } else {
            node.condition.accept(this);
            if (node.condition.type == null) return;
            if (!node.condition.type.match(boolType)) {
                errorRecorder.addRecord(node.location, "condition expression in a 'if' should be bool");
            }
        }
        if (node.trueStatement != null) {
            enterScope(new SymbolTable(currentScope));
            node.trueStatement.accept(this);
            exitScope();
        }
        if (node.falseStatement != null) {
            enterScope(new SymbolTable(currentScope));
            node.falseStatement.accept(this);
            exitScope();
        }
    }

    @Override
    public void visit(ReturnStatement node) {
        if (currentFunction == null) {
            errorRecorder.addRecord(node.location, "not in a function; can not return");
        } else {
            Type expect = currentFunction.returnType;
            Type returnType;
            if (node.expression == null) {
                returnType = voidType;
            } else {
                node.expression.accept(this);
                if (node.expression.type == null) return;
                returnType = node.expression.type;
            }
            if (expect.match(voidType) && !returnType.match(voidType)) {
                errorRecorder.addRecord(node.location, "void function '" + currentFunction.name + "' should not return a value");
            } else if (!expect.match(returnType)) {
                errorRecorder.addRecord(node.location, "incompatible return typeNode for function '" +
                        currentFunction.name + "'; " + "expected '" + expect.toString() + "', found '" + returnType.toString() + "'");
            }
        }
    }

    @Override
    public void visit(VarDeclStatement node) {
        node.declaration.forEach(this::visit);
    }

    @Override
    public void visit(WhileStatement node) {
        if (node.condition == null) {
            errorRecorder.addRecord(node.location, "expected expression for a while-loop condition");
        } else {
            node.condition.accept(this);
            if (node.condition.type == null) return;
            if (!node.condition.type.match(boolType)) {
                errorRecorder.addRecord(node.location, "condition expression in a while-loop should be bool");
            }
        }
        if (node.body != null) {
            enterScope(new SymbolTable(currentScope));
            ++loopCount;
            node.body.accept(this);
            --loopCount;
            exitScope();
        }
    }

    @Override
    public void visit(Expression node) {
        assert false;
    }

    @Override
    public void visit(ArrayExpression node) {
        node.array.accept(this);
        node.index.accept(this);
        if (node.array.type == null || node.index.type == null) return;
        if (!(node.array.type instanceof ArrayType)) {
            errorRecorder.addRecord(node.array.location, "subscripted value is not an array");
        } else if (!node.index.type.match(intType)) {
            errorRecorder.addRecord(node.index.location, "array subscript should be of int typeNode");
        } else {
            node.type = ((ArrayType) node.array.type).baseType;
            node.isLeft = true;
        }
    }

    @Override
    public void visit(AssignExpression node) {
        node.lhs.accept(this);
        node.rhs.accept(this);
        if (node.lhs.type == null || node.rhs.type == null) return;
        if (!node.lhs.type.match(node.rhs.type)) {
            errorRecorder.addRecord(node.location, "can not assign '" + node.rhs.type.toString() + "' to '" + node.lhs.type.toString() + "'");
        } else if (!node.lhs.isLeft) {
            errorRecorder.addRecord(node.location, "expression is not assignable");
        }
        node.type = voidType;
    }

    @Override
    public void visit(BinaryExpression node) {
        node.lhs.accept(this);
        node.rhs.accept(this);
        if (node.lhs.type == null || node.rhs.type == null) return;
        if (!node.lhs.type.match(node.rhs.type)) {
            errorRecorder.addRecord(node.location, "incompatible typeNode of '" + node.lhs.type.toString() + "' and '" + node.rhs.type.toString() + "'");
        } else {
            if (node.lhs.type.equals(nullType) || node.rhs.type.equals(nullType)) {
                if (equalityOp(node.op)) {
                    node.type = boolType;
                } else {
                    errorRecorder.addRecord(node.location, "undefined operation '" + node.op + "' for null");
                }
            } else if (node.lhs.type.match(intType)) {
                if (arithmeticOp(node.op)) {
                    node.type = intType;
                } else if (equalityOp(node.op) || compareOp(node.op)) {
                    node.type = boolType;
                } else {
                    errorRecorder.addRecord(node.location, "undefined operation '" + node.op + "' for int typeNode");
                }
            } else if (node.lhs.type.match(boolType)) {
                if (equalityOp(node.op) || boolOp(node.op)) {
                    node.type = boolType;
                } else {
                    errorRecorder.addRecord(node.location, "undefined operation '" + node.op + "' for bool typeNode");
                }
            } else if (node.lhs.type.match(stringType)) {
                if (compareOp(node.op) || equalityOp(node.op)) {
                    node.type = boolType;
                } else if (node.op.equals("+")) {
                    node.type = stringType;
                } else {
                    errorRecorder.addRecord(node.location, "undefined operation '" + node.op + "' for string typeNode");
                }
            } else {
                errorRecorder.addRecord(node.location, "invalid type '" + node.lhs.type.toString() + "' for binary operation");
            }
        }
    }

    @Override
    public void visit(FuncCallExpression node) {
        node.functionSymbol = resolveFunction(currentScope, node.function.name);
        node.parameterList.forEach(expression -> expression.accept(this));
        if (node.functionSymbol == null) {
            errorRecorder.addRecord(node.function.location, "undefined function '" + node.function.name + "'");
            return;
        }
        node.type = node.functionSymbol.returnType;
        int n = node.functionSymbol.parameterTypeList.size();
        int st = node.functionSymbol.isGlobal ? 0 : 1;  //class function: "this"
        if (n - st != node.parameterList.size()) {
            errorRecorder.addRecord(node.location, "mismatching argument numbers");
        } else {
            for (int i = st; i < n; ++i) {
                if (node.parameterList.get(i - st).type == null) return;
                if (!node.functionSymbol.parameterTypeList.get(i).match(node.parameterList.get(i - st).type)) {
                    errorRecorder.addRecord(node.location, "mismatching argument types");
                }
            }
        }
        if (currentFunction != null) {
            currentFunction.callee.add(node.functionSymbol);
        }
    }

    @Override
    public void visit(Identifier node) {
        node.isLeft = !node.name.equals("this");
        node.symbol = resolveVariable(currentScope, node.name);
        if (node.symbol == null) {
            errorRecorder.addRecord(node.location, "undefined identifier '" + node.name + "'");
            return;
        }
        node.type = node.symbol.type;
        if (node.symbol.isGlobal && currentFunction != null) {
            currentFunction.usedGlobals.add(node.symbol);
            currentFunction.withSideEffect = true;
        }
    }

    @Override
    public void visit(LiteralExpression node) {
        switch (node.typeName) {
            case "int":
                node.type = intType;
                break;
            case "bool":
                node.type = boolType;
                break;
            case "string":
                node.type = stringType;
                break;
            case "null":
                node.type = nullType;
                break;
        }
    }

    @Override
    public void visit(MemberExpression node) {
        node.lhs.accept(this);
        if (node.lhs.type == null) return;
        Type type = node.lhs.type;
        if (type instanceof ClassSymbol) {
            if (node.identifier != null) {
                VariableSymbol variableSymbol = resolveVariable(((ClassSymbol) type).symbolTable, node.identifier.name);
                if (variableSymbol == null) {
                    errorRecorder.addRecord(node.identifier.location, "undefined field '" + node.identifier.name + "' in '" + type.toString() + "'");
                    return;
                }
                node.type = variableSymbol.type;
                node.isLeft = true;
            } else {
                FunctionSymbol functionSymbol = resolveFunction(((ClassSymbol) type).symbolTable, node.functionCall.function.name);
                node.functionCall.parameterList.forEach(expression -> expression.accept(this));
                if (functionSymbol == null) {
                    errorRecorder.addRecord(node.functionCall.location, "undefined function '" + node.functionCall.function.name + "' in '" + type.toString() + "'");
                    return;
                }
                node.functionCall.functionSymbol = functionSymbol;
                node.type = functionSymbol.returnType;
                int n = functionSymbol.parameterTypeList.size();
                int st = 1;
                if (n - st != node.functionCall.parameterList.size()) {
                    errorRecorder.addRecord(node.location, "mismatching argument numbers");
                } else {
                    for (int i = st; i < n; ++i) {
                        if (node.functionCall.parameterList.get(i - st).type == null) return;
                        if (!functionSymbol.parameterTypeList.get(i).match(node.functionCall.parameterList.get(i - st).type)) {
                            errorRecorder.addRecord(node.location, "mismatching argument types");
                        }
                    }
                }
            }
        } else if (type instanceof ArrayType) {
            if (!(node.functionCall != null && node.functionCall.function.name.equals("size"))) {
                errorRecorder.addRecord(node.location, "undefined member or method of array type");
            } else {
                node.type = intType;
            }
        } else {
            errorRecorder.addRecord(node.lhs.location, "member reference base type is not a class, array or string");
        }
    }

    @Override
    public void visit(NewExpression node) {
        node.dimExpr.forEach(expression -> expression.accept(this));
        int dim = node.dimExpr.size() + node.remDim;
        node.type = resolveType(node.typeNode);
        if (node.type == null) return;
        if (dim > 0) {
            node.type = getArrayType(node.type, dim);
        }
    }

    @Override
    public void visit(UnaryExpression node) {
        node.expression.accept(this);
        if (node.expression.type == null) return;
        if (node.expression.type.match(intType)) {
            if (unaryAriOp(node.op)) {
                node.type = intType;
            } else if (selfIncPrefixOp(node.op) || selfIncSuffixOp(node.op)) {
                if (!node.expression.isLeft) {
                    errorRecorder.addRecord(node.expression.location, "expression is not assignable");
                } else {
                    node.type = intType;
                    node.isLeft = selfIncPrefixOp(node.op);
                }
            } else {
                errorRecorder.addRecord(node.location, "undefined operation '" + node.op + "' for int typeNode");
            }
        } else if (node.expression.type.match(boolType)) {
            if (node.op.equals("!")) {
                node.type = boolType;
            } else {
                errorRecorder.addRecord(node.location, "undefined operation '" + node.op + "' for bool typeNode");
            }
        } else {
            errorRecorder.addRecord(node.location, "invalid typeNode '" + node.expression.type.toString() + "' for unary operation");
        }
    }
}

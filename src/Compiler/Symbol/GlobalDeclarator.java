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

public class GlobalDeclarator implements ASTVisitor {

    private ErrorRecorder errorRecorder;
    private GlobalSymbolTable global;
    private SymbolTable currentScope;
    private ClassSymbol currentClass;

    public GlobalDeclarator(ErrorRecorder errorRecorder, GlobalSymbolTable global) {
        this.errorRecorder = errorRecorder;
        this.global = global;
        this.currentScope = global;
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

    private void defineVariable(VariableDeclaration variableDeclaration) {
        VariableSymbol variableSymbol = new VariableSymbol(variableDeclaration.name);
        variableSymbol.location = variableDeclaration.location;
        variableSymbol.isGlobal = (currentScope instanceof GlobalSymbolTable);
        variableSymbol.type = resolveType(variableDeclaration.type);
        defineVariable(variableSymbol);
        variableDeclaration.variableSymbol = variableSymbol;
    }

    private void defineVariable(VariableSymbol variableSymbol) {
        if (currentScope.containVariable(variableSymbol.name)) {
            errorRecorder.addRecord(variableSymbol.location, "redefinition of variable '" + variableSymbol.name + "'");
        } else if (global.containClass(variableSymbol.name)) {
            errorRecorder.addRecord(variableSymbol.location, "redefinition of '" + variableSymbol.name + "'; previously as a class");
        } else {
            currentScope.addVariable(variableSymbol.name, variableSymbol);
        }
    }

    private void defineFunction(FunctionDeclaration functionDeclaration) {
        FunctionSymbol functionSymbol = new FunctionSymbol(functionDeclaration.name);
        functionSymbol.location = functionDeclaration.location;
        functionSymbol.returnType = resolveType(functionDeclaration.returnType);
        functionSymbol.symbolTable = new SymbolTable(currentScope);
        functionSymbol.isGlobal = (currentScope instanceof GlobalSymbolTable);

        //todo
        enterScope(functionSymbol.symbolTable);
        if (currentClass != null) {
            functionSymbol.parameterNameList.add("this");
            functionSymbol.parameterTypeList.add(currentClass);
            VariableSymbol variableSymbol = new VariableSymbol("this");
            variableSymbol.type = currentClass;
            variableSymbol.location = currentClass.location;
            defineVariable(variableSymbol);
        }
        for (VariableDeclaration x : functionDeclaration.parameterList) {
            functionSymbol.parameterNameList.add(x.name);
            functionSymbol.parameterTypeList.add(resolveType(x.type));
            defineVariable(x);
        }
        exitScope();

        if (currentScope.containFunction(functionSymbol.name)) {
            errorRecorder.addRecord(functionSymbol.location, "redefinition of function '" + functionSymbol.name + "'");
        } else if (functionSymbol.isGlobal && global.containClass(functionSymbol.name)) {
            errorRecorder.addRecord(functionSymbol.location, "redefinition of '" + functionSymbol.name + "'; previously as a class");
        } else {
            currentScope.addFunction(functionSymbol.name, functionSymbol);
        }
        functionDeclaration.functionSymbol = functionSymbol;
    }

    private void enterScope(SymbolTable symbolTable) {
        currentScope = symbolTable;
    }

    private void exitScope() {
        currentScope = currentScope.parent;
    }

    @Override
    public void visit(ASTProgram node) {
        node.functionList.forEach(this::visit);
        node.classList.forEach(this::visit);
    }

    @Override
    public void visit(Declaration node) {
        assert false;
    }

    @Override
    public void visit(ClassDeclaration node) {
        enterScope(node.classSymbol.symbolTable);
        currentClass = node.classSymbol;
        node.variableList.forEach(this::visit);
        node.functionList.forEach(this::visit);
        visit(node.constructor);
        currentClass = null;
        exitScope();
    }

    @Override
    public void visit(FunctionDeclaration node) {
        defineFunction(node);
    }

    @Override
    public void visit(VariableDeclaration node) {
        defineVariable(node);
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

    }

    @Override
    public void visit(BlockStatement node) {

    }

    @Override
    public void visit(BreakStatement node) {

    }

    @Override
    public void visit(ContinueStatement node) {

    }

    @Override
    public void visit(EmptyStatement node) {

    }

    @Override
    public void visit(ExprStatement node) {

    }

    @Override
    public void visit(ForStatement node) {

    }

    @Override
    public void visit(IfStatement node) {

    }

    @Override
    public void visit(ReturnStatement node) {

    }

    @Override
    public void visit(VarDeclStatement node) {

    }

    @Override
    public void visit(WhileStatement node) {

    }

    @Override
    public void visit(Expression node) {

    }

    @Override
    public void visit(ArrayExpression node) {

    }

    @Override
    public void visit(AssignExpression node) {

    }

    @Override
    public void visit(BinaryExpression node) {

    }

    @Override
    public void visit(FuncCallExpression node) {

    }

    @Override
    public void visit(Identifier node) {

    }

    @Override
    public void visit(LiteralExpression node) {

    }

    @Override
    public void visit(MemberExpression node) {

    }

    @Override
    public void visit(NewExpression node) {

    }

    @Override
    public void visit(UnaryExpression node) {

    }
}

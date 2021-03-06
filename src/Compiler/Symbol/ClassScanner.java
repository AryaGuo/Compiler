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

public class ClassScanner implements ASTVisitor {

    private ErrorRecorder errorRecorder;
    private GlobalSymbolTable global;

    public ClassScanner(ErrorRecorder errorRecorder, GlobalSymbolTable global) {
        this.errorRecorder = errorRecorder;
        this.global = global;
    }

    @Override
    public void visit(ASTProgram node) {
        node.classList.forEach(this::visit);
    }

    @Override
    public void visit(Declaration node) {

    }

    @Override
    public void visit(ClassDeclaration node) {
        ClassSymbol classSymbol = new ClassSymbol(node.name);
        classSymbol.location = node.location;
        classSymbol.symbolTable = new SymbolTable(global);
        if (global.containClass(node.name)) {
            errorRecorder.addRecord(node.location, "redefinition of class '" + node.name + "'");
        } else {
            global.addClass(node.name, classSymbol);
            node.classSymbol = classSymbol;
        }
    }

    @Override
    public void visit(FunctionDeclaration node) {

    }

    @Override
    public void visit(VariableDeclaration node) {

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

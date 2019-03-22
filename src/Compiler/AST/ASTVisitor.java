package Compiler.AST;

import Compiler.AST.Declaration.*;
import Compiler.AST.Statement.*;
import Compiler.AST.TypeNode.*;
import Compiler.AST.Expression.*;

public interface ASTVisitor {
    void visit(ASTProgram node);

    void visit(Declaration node);
    void visit(ClassDeclaration node);
    void visit(FunctionDeclaration node);
    void visit(VariableDeclaration node);

    void visit(TypeNode node);
    void visit(ClassTypeNode node);
    void visit(ArrayTypeNode node);
    void visit(PrimitiveTypeNode node);

    void visit(Statement node);
    void visit(BlockStatement node);
    void visit(BreakStatement node);
    void visit(ContinueStatement node);
    void visit(EmptyStatement node);
    void visit(ExprStatement node);
    void visit(ForStatement node);
    void visit(IfStatement node);
    void visit(ReturnStatement node);
    void visit(VarDeclStatement node);
    void visit(WhileStatement node);

    void visit(Expression node);
    void visit(ArrayExpression node);
    void visit(AssignExpression node);
    void visit(BinaryExpression node);
    void visit(FuncCallExpression node);
    void visit(Identifier node);
    void visit(LiteralExpression node);
    void visit(MemberExpression node);
    void visit(NewExpression node);
    void visit(UnaryExpression node);

}

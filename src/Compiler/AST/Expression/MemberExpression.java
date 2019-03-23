package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;

public class MemberExpression extends Expression {

    public Expression lhs;
    public Identifier identifier;
    public FuncCallExpression functionCall;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

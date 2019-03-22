package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;

public class AssignExpression extends Expression {

    public Expression lhs;
    public Expression rhs;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

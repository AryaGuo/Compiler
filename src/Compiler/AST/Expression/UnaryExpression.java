package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;

public class UnaryExpression extends Expression {

    public Expression expression;
    public String op;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

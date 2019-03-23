package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;

public class BinaryExpression extends Expression {

    public Expression lhs;
    public Expression rhs;
    public String op;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

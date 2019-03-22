package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;

public class ArrayExpression extends Expression {

    public Expression array;
    public Expression index;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

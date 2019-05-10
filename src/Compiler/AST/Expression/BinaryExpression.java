package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.Symbol.Type;

public class BinaryExpression extends Expression {

    public Expression lhs;
    public Expression rhs;
    public String op;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public BinaryExpression(Type type, boolean isLeft) {
        super(type, isLeft);
    }

    public BinaryExpression() {
    }

    @Override
    public Expression copy() {
        BinaryExpression ret = new BinaryExpression(type, isLeft);
        ret.lhs = lhs.copy();
        ret.rhs = rhs.copy();
        ret.op = op;
        return ret;
    }
}

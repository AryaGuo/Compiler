package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.Symbol.Type;

public class UnaryExpression extends Expression {

    public Expression expression;
    public String op;

    public UnaryExpression() {
    }

    public UnaryExpression(Type type, boolean isLeft) {
        super(type, isLeft);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Expression copy() {
        UnaryExpression ret = new UnaryExpression(type, isLeft);
        ret.expression = expression.copy();
        ret.op = op;
        return ret;
    }
}

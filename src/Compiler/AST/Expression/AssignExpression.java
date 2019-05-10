package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.Symbol.Type;

public class AssignExpression extends Expression {

    public Expression lhs;
    public Expression rhs;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public AssignExpression(Type type, boolean isLeft) {
        super(type, isLeft);
    }

    public AssignExpression() {
    }

    @Override
    public Expression copy() {
        AssignExpression ret = new AssignExpression(type, isLeft);
        ret.lhs = lhs.copy();
        ret.rhs = rhs.copy();
        return ret;
    }
}

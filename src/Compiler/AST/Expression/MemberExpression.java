package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.Symbol.Type;

public class MemberExpression extends Expression {

    public Expression lhs;
    public Identifier identifier;
    public FuncCallExpression functionCall;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public MemberExpression(Type type, boolean isLeft) {
        super(type, isLeft);
    }

    public MemberExpression() {
    }

    @Override
    public Expression copy() {
        MemberExpression ret = new MemberExpression(type, isLeft);
        ret.lhs = lhs.copy();
        ret.identifier = (Identifier) identifier.copy();
        ret.functionCall = (FuncCallExpression) functionCall.copy();
        return ret;
    }
}

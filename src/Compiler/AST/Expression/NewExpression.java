package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.AST.TypeNode.TypeNode;
import Compiler.Symbol.Type;

import java.util.LinkedList;
import java.util.List;

public class NewExpression extends Expression {

    public TypeNode typeNode;
    public List<Expression> dimExpr;
    public int remDim;  //remaining dimension

    public NewExpression() {
        dimExpr = new LinkedList<>();
    }

    public NewExpression(Type type, boolean isLeft) {
        super(type, isLeft);
        dimExpr = new LinkedList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Expression copy() {
        NewExpression ret = new NewExpression(type, isLeft);
        ret.typeNode = typeNode;
        ret.remDim = remDim;
        for (Expression expression : dimExpr) {
            ret.dimExpr.add(expression.copy());
        }
        return ret;
    }
}

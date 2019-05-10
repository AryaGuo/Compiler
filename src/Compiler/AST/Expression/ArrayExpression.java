package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.Symbol.Type;

public class ArrayExpression extends Expression {

    public Expression array;
    public Expression index;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public ArrayExpression() {
    }

    public ArrayExpression(Type type, boolean isLeft) {
        super(type, isLeft);
    }

    @Override
    public Expression copy() {
        ArrayExpression ret = new ArrayExpression(type, isLeft);
        ret.array = array.copy();
        ret.index = index.copy();
        return ret;
    }
}

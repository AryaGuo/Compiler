package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.Symbol.FunctionSymbol;
import Compiler.Symbol.Type;

import java.util.LinkedList;
import java.util.List;

public class FuncCallExpression extends Expression {

    public Identifier function;
    public List<Expression> parameterList;
    public FunctionSymbol functionSymbol;

    public FuncCallExpression() {
        parameterList = new LinkedList<>();
    }

    public void addParameter(Expression expression) {
        parameterList.add(expression);
    }

    public FuncCallExpression(Type type, boolean isLeft) {
        super(type, isLeft);
        parameterList = new LinkedList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Expression copy() {
        FuncCallExpression ret = new FuncCallExpression(type, isLeft);
        ret.function = (Identifier) function.copy();
        ret.functionSymbol = functionSymbol;
        for (Expression expression : parameterList) {
            ret.parameterList.add(expression.copy());
        }
        return ret;
    }
}

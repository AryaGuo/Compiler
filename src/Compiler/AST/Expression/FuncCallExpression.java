package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.Symbol.FunctionSymbol;

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

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

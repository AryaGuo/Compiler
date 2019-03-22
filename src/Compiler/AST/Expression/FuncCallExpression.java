package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;

import java.util.LinkedList;
import java.util.List;

public class FuncCallExpression extends Expression {

    public Expression function;
    public List<Expression> parameterList;

    public FuncCallExpression() {
        parameterList = new LinkedList<>();
    }

    public void addParamenter(Expression expression) {
        parameterList.add(expression);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

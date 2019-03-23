package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.AST.TypeNode.TypeNode;

import java.util.LinkedList;
import java.util.List;

public class NewExpression extends Expression {

    public TypeNode typeNode;
    public List<Expression> dimExpr;
    public int remDim;  //remaining dimension

    public NewExpression() {
        dimExpr = new LinkedList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

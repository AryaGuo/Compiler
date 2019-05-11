package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Expression.Expression;

public class ReturnStatement extends Statement {

    public Expression expression;
    
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Statement copy() {
        ReturnStatement ret = new ReturnStatement();
        if (expression != null) {
            ret.expression = expression.copy();
        }
        return ret;
    }
}

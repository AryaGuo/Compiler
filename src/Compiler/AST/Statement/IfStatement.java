package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Expression.Expression;

public class IfStatement extends Statement {

    public Expression condition;
    public Statement trueStatement;
    public Statement falseStatement;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Statement copy() {
        IfStatement ret = new IfStatement();
        ret.condition = condition.copy();
        ret.trueStatement = trueStatement.copy();
        if (falseStatement != null) {
            ret.falseStatement = falseStatement.copy();
        }
        return ret;
    }
}

package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Expression.Expression;

public class ForStatement extends Statement {

    public Expression init;
    public Expression condition;
    public Expression step;
    public Statement body;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Statement copy() {
        ForStatement ret = new ForStatement();
        if (init != null) {
            ret.init = init.copy();
        }
        if (condition != null) {
            ret.condition = condition.copy();
        }
        if (step != null) {
            ret.step = step.copy();
        }
        ret.body = body.copy();
        return ret;
    }
}

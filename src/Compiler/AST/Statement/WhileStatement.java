package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Expression.Expression;

public class WhileStatement extends Statement {

    public Expression condition;
    public Statement body;
    
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Statement copy() {
        WhileStatement ret = new WhileStatement();
        ret.condition = condition.copy();
        ret.body = body.copy();
        return ret;
    }
}

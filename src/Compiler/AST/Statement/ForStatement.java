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
}

package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Expression.Expression;

public class ExprStatement extends Statement {

    public Expression expression;
    
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
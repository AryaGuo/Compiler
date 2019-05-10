package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;

public class BreakStatement extends Statement {
    
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Statement copy() {
        return new BreakStatement();
    }
}

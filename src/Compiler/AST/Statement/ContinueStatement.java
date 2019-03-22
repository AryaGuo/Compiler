package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;

public class ContinueStatement extends Statement {
    
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

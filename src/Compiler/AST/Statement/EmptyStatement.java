package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;

public class EmptyStatement extends Statement {
    
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

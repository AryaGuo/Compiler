package Compiler.AST.Statement;

import Compiler.AST.ASTNode;
import Compiler.AST.ASTVisitor;

public abstract class Statement extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public abstract Statement copy();
}

package Compiler.AST.Declaration;

import Compiler.AST.ASTNode;
import Compiler.AST.ASTVisitor;

public abstract class Declaration extends ASTNode {

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

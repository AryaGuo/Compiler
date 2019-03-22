package Compiler.AST.TypeNode;

import Compiler.AST.ASTNode;
import Compiler.AST.ASTVisitor;

public abstract class TypeNode extends ASTNode {
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

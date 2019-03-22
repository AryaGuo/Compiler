package Compiler.AST.TypeNode;

import Compiler.AST.ASTVisitor;

public class PrimitiveTypeNode extends TypeNode {
    public String name;

    public PrimitiveTypeNode(String name) {
        this.name = name;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

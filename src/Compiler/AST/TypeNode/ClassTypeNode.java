package Compiler.AST.TypeNode;

import Compiler.AST.ASTVisitor;

public class ClassTypeNode extends TypeNode {

    public String name;

    public ClassTypeNode(String name) {
        this.name = name;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

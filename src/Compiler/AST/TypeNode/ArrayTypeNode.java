package Compiler.AST.TypeNode;

import Compiler.AST.ASTVisitor;

public class ArrayTypeNode extends TypeNode {

    public TypeNode baseType;

    public int dimension = 0;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

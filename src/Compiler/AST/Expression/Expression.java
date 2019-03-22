package Compiler.AST.Expression;

import Compiler.AST.ASTNode;
import Compiler.AST.ASTVisitor;

public abstract class Expression extends ASTNode {

//    variable type;
    public boolean isLeft;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

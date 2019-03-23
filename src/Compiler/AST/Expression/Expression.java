package Compiler.AST.Expression;

import Compiler.AST.ASTNode;
import Compiler.AST.ASTVisitor;
import Compiler.Symbol.Type;

public abstract class Expression extends ASTNode {

    public Type type;
    public boolean isLeft;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

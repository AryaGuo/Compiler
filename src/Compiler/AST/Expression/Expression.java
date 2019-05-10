package Compiler.AST.Expression;

import Compiler.AST.ASTNode;
import Compiler.AST.ASTVisitor;
import Compiler.Symbol.Type;

public abstract class Expression extends ASTNode {

    public Type type;
    public boolean isLeft;

    public Expression(Type type, boolean isLeft) {
        this.type = type;
        this.isLeft = isLeft;
    }

    public Expression() {
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    public abstract Expression copy();
}

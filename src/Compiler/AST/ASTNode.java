package Compiler.AST;

public abstract class ASTNode {

    public TokenLocation location;
    public abstract void accept(ASTVisitor visitor);
}

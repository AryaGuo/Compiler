package Compiler.AST.Declaration;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Expression.Expression;
import Compiler.AST.TypeNode.TypeNode;

public class VariableDeclaration extends Declaration {

    public TypeNode type;
    public String name;
    public Expression init;

    //todo symbol

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

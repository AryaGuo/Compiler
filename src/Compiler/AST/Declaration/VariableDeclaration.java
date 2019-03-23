package Compiler.AST.Declaration;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Expression.Expression;
import Compiler.AST.TypeNode.TypeNode;
import Compiler.Symbol.VariableSymbol;

public class VariableDeclaration extends Declaration {

    public TypeNode type;
    public String name;
    public Expression init;
    public VariableSymbol variableSymbol;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

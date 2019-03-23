package Compiler.AST.Declaration;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Statement.Statement;
import Compiler.AST.TypeNode.TypeNode;
import Compiler.Symbol.FunctionSymbol;

import java.util.LinkedList;
import java.util.List;

public class FunctionDeclaration extends Declaration {
    public TypeNode returnType;
    public String name;
    public List<VariableDeclaration> parameterList;
    public List<Statement> body;
    public FunctionSymbol functionSymbol;

    public FunctionDeclaration() {
        parameterList = new LinkedList<>();
        body = new LinkedList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

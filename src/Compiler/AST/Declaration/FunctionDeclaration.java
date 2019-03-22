package Compiler.AST.Declaration;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Statement.Statement;
import Compiler.AST.TypeNode.TypeNode;

import java.util.LinkedList;
import java.util.List;

public class FunctionDeclaration extends Declaration {
    public TypeNode returnType;
    public String name;
    public List<VariableDeclaration> paramenterList;
    public List<Statement> body;

    //todo


    public FunctionDeclaration() {
        paramenterList = new LinkedList<>();
        body = new LinkedList<>();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

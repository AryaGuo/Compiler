package Compiler.AST.Declaration;

import Compiler.AST.ASTVisitor;

import java.util.LinkedList;
import java.util.List;

public class ClassDeclaration extends Declaration {

    public String name;
    public List<FunctionDeclaration> functionList;
    public List<VariableDeclaration> variableList;
    public FunctionDeclaration constructor;

    //todo symbol

    public ClassDeclaration() {
        functionList = new LinkedList<>();
        variableList = new LinkedList<>();
    }

    public void addFunction(FunctionDeclaration declaration) {
        functionList.add(declaration);
    }

    public void addVariable(List<VariableDeclaration> declarations) {
        variableList.addAll(declarations);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

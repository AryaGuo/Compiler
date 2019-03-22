package Compiler.AST;

import Compiler.AST.Declaration.ClassDeclaration;
import Compiler.AST.Declaration.Declaration;
import Compiler.AST.Declaration.FunctionDeclaration;
import Compiler.AST.Declaration.VariableDeclaration;

import java.util.LinkedList;
import java.util.List;

public class ASTProgram extends ASTNode {

    public List<FunctionDeclaration> functionList;
    public List<ClassDeclaration> classList;
    public List<VariableDeclaration> variableList;
    public List<Declaration> declarationList;

    public ASTProgram() {
        functionList = new LinkedList<>();
        classList = new LinkedList<>();
        variableList = new LinkedList<>();
        declarationList = new LinkedList<>();
    }

    public void addFunction(FunctionDeclaration declaration) {
        functionList.add(declaration);
        declarationList.add(declaration);
    }

    public void addClass(ClassDeclaration declaration) {
        classList.add(declaration);
        declarationList.add(declaration);
    }

    public void addVariable(List<VariableDeclaration> declarations) {
        variableList.addAll(declarations);
        declarationList.addAll(declarations);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

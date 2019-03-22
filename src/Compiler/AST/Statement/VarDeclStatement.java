package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Declaration.VariableDeclaration;

import java.util.List;

public class VarDeclStatement extends Statement {

    public List<VariableDeclaration> declaration;
    
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

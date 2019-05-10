package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;
import Compiler.AST.Declaration.VariableDeclaration;

import java.util.LinkedList;
import java.util.List;

public class VarDeclStatement extends Statement {

    public List<VariableDeclaration> declaration;
    
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Statement copy() {
        VarDeclStatement ret = new VarDeclStatement();
        ret.declaration = new LinkedList<>();
        for (VariableDeclaration vd : declaration) {
            ret.declaration.add(vd.copy());
        }
        return ret;
    }
}

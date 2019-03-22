package Compiler.AST.Statement;

import Compiler.AST.ASTVisitor;

import java.util.LinkedList;
import java.util.List;

public class BlockStatement extends Statement {

    public List<Statement> statementList;


    public BlockStatement() {
        statementList = new LinkedList<>();
    }

    public void add(Statement statement) {
        statementList.add(statement);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

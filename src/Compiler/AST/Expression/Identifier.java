package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.AST.TokenLocation;
import Compiler.Symbol.VariableSymbol;
import org.antlr.v4.runtime.Token;

public class Identifier extends Expression {

    public String name;
    public VariableSymbol symbol;

    public Identifier(Token token) {
        this.name = token.getText();
        this.location = new TokenLocation(token);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
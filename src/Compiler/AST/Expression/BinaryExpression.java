package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;

public class BinaryExpression extends Expression {

//    public enum BOP {
//        MULTI, DIV, MOD,
//        PLUS, MINUS,
//        LSHIFT, RSHIFT,
//        LT, GT, LEQ, GEQ,
//        EQ, NEQ,
//        AND, XOR, OR,
//        LAND, LOR
//    };

    public Expression lhs;
    public Expression rhs;
    public String op;
    
    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

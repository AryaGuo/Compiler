package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;

public class UnaryExpression extends Expression {

//    public enum UOP {
//        PLUS,   //+x
//        MINUS,  //-x
//        NOT,    //!x
//        REVERSE,    //~x
//        PPLUS,  //++x
//        PMINUS, //--x
//        SPLUS,  //x++
//        SMINUS  //x--
//    };

    public Expression expression;
    public String op;

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}

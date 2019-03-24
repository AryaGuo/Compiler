package Compiler.AST;

import Compiler.AST.Declaration.ClassDeclaration;
import Compiler.AST.Declaration.Declaration;
import Compiler.AST.Declaration.FunctionDeclaration;
import Compiler.AST.Declaration.VariableDeclaration;
import Compiler.AST.Expression.*;
import Compiler.AST.Statement.*;
import Compiler.AST.TypeNode.ArrayTypeNode;
import Compiler.AST.TypeNode.ClassTypeNode;
import Compiler.AST.TypeNode.PrimitiveTypeNode;
import Compiler.AST.TypeNode.TypeNode;

import java.io.PrintStream;

public class ASTPrinter implements ASTVisitor {
    private StringBuilder stringBuilder;
    private String curIndent;
    private String incIndent;

    public ASTPrinter() {
        stringBuilder = new StringBuilder();
        curIndent = "";
        incIndent = "  ";
    }

    public String toString() {
        return stringBuilder.toString();
    }

    public void printTo(PrintStream out) {
        out.println(toString());
    }

    private void indent() {
        curIndent = curIndent + incIndent;
    }

    private void unindent() {
        curIndent = curIndent.substring(0, curIndent.length() - incIndent.length());
    }

    private void appendNewLine(String s) {
        if (stringBuilder.length() != 0)
            stringBuilder.append("\n");
        stringBuilder.append(curIndent);
        stringBuilder.append(s);
    }

    private void appendCurrentLine(String s) {
        stringBuilder.append(s);
    }

    @Override
    public void visit(ASTProgram node) {
        appendNewLine("Functions:");
        indent();
        for (FunctionDeclaration f : node.functionList)
            visit(f);
        unindent();
        appendNewLine("Classes:");
        indent();
        for (ClassDeclaration c : node.classList)
            visit(c);
        unindent();
        appendNewLine("Global Variables:");
        indent();
        for (VariableDeclaration v : node.variableList) {
            appendNewLine("");
            visit(v);
        }
        unindent();
    }

    @Override
    public void visit(Declaration node) {

    }

    @Override
    public void visit(ClassDeclaration node) {
        appendNewLine("class: " + node.name);
        appendNewLine("constructor:");
        indent();
        visit(node.constructor);
        unindent();
        appendNewLine("fields:");
        indent();
        for (VariableDeclaration vd : node.variableList) {
            appendNewLine("");
            visit(vd);
        }
        unindent();
        appendNewLine("methods:");
        indent();
        for (FunctionDeclaration fd : node.functionList)
            visit(fd);
        unindent();
    }

    @Override
    public void visit(FunctionDeclaration node) {
        appendNewLine("function: " + node.name);
        if (node.returnType != null) {     //  not constructor
            appendNewLine("return typeNode: ");
            node.returnType.accept(this);
        }
        if (node.parameterList != null) {
            appendNewLine("parameters:");
            for (VariableDeclaration vd : node.parameterList) {
                visit(vd);
                appendCurrentLine(",");
            }
        }

        appendNewLine("body:");
        indent();
        for (Statement s : node.body)
            s.accept(this);
        unindent();
    }

    @Override
    public void visit(VariableDeclaration node) {
        if (node.type != null) {
            node.type.accept(this);
            appendCurrentLine(" ");
        }
        appendCurrentLine(node.name);
        if (node.init != null) {
            appendCurrentLine(" = ");
            node.init.accept(this);
        }
    }

    @Override
    public void visit(TypeNode node) {

    }

    @Override
    public void visit(ClassTypeNode node) {
        appendCurrentLine(node.name);
    }

    @Override
    public void visit(ArrayTypeNode node) {
        visit(node.baseType);
        for (int i = 0; i < node.dimension; i++)
            appendCurrentLine("[]");
    }

    @Override
    public void visit(PrimitiveTypeNode node) {
        appendCurrentLine(node.name);
    }

    @Override
    public void visit(Statement node) {

    }

    @Override
    public void visit(BlockStatement node) {
        appendNewLine("{");
        indent();
        for (Statement s : node.statementList)
            s.accept(this);
        unindent();
        appendNewLine("}");
    }

    @Override
    public void visit(BreakStatement node) {
        appendNewLine("break");
    }

    @Override
    public void visit(ContinueStatement node) {
        appendNewLine("continue");
    }

    @Override
    public void visit(EmptyStatement node) {

    }

    @Override
    public void visit(ExprStatement node) {
        node.expression.accept(this);
    }

    @Override
    public void visit(ForStatement node) {
        appendNewLine("for:");
        indent();
        appendNewLine("init statement:");
        indent();
        if (node.init != null)
            node.init.accept(this);
        unindent();
        appendNewLine("condition: ");
        if (node.condition != null)
            node.condition.accept(this);
        appendNewLine("update statement: ");
        indent();
        if (node.step != null)
            node.step.accept(this);
        unindent();
        appendNewLine("body:");
        indent();
        node.body.accept(this);
        unindent();
        unindent();
    }

    @Override
    public void visit(IfStatement node) {
        appendNewLine("if:");
        indent();
        appendNewLine("condition: ");
        node.condition.accept(this);
        appendNewLine("then:");
        indent();
        node.trueStatement.accept(this);
        unindent();
        if (node.falseStatement != null) {
            appendNewLine("else:");
            indent();
            node.falseStatement.accept(this);
            unindent();
        }
        unindent();
    }

    @Override
    public void visit(ReturnStatement node) {
        appendNewLine("return ");
        if (node.expression != null)
            node.expression.accept(this);
    }

    @Override
    public void visit(VarDeclStatement node) {
        for (VariableDeclaration x : node.declaration) {
            x.accept(this);
        }
    }

    @Override
    public void visit(WhileStatement node) {
        appendNewLine("while:");
        indent();
        appendNewLine("condition: ");
        node.condition.accept(this);
        appendNewLine("body: ");
        indent();
        node.body.accept(this);
        unindent();
        unindent();
    }

    @Override
    public void visit(Expression node) {

    }

    @Override
    public void visit(ArrayExpression node) {
        node.array.accept(this);
        appendCurrentLine("[");
        node.index.accept(this);
        appendCurrentLine("]");
    }

    @Override
    public void visit(AssignExpression node) {
        node.lhs.accept(this);
        appendCurrentLine("=");
        appendCurrentLine("(");
        node.rhs.accept(this);
        appendCurrentLine(")");
    }

    @Override
    public void visit(BinaryExpression node) {
        appendCurrentLine("(");
        node.lhs.accept(this);
        appendCurrentLine(")");
        appendCurrentLine(node.op);
        appendCurrentLine("(");
        node.rhs.accept(this);
        appendCurrentLine(")");
    }

    @Override
    public void visit(FuncCallExpression node) {
        node.function.accept(this);
        appendCurrentLine(".(");
        for (Expression e : node.parameterList) {
            e.accept(this);
            appendCurrentLine(",");
        }
        appendCurrentLine(")");
    }

    @Override
    public void visit(Identifier node) {
        appendCurrentLine(node.name);
    }

    @Override
    public void visit(LiteralExpression node) {
        appendCurrentLine(node.value);
    }

    @Override
    public void visit(MemberExpression node) {
        node.lhs.accept(this);
        appendCurrentLine(".");
        if (node.identifier != null) {
            visit(node.identifier);
        } else {
            visit(node.functionCall);
        }
    }

    @Override
    public void visit(NewExpression node) {
        appendCurrentLine("new ");
        node.typeNode.accept(this);
        for (Expression e : node.dimExpr) {
            appendCurrentLine("[");
            e.accept(this);
            appendCurrentLine("]");
        }
        for (int i = 0; i < node.remDim; i++)
            appendCurrentLine("[]");
    }

    @Override
    public void visit(UnaryExpression node) {
        if (node.op.contains("x")) {
            if (node.op.charAt(0) == 'x') {
                appendCurrentLine("(");
                node.expression.accept(this);
                appendCurrentLine(")");
                appendCurrentLine(node.op.substring(1, 3));
            } else {
                appendCurrentLine(node.op.substring(0, 2));
                appendCurrentLine("(");
                node.expression.accept(this);
                appendCurrentLine(")");
            }
        } else {
            appendCurrentLine(node.op);
            appendCurrentLine("(");
            node.expression.accept(this);
            appendCurrentLine(")");
        }
    }
}

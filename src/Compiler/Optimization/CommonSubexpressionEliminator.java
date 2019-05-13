package Compiler.Optimization;

import Compiler.AST.ASTPrinter;
import Compiler.AST.ASTProgram;
import Compiler.AST.ASTVisitor;
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
import Compiler.Symbol.PrimitiveSymbol;
import Compiler.Symbol.VariableSymbol;

import java.util.HashMap;
import java.util.HashSet;

public class CommonSubexpressionEliminator implements ASTVisitor {

    private ASTProgram astProgram;

    private HashSet<VariableSymbol> assignDependencySet;
    private HashMap<Long, Identifier> hashCommonSubexpressionMap;

    public CommonSubexpressionEliminator(ASTProgram astProgram) {
        this.astProgram = astProgram;
        this.assignDependencySet = new HashSet<>();
        this.hashCommonSubexpressionMap = new HashMap<>();
    }

    public void run() {
        for (FunctionDeclaration functionDeclaration : astProgram.functionList) {
            functionDeclaration.accept(this);
        }
    }

    private Long expressionHashCode(Expression expression) {
        long mod = 1000000007;
        if (expression instanceof BinaryExpression) {
            Long lhash = expressionHashCode(((BinaryExpression) expression).lhs);
            Long rhash = expressionHashCode(((BinaryExpression) expression).rhs);
            Long ohash = (long) ((BinaryExpression) expression).op.hashCode();
            if (lhash < 0) return (long) -1;
            if (rhash < 0) return (long) -1;
            return ((lhash * 107 + ohash) % mod * 97 + rhash) % mod;
        } else if (expression instanceof Identifier) {
            return ((Identifier) expression).symbol.hashCode() % mod;
        } else if (expression instanceof LiteralExpression) {
            if (((LiteralExpression) expression).typeName.equals("bool")) {
                if (((LiteralExpression) expression).value.equals("true"))
                    return (long) 327;
                else
                    return (long) 123;
            } else if (((LiteralExpression) expression).typeName.equals("int")) {
                return Long.valueOf(((LiteralExpression) expression).value) + 13;
            } else {
                return (long) -1;
            }
        } else {
            return (long) -1;
        }
    }

    private void addDependencySet(Expression expression) {
        if (expression instanceof BinaryExpression) {
            addDependencySet(((BinaryExpression) expression).lhs);
            addDependencySet(((BinaryExpression) expression).rhs);
        } else if (expression instanceof Identifier) {
            assignDependencySet.add(((Identifier) expression).symbol);
        }
    }

    @Override
    public void visit(ASTProgram node) {

    }

    @Override
    public void visit(Declaration node) {

    }

    @Override
    public void visit(ClassDeclaration node) {

    }

    @Override
    public void visit(FunctionDeclaration node) {
        for (Statement statement : node.body) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(VariableDeclaration node) {

    }

    @Override
    public void visit(TypeNode node) {

    }

    @Override
    public void visit(ClassTypeNode node) {

    }

    @Override
    public void visit(ArrayTypeNode node) {

    }

    @Override
    public void visit(PrimitiveTypeNode node) {

    }

    @Override
    public void visit(Statement node) {

    }

    @Override
    public void visit(BlockStatement node) {
        assignDependencySet.clear();
        hashCommonSubexpressionMap.clear();
        for (Statement statement : node.statementList) {
            if (statement instanceof VarDeclStatement) {
                for (VariableDeclaration vd : ((VarDeclStatement) statement).declaration) {
                    if (vd.variableSymbol.type instanceof PrimitiveSymbol) {
                        if (vd.init != null) {
                            Long hashcode = expressionHashCode(vd.init);
                            if (hashcode < 0) continue;
                            Identifier dest = hashCommonSubexpressionMap.get(hashcode);
                            if (dest != null) {
                                System.err.println("replacing ");
                                ASTPrinter astPrinter = new ASTPrinter();
                                vd.init.accept(astPrinter);
                                astPrinter.printTo(System.err);
                                System.err.println("with " + dest.name);
                                vd.init = dest;
                            }
                        }
                    }
                    if (assignDependencySet.contains(vd.variableSymbol)) {
                        assignDependencySet.clear();
                        hashCommonSubexpressionMap.clear();
                        return;
                    } else {
                        if (vd.init == null) continue;
                        Long hashcode = expressionHashCode(vd.init);
                        if (hashcode >= 0) {
                            Identifier identifier = new Identifier();
                            identifier.symbol = vd.variableSymbol;
                            identifier.name = vd.name;
                            hashCommonSubexpressionMap.put(hashcode, identifier);
                            addDependencySet(vd.init);
                            System.err.println("hash code " + hashcode);
                        }
                    }
                }
            } else {
                return;
            }
        }
    }

    @Override
    public void visit(BreakStatement node) {

    }

    @Override
    public void visit(ContinueStatement node) {

    }

    @Override
    public void visit(EmptyStatement node) {

    }

    @Override
    public void visit(ExprStatement node) {

    }

    @Override
    public void visit(ForStatement node) {
        node.body.accept(this);
    }

    @Override
    public void visit(IfStatement node) {
        node.trueStatement.accept(this);
        if (node.falseStatement != null) {
            node.falseStatement.accept(this);
        }
    }

    @Override
    public void visit(ReturnStatement node) {

    }

    @Override
    public void visit(VarDeclStatement node) {

    }

    @Override
    public void visit(WhileStatement node) {
        node.body.accept(this);
    }

    @Override
    public void visit(Expression node) {

    }

    @Override
    public void visit(ArrayExpression node) {

    }

    @Override
    public void visit(AssignExpression node) {

    }

    @Override
    public void visit(BinaryExpression node) {

    }

    @Override
    public void visit(FuncCallExpression node) {

    }

    @Override
    public void visit(Identifier node) {

    }

    @Override
    public void visit(LiteralExpression node) {

    }

    @Override
    public void visit(MemberExpression node) {

    }

    @Override
    public void visit(NewExpression node) {

    }

    @Override
    public void visit(UnaryExpression node) {

    }
}

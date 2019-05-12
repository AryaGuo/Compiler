package Compiler.Optimization;

import Compiler.AST.ASTNode;
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
import Compiler.Symbol.ArrayType;
import Compiler.Symbol.VariableSymbol;

import java.util.*;

public class OutputIrrelevantEliminator implements ASTVisitor {
    private ASTProgram astProgram;
    private HashMap<ASTNode, HashSet<VariableSymbol>> def;
    private HashMap<ASTNode, HashSet<VariableSymbol>> use;
    private HashSet<VariableSymbol> usefulSymbols;
    private LinkedList<Boolean> inAssign;

    private VariableSymbol functionCallIndicator;
    private VariableSymbol globalVariableIndicator;
    private VariableSymbol keyIndicator;

    private enum Stage {
        MARK, RUN, REMOVE
    }

    /*
        Mark: add the important symbols to usefulSymbols; build dependency graph (def & use)
        Run: find out all useful symbols iteratively
        Remove: remove useless node
     */
    private Stage stage;

    public OutputIrrelevantEliminator(ASTProgram astProgram) {
        this.astProgram = astProgram;
        def = new HashMap<>();
        use = new HashMap<>();
        usefulSymbols = new HashSet<>();
        inAssign = new LinkedList<>();
        inAssign.addLast(false);

        functionCallIndicator = new VariableSymbol("functionCallIndicator");
        globalVariableIndicator = new VariableSymbol("globalVariableIndicator");
        keyIndicator = new VariableSymbol("keyIndicator");
        usefulSymbols.add(functionCallIndicator);
        usefulSymbols.add(globalVariableIndicator);
        usefulSymbols.add(keyIndicator);
    }

    public void run() {
        if (astProgram.classList.size() > 0) {
            return;
        }
        astProgram.accept(this);
    }

    private void init(ASTNode node) {
        def.put(node, new HashSet<>());
        use.put(node, new HashSet<>());
    }

    private void add(ASTNode a, ASTNode b) {
//       b is a component of a
        def.get(a).addAll(def.get(b));
        use.get(a).addAll(use.get(b));
    }

    private boolean removable(ASTNode node) {
        HashSet<VariableSymbol> set = new HashSet<>(def.get(node));
        set.retainAll(usefulSymbols);
        return set.isEmpty();
    }

    private void propagate(ASTNode node, ASTNode... succ) {
        propagate(node, Arrays.asList(succ));
    }

    private void propagate(ASTNode node, List<? extends ASTNode> list) {
        if (!removable(node)) {
            for (ASTNode succ : list) {
                if (succ != null) {
                    usefulSymbols.addAll(use.get(succ));
                }
            }
        }
    }

    private void remove(List<Statement> statements) {
        List<Statement> removed = new LinkedList<>();
        for (Statement statement : statements) {
            if (removable(statement)) {
                removed.add(statement);
                System.err.println(statement.toString() + " " + statement.location);
            }
        }
        statements.removeAll(removed);
    }

    @Override
    public void visit(ASTProgram node) {
        stage = Stage.MARK;
        for (FunctionDeclaration fd : node.functionList) {
            fd.accept(this);
        }
        stage = Stage.RUN;
        int last = -1;
        while (last != usefulSymbols.size()) {
            last = usefulSymbols.size();
            for (FunctionDeclaration fd : node.functionList) {
                fd.accept(this);
            }
        }
        stage = Stage.REMOVE;
        for (FunctionDeclaration fd : node.functionList) {
            fd.accept(this);
        }
    }

    @Override
    public void visit(Declaration node) {

    }

    @Override
    public void visit(ClassDeclaration node) {

    }

    @Override
    public void visit(FunctionDeclaration node) {
        switch (stage) {
            case MARK:
            case RUN:
                for (Statement statement : node.body) {
                    statement.accept(this);
                }
                break;
            case REMOVE:
                remove(node.body);
        }
    }

    @Override
    public void visit(VariableDeclaration node) {
        switch (stage) {
            case MARK:
                init(node);
                if (node.init != null) {
                    inAssign.addLast(true);
                    node.init.accept(this);
                    inAssign.removeLast();
                    add(node, node.init);
                }
                def.get(node).add(node.variableSymbol);
                break;
            case RUN:
                if (node.init != null) {
                    propagate(node, node.init);
                    node.init.accept(this);
                }
                break;
        }
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
        switch (stage) {
            case MARK:
                init(node);
                for (Statement statement : node.statementList) {
                    statement.accept(this);
                    add(node, statement);
                }
                break;
            case RUN:
                for (Statement statement : node.statementList) {
                    statement.accept(this);
                }
                break;
            case REMOVE:
                remove(node.statementList);
        }
    }

    @Override
    public void visit(BreakStatement node) {
        if (stage == Stage.MARK) {
            init(node);
            def.get(node).add(keyIndicator);
        }
    }

    @Override
    public void visit(ContinueStatement node) {
        if (stage == Stage.MARK) {
            init(node);
            def.get(node).add(keyIndicator);
        }
    }

    @Override
    public void visit(EmptyStatement node) {
        if (stage == Stage.MARK) {
            init(node);
        }
    }

    @Override
    public void visit(ExprStatement node) {
        switch (stage) {
            case MARK:
                init(node);
                node.expression.accept(this);
                add(node, node.expression);
                break;
            case RUN:
                propagate(node, node.expression);
                node.expression.accept(this);
                break;
        }
    }

    @Override
    public void visit(ForStatement node) {
        switch (stage) {
            case MARK:
                init(node);
                if (node.init != null) {
                    node.init.accept(this);
                    add(node, node.init);
                }
                if (node.step != null) {
                    node.step.accept(this);
                    add(node, node.step);
                }
                if (node.condition != null) {
                    node.condition.accept(this);
                    add(node, node.condition);
                }
                node.body.accept(this);
                add(node, node.body);
                break;
            case RUN:
                propagate(node, node.init, node.step, node.condition);
                node.body.accept(this);
                break;
        }

    }

    @Override
    public void visit(IfStatement node) {
        switch (stage) {
            case MARK:
                init(node);
                node.condition.accept(this);
                add(node, node.condition);
                node.trueStatement.accept(this);
                add(node, node.trueStatement);
                if (node.falseStatement != null) {
                    node.falseStatement.accept(this);
                    add(node, node.falseStatement);
                }
                break;
            case RUN:
                propagate(node, node.condition, node.trueStatement, node.falseStatement);
                node.condition.accept(this);
                node.trueStatement.accept(this);
                if (node.falseStatement != null) {
                    node.falseStatement.accept(this);
                }
                break;
        }
    }

    @Override
    public void visit(ReturnStatement node) {
        switch (stage) {
            case MARK:
                init(node);
                if (node.expression != null) {
                    node.expression.accept(this);
                    add(node, node.expression);
                    usefulSymbols.addAll(use.get(node));
                }
                def.get(node).add(keyIndicator);
                break;
            case RUN:
                if (node.expression != null) {
                    node.expression.accept(this);
                }
                break;

        }
    }

    @Override
    public void visit(VarDeclStatement node) {
        switch (stage) {
            case MARK:
                init(node);
                for (VariableDeclaration vd : node.declaration) {
                    vd.accept(this);
                    add(node, vd);
                }
                break;
            case RUN:
                propagate(node, node.declaration);
                for (VariableDeclaration vd : node.declaration) {
                    vd.accept(this);
                }
                break;
        }
    }

    @Override
    public void visit(WhileStatement node) {
        switch (stage) {
            case MARK:
                init(node);
                node.condition.accept(this);
                add(node, node.condition);
                node.body.accept(this);
                add(node, node.body);
                break;
            case RUN:
                propagate(node, node.condition, node.body);
                node.condition.accept(this);
                node.body.accept(this);
                break;
        }
    }

    @Override
    public void visit(Expression node) {

    }

    @Override
    public void visit(ArrayExpression node) {
        switch (stage) {
            case MARK:
                init(node);
                node.array.accept(this);
                add(node, node.array);
                inAssign.addLast(false);
                node.index.accept(this);
                inAssign.removeLast();
                add(node, node.index);
                break;
        }
    }

    @Override
    public void visit(AssignExpression node) {
        switch (stage) {
            case MARK:
                init(node);
                inAssign.addLast(true);
                node.lhs.accept(this);
                inAssign.removeLast();
                add(node, node.lhs);
                def.get(node).addAll(def.get(node.lhs)); // TODO: 2019-05-12
                node.rhs.accept(this);
                add(node, node.rhs);
                if (node.rhs.type instanceof ArrayType) {
                    def.get(node).addAll(use.get(node.rhs));
                }
                break;
            case RUN:
                propagate(node, node.lhs, node.rhs);
                node.lhs.accept(this);
                node.rhs.accept(this);
                break;
        }
    }

    @Override
    public void visit(BinaryExpression node) {
        switch (stage) {
            case MARK:
                init(node);
                node.lhs.accept(this);
                add(node, node.lhs);
                node.rhs.accept(this);
                add(node, node.rhs);
                break;
            case RUN:
                propagate(node, node.lhs, node.rhs);
                node.lhs.accept(this);
                node.rhs.accept(this);
                break;
        }
    }

    @Override
    public void visit(FuncCallExpression node) {
        switch (stage) {
            case MARK:
                init(node);
                for (Expression expression : node.parameterList) {
                    expression.accept(this);
                    add(node, expression);
                }
                if (node.functionSymbol != null && node.functionSymbol.withSideEffect) {
                    def.get(node).add(functionCallIndicator);
                    usefulSymbols.addAll(use.get(node));
                }
                break;
            case RUN:
                propagate(node, node.parameterList);
                for (Expression expression : node.parameterList) {
                    expression.accept(this);
                }
                break;
        }
    }

    @Override
    public void visit(Identifier node) {
        if (stage == Stage.MARK) {
            init(node);
            if (node.symbol.isGlobal) {
                def.get(node).add(globalVariableIndicator);
            } else {
                use.get(node).add(node.symbol);
                if (inAssign.getLast()) {
                    def.get(node).add(node.symbol);
                }
            }
        }
    }

    @Override
    public void visit(LiteralExpression node) {
        if (stage == Stage.MARK) {
            init(node);
        }
    }

    @Override
    public void visit(MemberExpression node) {
        switch (stage) {
            case MARK:
                init(node);
                node.lhs.accept(this);
                add(node, node.lhs);
                if (node.identifier != null) {
                    node.identifier.accept(this);
                    add(node, node.identifier);
                }
                if (node.functionCall != null) {
                    node.functionCall.accept(this);
                    add(node, node.functionCall);
                }
                break;
            case RUN:
                propagate(node, node.lhs, node.identifier, node.functionCall);
                node.lhs.accept(this);
                if (node.identifier != null) {
                    node.identifier.accept(this);
                }
                if (node.functionCall != null) {
                    node.functionCall.accept(this);
                }
                break;
        }
    }

    @Override
    public void visit(NewExpression node) {
        switch (stage) {
            case MARK:
                init(node);
                for (Expression expression : node.dimExpr) {
                    expression.accept(this);
                    add(node, expression);
                }
                break;
            case RUN:
                propagate(node, node.dimExpr);
                for (Expression expression : node.dimExpr) {
                    expression.accept(this);
                }
                break;
        }
    }

    @Override
    public void visit(UnaryExpression node) {
        switch (stage) {
            case MARK:
                init(node);
                node.expression.accept(this);
                add(node, node.expression);
                if (node.op.contains("++") || node.op.contains("--")) {
                    def.get(node).addAll(use.get(node.expression));
                }
                break;
            case RUN:
                propagate(node, node.expression);
                node.expression.accept(this);
                break;
        }
    }

}

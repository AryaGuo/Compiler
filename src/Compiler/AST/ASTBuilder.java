package Compiler.AST;

import Compiler.AST.Declaration.ClassDeclaration;
import Compiler.AST.Declaration.FunctionDeclaration;
import Compiler.AST.Declaration.VariableDeclaration;
import Compiler.AST.Expression.*;
import Compiler.AST.Statement.*;
import Compiler.AST.TypeNode.ArrayTypeNode;
import Compiler.AST.TypeNode.ClassTypeNode;
import Compiler.AST.TypeNode.PrimitiveTypeNode;
import Compiler.AST.TypeNode.TypeNode;
import Compiler.Parser.MxBaseVisitor;
import Compiler.Parser.MxParser;
import Compiler.Utility.ErrorRecorder;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.LinkedList;
import java.util.List;

import static Compiler.Parser.MxParser.IDENTIFIER;
import static Compiler.Parser.MxParser.THIS;

public class ASTBuilder extends MxBaseVisitor {

    private ASTProgram program;
    private ErrorRecorder errorRecorder;

    public ASTBuilder(ErrorRecorder errorRecorder) {
        program = new ASTProgram();
        program.location = new TokenLocation(0, 0);
        this.errorRecorder = errorRecorder;
    }

    public ASTProgram getProgram() {
        return program;
    }

    @Override
    public ASTProgram visitProgram(MxParser.ProgramContext ctx) {
        for (MxParser.DeclarationContext x : ctx.declaration()) {
            if (x.classDeclaration() != null) {
                program.addClass(visitClassDeclaration(x.classDeclaration()));
            } else if (x.functionDeclaration() != null) {
                program.addFunction(visitFunctionDeclaration(x.functionDeclaration()));
            } else {
                program.addVariable(visitVariableDeclaration(x.variableDeclaration()));
            }
        }
        return program;
    }

    @Override
    public List<VariableDeclaration> visitVariableDeclaration(MxParser.VariableDeclarationContext ctx) {
        TypeNode type = visitType(ctx.type());
        List<VariableDeclaration> list = visitVariableDeclarators(ctx.variableDeclarators());
        list.forEach(variableDeclaration -> variableDeclaration.type = type);
        return list;
    }

    @Override
    public List<VariableDeclaration> visitVariableDeclarators(MxParser.VariableDeclaratorsContext ctx) {
        List<VariableDeclaration> list = new LinkedList<>();
        ctx.variableDeclarator().forEach(variableDeclaratorContext -> list.add(visitVariableDeclarator(variableDeclaratorContext)));
        return list;
    }

    @Override
    public VariableDeclaration visitVariableDeclarator(MxParser.VariableDeclaratorContext ctx) {
        VariableDeclaration variableDeclaration = new VariableDeclaration();
        variableDeclaration.location = new TokenLocation(ctx);
        variableDeclaration.name = ctx.IDENTIFIER().getText();
        if (ctx.expression() != null) {
            variableDeclaration.init = (Expression) ctx.expression().accept(this);
        }
        return variableDeclaration;
    }

    @Override
    public FunctionDeclaration visitFunctionDeclaration(MxParser.FunctionDeclarationContext ctx) {
        FunctionDeclaration functionDeclaration = new FunctionDeclaration();
        functionDeclaration.returnType = visitReturnType(ctx.returnType());
        functionDeclaration.name = ctx.IDENTIFIER().getText();
        if (ctx.parameterList() != null) {
            functionDeclaration.parameterList = visitParameterList(ctx.parameterList());
        }
        functionDeclaration.body = visitFunctionBody(ctx.functionBody());
        functionDeclaration.location = functionDeclaration.returnType.location;
        return functionDeclaration;
    }

    @Override
    public ClassDeclaration visitClassDeclaration(MxParser.ClassDeclarationContext ctx) {
        ClassDeclaration classDeclaration = new ClassDeclaration();
        classDeclaration.location = new TokenLocation(ctx);
        classDeclaration.name = ctx.IDENTIFIER().getText();
        ctx.variableDeclaration().forEach(variableDeclarationContext ->
                classDeclaration.addVariable(visitVariableDeclaration(variableDeclarationContext)));
        ctx.functionDeclaration().forEach(functionDeclarationContext ->
                classDeclaration.addFunction(visitFunctionDeclaration(functionDeclarationContext)));
        for (MxParser.ConstructorDeclarationContext x : ctx.constructorDeclaration()) {
            if (classDeclaration.constructor == null) {
                classDeclaration.constructor = visitConstructorDeclaration(x);
                if (!classDeclaration.constructor.name.equals(classDeclaration.name)) {
                    errorRecorder.addRecord(new TokenLocation(x), "expected a typeNode specifier");
                }
            } else {
                errorRecorder.addRecord(new TokenLocation(x), "constructor cannot be redeclared");
            }
        }
        if (classDeclaration.constructor == null) {
            FunctionDeclaration functionDeclaration = new FunctionDeclaration();
            functionDeclaration.name = classDeclaration.name;

            classDeclaration.constructor = functionDeclaration;
        }
        return classDeclaration;
    }

    @Override
    public List<VariableDeclaration> visitParameterList(MxParser.ParameterListContext ctx) {
        List<VariableDeclaration> list = new LinkedList<>();
        if (ctx.type() == null) {
            return list;
        }
        for (int i = 0; i < ctx.type().size(); ++i) {
            VariableDeclaration variableDeclaration = new VariableDeclaration();
            variableDeclaration.type = visitType(ctx.type(i));
            variableDeclaration.name = ctx.IDENTIFIER(i).getText();
            variableDeclaration.location = new TokenLocation(ctx.type(i));
            list.add(variableDeclaration);
        }
        return list;
    }

    @Override
    public TypeNode visitType(MxParser.TypeContext ctx) {
        if (ctx.baseType() != null) {
            return visitBaseType(ctx.baseType());
        } else {
            return visitArrayType(ctx.arrayType());
        }
    }

    @Override
    public TypeNode visitBaseType(MxParser.BaseTypeContext ctx) {
        if (ctx.IDENTIFIER() != null) {
            ClassTypeNode classTypeNode = new ClassTypeNode(ctx.IDENTIFIER().getText());
            classTypeNode.location = new TokenLocation(ctx);
            return classTypeNode;
        } else {
            PrimitiveTypeNode primitiveTypeNode = new PrimitiveTypeNode(ctx.token.getText());
            primitiveTypeNode.location = new TokenLocation(ctx);
            return primitiveTypeNode;
        }
    }

    @Override
    public ArrayTypeNode visitArrayType(MxParser.ArrayTypeContext ctx) {
        ArrayTypeNode arrayTypeNode = new ArrayTypeNode();
        arrayTypeNode.location = new TokenLocation(ctx);
        arrayTypeNode.baseType = visitBaseType(ctx.baseType());
        for (ParseTree x : ctx.children) {
            if (x.getText().equals("[")) ++arrayTypeNode.dimension;
        }
        return arrayTypeNode;
    }

    @Override
    public TypeNode visitReturnType(MxParser.ReturnTypeContext ctx) {
        if (ctx.VOID() != null) {
            return new PrimitiveTypeNode(ctx.VOID().getText());
        } else {
            return visitType(ctx.type());
        }
    }

    @Override
    public FunctionDeclaration visitConstructorDeclaration(MxParser.ConstructorDeclarationContext ctx) {
        FunctionDeclaration functionDeclaration = new FunctionDeclaration();
        functionDeclaration.name = ctx.IDENTIFIER().getText();
        functionDeclaration.body = visitFunctionBody(ctx.functionBody());
        functionDeclaration.location = new TokenLocation(ctx);
        return functionDeclaration;
    }

    @Override
    public List<Statement> visitFunctionBody(MxParser.FunctionBodyContext ctx) {
        List<Statement> list = new LinkedList<>();
        ctx.statement().forEach(statementContext -> list.add((Statement) statementContext.accept(this)));
        return list;
    }

    @Override
    public BlockStatement visitBlockStatement(MxParser.BlockStatementContext ctx) {
        BlockStatement blockStatement = new BlockStatement();
        blockStatement.location = new TokenLocation(ctx);
        ctx.statement().forEach(statementContext -> blockStatement.add((Statement) statementContext.accept(this)));
        return blockStatement;
    }

    @Override
    public ExprStatement visitExpressionStatement(MxParser.ExpressionStatementContext ctx) {
        ExprStatement exprStatement = new ExprStatement();
        exprStatement.expression = (Expression) ctx.expression().accept(this);
        exprStatement.location = new TokenLocation(ctx);
        return exprStatement;
    }

    @Override
    public IfStatement visitIfStatement(MxParser.IfStatementContext ctx) {
        IfStatement ifStatement = new IfStatement();
        ifStatement.condition = (Expression) ctx.expression().accept(this);
        ifStatement.trueStatement = (Statement) ctx.statement(0).accept(this);
        if (ctx.ELSE() != null) {
            ifStatement.falseStatement = (Statement) ctx.statement(1).accept(this);
        }
        ifStatement.location = new TokenLocation(ctx);
        return ifStatement;
    }

    @Override
    public ForStatement visitForStatement(MxParser.ForStatementContext ctx) {
        ForStatement forStatement = new ForStatement();
        int count = 0;
        if (ctx.init != null) {
            forStatement.init = (Expression) ctx.expression(count).accept(this);
            ++count;
        }
        if (ctx.cond != null) {
            forStatement.condition = (Expression) ctx.expression(count).accept(this);
            ++count;
        }
        if (ctx.step != null) {
            forStatement.step = (Expression) ctx.expression(count).accept(this);
            ++count;
        }
        forStatement.body = (Statement) ctx.statement().accept(this);
        forStatement.location = new TokenLocation(ctx);
        return forStatement;
    }

    @Override
    public WhileStatement visitWhileStatement(MxParser.WhileStatementContext ctx) {
        WhileStatement whileStatement = new WhileStatement();
        whileStatement.condition = (Expression) ctx.expression().accept(this);
        whileStatement.body = (Statement) ctx.statement().accept(this);
        whileStatement.location = new TokenLocation(ctx);
        return whileStatement;
    }

    @Override
    public ContinueStatement visitContinueStatement(MxParser.ContinueStatementContext ctx) {
        ContinueStatement continueStatement = new ContinueStatement();
        continueStatement.location = new TokenLocation(ctx);
        return continueStatement;
    }

    @Override
    public BreakStatement visitBreakStatement(MxParser.BreakStatementContext ctx) {
        BreakStatement breakStatement = new BreakStatement();
        breakStatement.location = new TokenLocation(ctx);
        return breakStatement;
    }

    @Override
    public ReturnStatement visitReturnStatement(MxParser.ReturnStatementContext ctx) {
        ReturnStatement returnStatement = new ReturnStatement();
        if (ctx.expression() != null) {
            returnStatement.expression = (Expression) ctx.expression().accept(this);
        }
        returnStatement.location = new TokenLocation(ctx);
        return returnStatement;
    }

    @Override
    public VarDeclStatement visitVariableDeclarationStatement(MxParser.VariableDeclarationStatementContext ctx) {
        VarDeclStatement varDeclStatement = new VarDeclStatement();
        varDeclStatement.declaration = visitVariableDeclaration(ctx.variableDeclaration());
        varDeclStatement.location = new TokenLocation(ctx);
        return varDeclStatement;
    }

    @Override
    public EmptyStatement visitEmptyStatement(MxParser.EmptyStatementContext ctx) {
        EmptyStatement emptyStatement = new EmptyStatement();
        emptyStatement.location = new TokenLocation(ctx);
        return emptyStatement;
    }

    @Override
    public Expression visitPrimaryExpression(MxParser.PrimaryExpressionContext ctx) {
        if (ctx.token == null) {    //subexpression
            return (Expression) ctx.expression().accept(this);
        } else if (ctx.token.getType() == IDENTIFIER || ctx.token.getType() == THIS) {
            return new Identifier(ctx.token);
        } else {
            return new LiteralExpression(ctx.token);
        }
    }

    @Override
    public BinaryExpression visitBinaryExpression(MxParser.BinaryExpressionContext ctx) {
        BinaryExpression binaryExpression = new BinaryExpression();
        binaryExpression.lhs = (Expression) ctx.expression(0).accept(this);
        binaryExpression.rhs = (Expression) ctx.expression(1).accept(this);
        binaryExpression.op = ctx.operator.getText();
        binaryExpression.location = new TokenLocation(ctx);
        return binaryExpression;
    }

    @Override
    public ArrayExpression visitArrayExpression(MxParser.ArrayExpressionContext ctx) {
        ArrayExpression arrayExpression = new ArrayExpression();
        arrayExpression.array = (Expression) ctx.expression(0).accept(this);
        arrayExpression.index = (Expression) ctx.expression(1).accept(this);
        arrayExpression.location = new TokenLocation(ctx);
        return arrayExpression;
    }

    @Override
    public NewExpression visitNewExpression(MxParser.NewExpressionContext ctx) {
        return visitCreator(ctx.creator());
    }

    @Override
    public AssignExpression visitAssignExpression(MxParser.AssignExpressionContext ctx) {
        AssignExpression assignExpression = new AssignExpression();
        assignExpression.lhs = (Expression) ctx.expression(0).accept(this);
        assignExpression.rhs = (Expression) ctx.expression(1).accept(this);
        assignExpression.location = new TokenLocation(ctx);
        return assignExpression;
    }

    @Override
    public FuncCallExpression visitFunctionCallExpression(MxParser.FunctionCallExpressionContext ctx) {
        return visitFunctionCall(ctx.functionCall());
    }

    @Override
    public UnaryExpression visitUnaryExpression(MxParser.UnaryExpressionContext ctx) {
        UnaryExpression unaryExpression = new UnaryExpression();
        unaryExpression.expression = (Expression) ctx.expression().accept(this);
        unaryExpression.location = new TokenLocation(ctx);
        if (ctx.suffix != null) {
            unaryExpression.op = "x" + ctx.suffix.getText();
        } else if (ctx.prefix.getText().length() > 1) {
            unaryExpression.op = ctx.prefix.getText() + "x";
        } else {
            unaryExpression.op = ctx.prefix.getText();
        }
        return unaryExpression;
    }

    @Override
    public MemberExpression visitMemberExpression(MxParser.MemberExpressionContext ctx) {
        MemberExpression memberExpression = new MemberExpression();
        memberExpression.lhs = (Expression) ctx.expression().accept(this);
        memberExpression.location = new TokenLocation(ctx);
        if (ctx.IDENTIFIER() != null) {
            memberExpression.identifier = new Identifier(ctx.IDENTIFIER().getSymbol());
        } else {
            memberExpression.functionCall = visitFunctionCall(ctx.functionCall());
        }
        return memberExpression;
    }

    @Override
    public FuncCallExpression visitFunctionCall(MxParser.FunctionCallContext ctx) {
        FuncCallExpression funcCallExpression = new FuncCallExpression();
        funcCallExpression.function = new Identifier(ctx.IDENTIFIER().getSymbol());
        funcCallExpression.location = new TokenLocation(ctx);
        for (int i = 0; i < ctx.expression().size(); ++i) {
            funcCallExpression.addParameter((Expression) ctx.expression(i).accept(this));
        }
        return funcCallExpression;
    }

    @Override
    public NewExpression visitCreator(MxParser.CreatorContext ctx) {
        NewExpression newExpression = new NewExpression();
        newExpression.typeNode = visitBaseType(ctx.baseType());
        newExpression.location = new TokenLocation(ctx);
        if (ctx.expression() != null) {
            ctx.expression().forEach(expressionContext -> newExpression.dimExpr.add((Expression) expressionContext.accept(this)));
        }
        if (ctx.empty() != null) {
            newExpression.remDim = ctx.empty().size();
        }
        return newExpression;
    }

}

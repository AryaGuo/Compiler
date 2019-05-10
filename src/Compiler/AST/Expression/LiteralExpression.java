package Compiler.AST.Expression;

import Compiler.AST.ASTVisitor;
import Compiler.AST.TokenLocation;
import Compiler.Symbol.Type;
import org.antlr.v4.runtime.Token;

import static Compiler.Parser.MxParser.*;

public class LiteralExpression extends Expression {

    public String value;
    public String typeName;

    public LiteralExpression(Type type, boolean isLeft) {
        super(type, isLeft);
    }

    public LiteralExpression(Token token) {
        switch (token.getType()) {
            case BOOL_LITERAL:
                this.typeName = "bool";
                this.value = token.getText();
                break;
            case NULL_LITERAL:
                this.typeName = "null";
                this.value = token.getText();
                break;
            case INTEGER_LITERAL:
                this.typeName = "int";
                this.value = token.getText();
                break;
            case STRING_LITERAL:
                this.typeName = "string";
                this.value = getString(token.getText());
                this.value = this.value.substring(1, this.value.length() - 1);
        }
        this.location = new TokenLocation(token);
    }

    private String getString(String s) {
        int n = s.length();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            if (s.charAt(i) == '\\' && i + 1 < n) {
                char nc = s.charAt(i + 1);
                switch (nc) {
                    case 'n':
                        stringBuilder.append('\n');
                        break;
                    case 't':
                        stringBuilder.append('\t');
                        break;
                    case '\\':
                        stringBuilder.append('\\');
                        break;
                    case '"':
                        stringBuilder.append('"');
                        break;
                    default:
                        stringBuilder.append(nc);
                }
                i++;
            } else {
                stringBuilder.append(s.charAt(i));
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Expression copy() {
        LiteralExpression ret = new LiteralExpression(type, isLeft);
        ret.value = value;
        ret.typeName = typeName;
        return ret;
    }
}

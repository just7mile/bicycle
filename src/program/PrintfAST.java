package program;

import lexer.Token;

import java.util.List;
import java.util.Map;

/**
 * AST for printing to console.
 */
public class PrintfAST extends StatementAST {
    /**
     * Expression to print.
     */
    private ExpressionAST expression;

    public PrintfAST(int line, int column) {
        super(line, column);
    }

    public ExpressionAST getExpression() {
        return expression;
    }

    public void setExpression(ExpressionAST expression) {
        this.expression = expression;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> functions, List<AST> calStack) throws Exception {
        if (this.expression != null) {
            this.expression.checkSemantics(types, vars, functions, calStack);
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> functions) throws Exception {
        if (this.expression != null) {
            Object result = this.expression.execute(structs, vars, functions);
            if (result == null) {
                System.out.println("null");
            } else {
                System.out.println(result);
            }
        } else {
            System.out.println();
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(Token.getLabelValue(Token.PRINTF));
        str.append(" ");
        str.append(Token.getLabelValue(Token.LB));
        if (this.expression != null) {
            str.append(this.expression);
        }
        str.append(Token.getLabelValue(Token.RB));
        return str.toString();
    }
}

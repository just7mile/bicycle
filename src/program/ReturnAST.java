package program;

import lexer.Token;

import java.util.List;
import java.util.Map;

public class ReturnAST extends AST implements StatementAST {
    private ExpressionAST expression;

    public ReturnAST(int line, int column) {
        super(line, column);
    }

    public ReturnAST(int line, int column, ExpressionAST expression) {
        super(line, column);
        this.expression = expression;
    }

    public ExpressionAST getExpression() {
        return expression;
    }

    public void setExpression(ExpressionAST expression) {
        this.expression = expression;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> funcs, List<AST> callStack) throws Exception {
        String type = null;
        FunctionAST func = null;
        if (this.expression != null) type = this.expression.checkSemantics(types, vars, funcs, callStack);
        for (AST call: callStack) {
            if (call instanceof FunctionAST) {
                func = (FunctionAST) call;
                break;
            }
        }
        if (func == null) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Unexpected '" + this.toString() + "'!");
        } else if (type == null && !func.getType().equals(Token.getLabelValue(Token.VOID))) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": No '" + Token.getLabelValue(Token.RETURN) + "' value found for type '" + func.getType() + "'!");
        } else if (type != null && func.getType().equals(Token.getLabelValue(Token.VOID))) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": No '" + Token.getLabelValue(Token.RETURN) + "' required for type '" + Token.getLabelValue(Token.VOID) + "'!");
        } else if (type != null && !func.getType().equals(Token.getLabelValue(Token.VOID)) && !type.equals(func.getType())) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Function '" + func.getLabel() + "' returns type '" + func.getType() + "', but found '" + type + "'!");
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> funcs) throws Exception {
        if (this.expression != null) return this.expression.execute(structs, vars, funcs);
        return new ReturnAST(this.getLine(), this.getColumn());
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(Token.getLabelValue(Token.RETURN));
        if (this.expression != null) {
            str.append(" ");
            str.append(this.expression.toString());
        }
        return str.toString();
    }
}

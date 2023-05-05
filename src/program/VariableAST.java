package program;

import lexer.Token;

import java.util.List;
import java.util.Map;

/**
 * AST for a variable.
 */
public class VariableAST extends StatementAST {
    /**
     * Type of the variable.
     */
    private String type;

    /**
     * Name of the variable.
     */
    private String label;

    /**
     * Value of the variable.
     */
    private ExpressionAST value;

    /**
     * Whether it is a function parameter.
     */
    private boolean isParam;

    public VariableAST(String type, String label, int line, int column) {
        super(line, column);
        this.type = type;
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ExpressionAST getValue() {
        return value;
    }

    public void setValue(ExpressionAST value) {
        this.value = value;
    }

    public boolean isParam() {
        return isParam;
    }

    public void setParam(boolean param) {
        isParam = param;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> functions, List<AST> callStack) throws Exception {
        if (types.containsKey(this.label)) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Label '" + this.label + "' is used as type!");
        }
        if (this.value != null) {
            String valueType = this.value.checkSemantics(types, vars, functions, callStack);
            if (valueType != null) {
                if (!valueType.equals("null") && !this.type.equals(valueType)) {
                    throw new Exception("Error at line " + this.value.getLine() + ", column " + this.value.getColumn() + ": Type mismatch, expected '" + this.type + "', but found '" + valueType + "'!");
                }
            }
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> functions) throws Exception {
        if (this.value != null) {
            return this.value.execute(structs, vars, functions);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.label);
        if (this.value != null) {
            str.append(" ");
            str.append(Token.getLabelValue(Token.ASSIGNMENT));
            str.append(" ");
            str.append(this.value);
        }
        return str.toString();
    }
}

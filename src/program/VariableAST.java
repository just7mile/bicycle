package program;

import lexer.Token;

import java.util.List;
import java.util.Map;

public class VariableAST extends AST implements StatementAST {
    private String type, label;
    private ExpressionAST value;
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
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> funcs, List<AST> callStack) throws Exception {
        if (types.containsKey(this.label)) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Label '" + this.label + "' is used as type!");
        }
        if (this.value != null) {
            String valueType = this.value.checkSemantics(types, vars, funcs, callStack);
            if (valueType != null) {
                if (!valueType.equals("null") && !this.type.equals(valueType)) {
                    throw new Exception("Error at line " + this.value.getLine() + ", column " + this.value.getColumn() + ": Type mismatch, needed '" + this.type + "', but found '" + valueType + "'!");
                }
            }
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> funcs) throws Exception {
        if (this.value != null) return this.value.execute(structs, vars, funcs);
        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.label);
        if (this.value != null) {
            str.append(" ");
            str.append(Token.getLabelValue(Token.ASSIGNMENT));
            str.append(" ");
            str.append(this.value.toString());
        }
        return str.toString();
    }
}

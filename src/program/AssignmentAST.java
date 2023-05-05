package program;

import lexer.Token;

import java.util.List;
import java.util.Map;

public class AssignmentAST extends AST implements StatementAST {
    private String label, field;
    private ExpressionAST expression;

    public AssignmentAST(String label, int line, int column) {
        super(line, column);
        this.label = label;
    }

    public AssignmentAST(String label, String field, int line, int column) {
        super(line, column);
        this.label = label;
        this.field = field;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public ExpressionAST getExpression() {
        return expression;
    }

    public void setExpression(ExpressionAST expression) {
        this.expression = expression;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> funcs, List<AST> callStack) throws Exception {
        if (!vars.containsKey(this.label)) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Variable '" + this.label + "' does not exist!");
        }
        String type;
        VariableAST var = vars.get(this.label);
        if (this.field != null) {
            Map<String, String> fields = types.get(var.getType());
            if (!fields.containsKey(this.field)) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Struct '" + var.getType() + "' does not have field '" + this.field + "'!");
            }
            type = fields.get(this.field);
        } else type = var.getType();


        String valueType = this.expression.checkSemantics(types, vars, funcs, callStack);
        if (valueType != null) {
            if (!valueType.equals("null") && !type.equals(valueType)) {
                throw new Exception("Error at line " + this.expression.getLine() + ", column " + this.expression.getColumn() + ": Type mismatch, needed '" + type + "', but found '" + valueType + "'!");
            }
            if (var.getValue() == null) var.setValue(this.expression);
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> funcs) throws Exception {
        ValueObject var = vars.get(this.label);
        if (this.field != null) {
            StructObject struct = (StructObject) var.getValue();
            struct.setField(this.field, this.expression.execute(structs, vars, funcs));
        } else {
            var.setValue(this.expression.execute(structs, vars, funcs));
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.label);
        if (this.field != null) {
            str.append(Token.getLabelValue(Token.DOT));
            str.append(this.field);
        }
        str.append(" ");
        str.append(Token.getLabelValue(Token.ASSIGNMENT));
        str.append(" ");
        str.append(this.expression.toString());
        return str.toString();
    }
}

package program;

import lexer.Token;

import java.util.List;
import java.util.Map;

public class ExpressionAST extends AST {
    private String label;
    private Object value;
    private int operation = 0;
    private ExpressionAST left, right;
    private CallFuncAST func;
    private String field;
    private boolean newObject, isNull;

    public ExpressionAST(int line, int column) {
        super(line, column);
    }

    public ExpressionAST(String label, int line, int column) {
        super(line, column);
        this.label = label;
    }

    public ExpressionAST(Object value, int line, int column) {
        super(line, column);
        this.value = value;
    }

    public ExpressionAST(int operation, int line, int column) {
        super(line, column);
        this.operation = operation;
    }

    public ExpressionAST(CallFuncAST func, int line, int column) {
        super(line, column);
        this.func = func;
    }

    public ExpressionAST(String label, String field, int line, int column) {
        super(line, column);
        this.label = label;
        this.field = field;
    }

    public ExpressionAST(boolean newObject, int line, int column) {
        super(line, column);
        this.newObject = newObject;
    }

    public ExpressionAST(int line, int column, boolean isNull) {
        super(line, column);
        this.isNull = isNull;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getOperation() {
        return operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }

    public ExpressionAST getLeft() {
        return left;
    }

    public void setLeft(ExpressionAST left) {
        this.left = left;
    }

    public ExpressionAST getRight() {
        return right;
    }

    public void setRight(ExpressionAST right) {
        this.right = right;
    }

    public CallFuncAST getFunc() {
        return func;
    }

    public void setFunc(CallFuncAST func) {
        this.func = func;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isNewObject() {
        return newObject;
    }

    public void setNewObject(boolean newObject) {
        this.newObject = newObject;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> funcs, List<AST> callStack) throws Exception {
        if (this.operation != Token.NONE) {
            String leftType = this.left.checkSemantics(types, vars, funcs, callStack);
            if (leftType == null) return null;
            String rightType = this.right.checkSemantics(types, vars, funcs, callStack);
            if (rightType == null) return null;

            if (leftType.equals(Token.getLabelValue(Token.STRING)) || rightType.equals(Token.getLabelValue(Token.STRING))) {
                if (this.operation != Token.ADDITION) {
                    throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Only addition allowed for '" + Token.getLabelValue(Token.INTEGER) + "'!");
                }
                return Token.getLabelValue(Token.STRING);
            } else if ((leftType.equals(Token.getLabelValue(Token.INTEGER))|| leftType.equals(Token.getLabelValue(Token.DOUBLE)))
                    && (rightType.equals(Token.getLabelValue(Token.INTEGER)) || rightType.equals(Token.getLabelValue(Token.DOUBLE)))) {
                if (leftType.equals(Token.getLabelValue(Token.DOUBLE))) return leftType;
                else return rightType;
            } else {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": No math operation allowed between types '" + leftType + "' and '" + rightType + "'!");
            }
        } else if (this.func != null) {
            return this.func.checkSemantics(types, vars, funcs, callStack);
        } else if (this.newObject) {
            return this.label;
        } else if (this.isNull) {
            return "null";
        } else if (this.field != null) {
            if (!vars.containsKey(this.label)) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Variable '" + this.label + "' does not exist!");
            }
            VariableAST var = vars.get(this.label);
            Map<String, String> fields = types.get(var.getType());
            if (!fields.containsKey(this.field)) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Struct '" + var.getType() + "' does not have field '" + this.field + "'!");
            }
            return fields.get(this.field);
        } else if (this.label != null) {
            if (!vars.containsKey(this.label)) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Variable '" + this.label + "' does not exist!");
            }
            VariableAST var = vars.get(this.label);
            if (!var.isParam() && (var.getValue() == null || var.getValue().checkSemantics(types, vars, funcs, callStack) == null)) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Variable '" + var.getLabel() + "' not initialized!");
            }
            return var.getType();
        } else if (this.value != null) {
            if (this.value instanceof Integer) return Token.getLabelValue(Token.INTEGER);
            else if (this.value instanceof Double) return Token.getLabelValue(Token.DOUBLE);
            else if (this.value instanceof String) return Token.getLabelValue(Token.STRING);
            else if (this.value instanceof Boolean)return Token.getLabelValue(Token.BOOLEAN);
        }

        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> funcs) throws Exception {
        if (this.operation != Token.NONE) {
            Object leftResult = this.left.execute(structs, vars, funcs);
            Object rightResult = this.right.execute(structs, vars, funcs);

            switch (this.operation) {
                case Token.ADDITION:
                    if (leftResult instanceof String || rightResult instanceof String) {
                        StringBuilder str = new StringBuilder();
                        if (leftResult instanceof String) str.append((String) leftResult);
                        else if (leftResult instanceof Integer) str.append((Integer) leftResult);
                        else if (leftResult instanceof Double) str.append((Double) leftResult);
                        else if (leftResult instanceof StructObject) str.append(((StructObject) leftResult).toString());
                        if (rightResult instanceof String) str.append((String) rightResult);
                        else if (rightResult instanceof Integer) str.append((Integer) rightResult);
                        else if (rightResult instanceof Double) str.append((Double) rightResult);
                        else if (rightResult instanceof StructObject) str.append(((StructObject) rightResult).toString());
                        return str.toString();
                    } else if (leftResult instanceof Integer) {
                        if (rightResult instanceof Integer) return ((Integer) leftResult) + ((Integer) rightResult);
                        else return ((Integer) leftResult) + ((Double) rightResult);
                    } else if (leftResult instanceof Double) {
                        if (rightResult instanceof Integer) return ((Double) leftResult) + ((Integer) rightResult);
                        else return ((Double) leftResult) + ((Double) rightResult);
                    }
                    return null;
                case Token.SUBTRACTION:
                    if (leftResult instanceof Integer) {
                        if (rightResult instanceof Integer) return ((Integer) leftResult) - ((Integer) rightResult);
                        else return ((Integer) leftResult) - ((Double) rightResult);
                    } else if (leftResult instanceof Double) {
                        if (rightResult instanceof Integer) return ((Double) leftResult) - ((Integer) rightResult);
                        else return ((Double) leftResult) - ((Double) rightResult);
                    }
                case Token.MULTIPLICATION:
                    if (leftResult instanceof Integer) {
                        if (rightResult instanceof Integer) return ((Integer) leftResult) * ((Integer) rightResult);
                        else return ((Integer) leftResult) * ((Double) rightResult);
                    } else if (leftResult instanceof Double) {
                        if (rightResult instanceof Integer) return ((Double) leftResult) * ((Integer) rightResult);
                        else return ((Double) leftResult) * ((Double) rightResult);
                    }
                case Token.DIVISION:
                    if (leftResult instanceof Integer) {
                        if (rightResult instanceof Integer) return ((Integer) leftResult) / ((Integer) rightResult);
                        else return ((Integer) leftResult) / ((Double) rightResult);
                    } else if (leftResult instanceof Double) {
                        if (rightResult instanceof Integer) return ((Double) leftResult) / ((Integer) rightResult);
                        else return ((Double) leftResult) / ((Double) rightResult);
                    }
                case Token.MOD:
                    if (leftResult instanceof Integer) {
                        if (rightResult instanceof Integer) return ((Integer) leftResult) % ((Integer) rightResult);
                        else return ((Integer) leftResult) % ((Double) rightResult);
                    } else if (leftResult instanceof Double) {
                        if (rightResult instanceof Integer) return ((Double) leftResult) % ((Integer) rightResult);
                        else return ((Double) leftResult) % ((Double) rightResult);
                    }
            }
        } else if (this.func != null) {
            return this.func.execute(structs, vars, funcs);
        } else if (this.newObject) {
            StructAST struct = structs.get(this.label);
            StructObject object = new StructObject(this.label);
            for (VarListAST vs: struct.getFields()) {
                for (VariableAST v: vs.getVariables()) {
                    object.addField(v.getLabel(), v.execute(structs, vars, funcs));
                }
            }
            return object;
        } else if (this.isNull) {
            return null;
        } else if (this.field != null) {
            StructObject var = (StructObject) vars.get(this.label).getValue();
            return var.getField(this.field);
        } else if (this.label != null) {
            return vars.get(this.label).getValue();
        }
        return this.value;
    }

    @Override
    public String toString() {
        if (this.func != null) return this.func.toString();
        StringBuilder str = new StringBuilder();
        if (this.operation != Token.NONE) {
            str.append(Token.getLabelValue(Token.LB));
            str.append(this.left.toString());
            str.append(Token.getLabelValue(Token.RB));
            str.append(" ");
            str.append(Token.getLabelValue(this.operation));
            str.append(Token.getLabelValue(Token.LB));
            str.append(this.right.toString());
            str.append(Token.getLabelValue(Token.RB));
        } else if (this.field != null) {
            str.append(this.label);
            str.append(Token.getLabelValue(Token.DOT));
            str.append(this.field);
        } else if (this.newObject) {
            str.append(Token.getLabelValue(Token.NEW));
            str.append(this.label);
            str.append(Token.getLabelValue(Token.LB));
            str.append(Token.getLabelValue(Token.RB));
        } else if (this.label != null) {
            str.append(this.label);
        } else if (this.value != null) {
            if (this.value instanceof String) str.append('"');
            str.append(this.value.toString());
            if (this.value instanceof String) str.append('"');
        } else {
            str.append(Token.getLabelValue(Token.NULL));
        }
        return str.toString();
    }
}

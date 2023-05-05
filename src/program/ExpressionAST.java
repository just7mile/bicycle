package program;

import lexer.Token;

import java.util.List;
import java.util.Map;

/**
 * AST for expressions.
 */
public class ExpressionAST extends AST {
    /**
     * Label of the variable used in the expression.
     */
    private String label;

    /**
     * If the variable is struct, then the field of the struct used in the expression.
     */
    private String field;

    /**
     * A value used in the expression.
     */
    private Object value;

    /**
     * An arithmetic operation used in the expression.
     */
    private int operation = 0;

    /**
     * Left AST of the expression.
     */
    private ExpressionAST left;

    /**
     * Right AST of the expression.
     */
    private ExpressionAST right;

    /**
     * Function call used in the expression.
     */
    private CallFuncAST func;

    /**
     * If the expression is for creating a new object (struct).
     */
    private boolean isNewObjectCreation;

    /**
     * If it is a null expression.
     */
    private boolean isNull;

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

    public ExpressionAST(boolean isNewObjectCreation, int line, int column) {
        super(line, column);
        this.isNewObjectCreation = isNewObjectCreation;
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

    public boolean isNewObjectCreation() {
        return isNewObjectCreation;
    }

    public void setIsNewObject(boolean newObject) {
        this.isNewObjectCreation = newObject;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> functions, List<AST> callStack) throws Exception {
        if (this.operation != Token.NONE) {
            String leftType = this.left.checkSemantics(types, vars, functions, callStack);
            if (leftType == null) {
                return null;
            }
            String rightType = this.right.checkSemantics(types, vars, functions, callStack);
            if (rightType == null) {
                return null;
            }

            if (leftType.equals(Token.getLabelValue(Token.STRING)) || rightType.equals(Token.getLabelValue(Token.STRING))) {
                if (this.operation != Token.ADDITION) {
                    throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Only addition allowed for '" + Token.getLabelValue(Token.INTEGER) + "'!");
                }
                return Token.getLabelValue(Token.STRING);
            } else if ((leftType.equals(Token.getLabelValue(Token.INTEGER)) || leftType.equals(Token.getLabelValue(Token.DOUBLE)))
                    && (rightType.equals(Token.getLabelValue(Token.INTEGER)) || rightType.equals(Token.getLabelValue(Token.DOUBLE)))) {
                if (leftType.equals(Token.getLabelValue(Token.DOUBLE))) {
                    return leftType;
                } else {
                    return rightType;
                }
            } else {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": No math operation allowed between types '" + leftType + "' and '" + rightType + "'!");
            }
        }
        if (this.func != null) {
            return this.func.checkSemantics(types, vars, functions, callStack);
        }
        if (this.isNewObjectCreation) {
            return this.label;
        }
        if (this.isNull) {
            return "null";
        }
        if (this.field != null) {
            if (!vars.containsKey(this.label)) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Variable '" + this.label + "' does not exist!");
            }
            VariableAST var = vars.get(this.label);
            Map<String, String> fields = types.get(var.getType());
            if (!fields.containsKey(this.field)) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Struct '" + var.getType() + "' does not have field '" + this.field + "'!");
            }
            return fields.get(this.field);
        }
        if (this.label != null) {
            if (!vars.containsKey(this.label)) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Variable '" + this.label + "' does not exist!");
            }
            VariableAST var = vars.get(this.label);
            if (!var.isParam() && (var.getValue() == null || var.getValue().checkSemantics(types, vars, functions, callStack) == null)) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Variable '" + var.getLabel() + "' not initialized!");
            }
            return var.getType();
        }
        if (this.value != null) {
            if (this.value instanceof Integer) {
                return Token.getLabelValue(Token.INTEGER);
            }
            if (this.value instanceof Double) {
                return Token.getLabelValue(Token.DOUBLE);
            }
            if (this.value instanceof String) {
                return Token.getLabelValue(Token.STRING);
            }
            if (this.value instanceof Boolean) {
                return Token.getLabelValue(Token.BOOLEAN);
            }
        }

        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> functions) throws Exception {
        if (this.operation != Token.NONE) {
            Object leftResult = this.left.execute(structs, vars, functions);
            Object rightResult = this.right.execute(structs, vars, functions);

            switch (this.operation) {
                case Token.ADDITION:
                    if (leftResult instanceof String || rightResult instanceof String) {
                        return leftResult.toString() + rightResult;
                    }
                    if (leftResult instanceof Integer) {
                        if (rightResult instanceof Integer) {
                            return ((Integer) leftResult) + ((Integer) rightResult);
                        }
                        return ((Integer) leftResult) + ((Double) rightResult);
                    }
                    if (leftResult instanceof Double) {
                        if (rightResult instanceof Integer) {
                            return ((Double) leftResult) + ((Integer) rightResult);
                        }
                        return ((Double) leftResult) + ((Double) rightResult);
                    }
                    return null;
                case Token.SUBTRACTION:
                    if (leftResult instanceof Integer) {
                        if (rightResult instanceof Integer) {
                            return ((Integer) leftResult) - ((Integer) rightResult);
                        }
                        return ((Integer) leftResult) - ((Double) rightResult);
                    }
                    if (leftResult instanceof Double) {
                        if (rightResult instanceof Integer) {
                            return ((Double) leftResult) - ((Integer) rightResult);
                        }
                        return ((Double) leftResult) - ((Double) rightResult);
                    }
                case Token.MULTIPLICATION:
                    if (leftResult instanceof Integer) {
                        if (rightResult instanceof Integer) {
                            return ((Integer) leftResult) * ((Integer) rightResult);
                        }
                        return ((Integer) leftResult) * ((Double) rightResult);
                    }
                    if (leftResult instanceof Double) {
                        if (rightResult instanceof Integer) {
                            return ((Double) leftResult) * ((Integer) rightResult);
                        }
                        return ((Double) leftResult) * ((Double) rightResult);
                    }
                case Token.DIVISION:
                    if (leftResult instanceof Integer) {
                        if (rightResult instanceof Integer) {
                            return ((Integer) leftResult) / ((Integer) rightResult);
                        }
                        return ((Integer) leftResult) / ((Double) rightResult);
                    }
                    if (leftResult instanceof Double) {
                        if (rightResult instanceof Integer) {
                            return ((Double) leftResult) / ((Integer) rightResult);
                        }
                        return ((Double) leftResult) / ((Double) rightResult);
                    }
                case Token.MOD:
                    if (leftResult instanceof Integer) {
                        if (rightResult instanceof Integer) {
                            return ((Integer) leftResult) % ((Integer) rightResult);
                        }
                        return ((Integer) leftResult) % ((Double) rightResult);
                    }
                    if (leftResult instanceof Double) {
                        if (rightResult instanceof Integer) {
                            return ((Double) leftResult) % ((Integer) rightResult);
                        }
                        return ((Double) leftResult) % ((Double) rightResult);
                    }
            }
        }
        if (this.func != null) {
            return this.func.execute(structs, vars, functions);
        }
        if (this.isNewObjectCreation) {
            StructAST struct = structs.get(this.label);
            StructObject object = new StructObject(this.label);
            for (VarListAST vs : struct.getFields()) {
                for (VariableAST v : vs.getVariables()) {
                    object.addField(v.getLabel(), v.execute(structs, vars, functions));
                }
            }
            return object;
        }
        if (this.isNull) {
            return null;
        }
        if (this.field != null) {
            StructObject var = (StructObject) vars.get(this.label).getValue();
            return var.getField(this.field);
        }
        if (this.label != null) {
            return vars.get(this.label).getValue();
        }
        return this.value;
    }

    @Override
    public String toString() {
        if (this.func != null) {
            return this.func.toString();
        }
        StringBuilder str = new StringBuilder();
        if (this.operation != Token.NONE) {
            str.append(Token.getLabelValue(Token.LB));
            str.append(this.left);
            str.append(Token.getLabelValue(Token.RB));
            str.append(" ");
            str.append(Token.getLabelValue(this.operation));
            str.append(Token.getLabelValue(Token.LB));
            str.append(this.right);
            str.append(Token.getLabelValue(Token.RB));
        } else if (this.field != null) {
            str.append(this.label);
            str.append(Token.getLabelValue(Token.DOT));
            str.append(this.field);
        } else if (this.isNewObjectCreation) {
            str.append(Token.getLabelValue(Token.NEW));
            str.append(this.label);
            str.append(Token.getLabelValue(Token.LB));
            str.append(Token.getLabelValue(Token.RB));
        } else if (this.label != null) {
            str.append(this.label);
        } else if (this.value != null) {
            if (this.value instanceof String) {
                str.append('"');
            }
            str.append(this.value);
            if (this.value instanceof String) {
                str.append('"');
            }
        } else {
            str.append(Token.getLabelValue(Token.NULL));
        }
        return str.toString();
    }
}

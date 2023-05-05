package program;

import lexer.Token;

import java.util.List;
import java.util.Map;

public class ComparisonAST extends AST {
    private ComparisonAST left, right;
    private int cmp;
    private ExpressionAST leftExpr, rightExpr;

    public ComparisonAST(int line, int column) {
        super(line, column);
    }

    public ComparisonAST(int cmp, int line, int column) {
        super(line, column);
        this.cmp = cmp;
    }

    public ComparisonAST getLeft() {
        return left;
    }

    public void setLeft(ComparisonAST left) {
        this.left = left;
    }

    public ComparisonAST getRight() {
        return right;
    }

    public void setRight(ComparisonAST right) {
        this.right = right;
    }

    public int getCmp() {
        return cmp;
    }

    public void setCmp(int cmp) {
        this.cmp = cmp;
    }

    public ExpressionAST getLeftExpr() {
        return leftExpr;
    }

    public void setLeftExpr(ExpressionAST leftExpr) {
        this.leftExpr = leftExpr;
    }

    public ExpressionAST getRightExpr() {
        return rightExpr;
    }

    public void setRightExpr(ExpressionAST rightExpr) {
        this.rightExpr = rightExpr;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> functions, List<AST> callStack) throws Exception {
        if (this.cmp == Token.NEGATION) return this.right.checkSemantics(types, vars, functions, callStack);
        else if (this.left != null) {
            String leftType = this.left.checkSemantics(types, vars, functions, callStack);
            String rightType = this.right.checkSemantics(types, vars, functions, callStack);

            if (!leftType.equals(Token.getLabelValue(Token.BOOLEAN)) || !rightType.equals(Token.getLabelValue(Token.BOOLEAN))) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": No boolean operation allowed between types '" + leftType + "' and '" + rightType + "'!");
            }
            return Token.getLabelValue(Token.BOOLEAN);
        } else if (this.leftExpr != null) {
            String leftType = this.leftExpr.checkSemantics(types, vars, functions, callStack);
            if (leftType == null) return null;
            String rightType = this.rightExpr.checkSemantics(types, vars, functions, callStack);
            if (rightType == null) return null;

            if (leftType.equals("null") || rightType.equals("null")) {
                if (this.cmp != Token.NOT_EQUAL && this.cmp != Token.EQUAL) {
                    throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Only '" + Token.getLabelValue(Token.EQUAL) + "' and '" + Token.getLabelValue(Token.EQUAL) + "' operations allowed between types '" + leftType + "' and '" + rightType + "'!");
                } else return Token.getLabelValue(Token.BOOLEAN);
            }

            if (leftType.equals(Token.getLabelValue(Token.STRING)) && rightType.equals(Token.getLabelValue(Token.STRING))) {
                return Token.getLabelValue(Token.BOOLEAN);
            } else if ((leftType.equals(Token.getLabelValue(Token.INTEGER)) || leftType.equals(Token.getLabelValue(Token.DOUBLE)))
                    && (rightType.equals(Token.getLabelValue(Token.INTEGER)) || rightType.equals(Token.getLabelValue(Token.DOUBLE)))) {
                return Token.getLabelValue(Token.BOOLEAN);
            } else {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": No boolean operation allowed between types '" + leftType + "' and '" + rightType + "'!");
            }
        } else if (this.rightExpr != null) {
            String rightType = this.rightExpr.checkSemantics(types, vars, functions, callStack);
            if (rightType == null) return null;
            if (!rightType.equals(Token.getLabelValue(Token.BOOLEAN))) {
                throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Expected '" + Token.getLabelValue(Token.BOOLEAN) + "', but found '" + rightType + "'!");
            }
        }

        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> functions) throws Exception {
        if (this.cmp == Token.NEGATION) {
            return !((Boolean) this.right.execute(structs, vars, functions));
        }
        if (this.left != null) {
            Boolean leftResult = (Boolean) this.left.execute(structs, vars, functions);
            Boolean rightResult = (Boolean) this.right.execute(structs, vars, functions);

            if (this.cmp == Token.AND) {
                return (leftResult && rightResult);
            }
            return (leftResult || rightResult);
        }
        if (this.leftExpr != null) {
            Object leftResult = this.leftExpr.execute(structs, vars, functions);
            Object rightResult = this.rightExpr.execute(structs, vars, functions);
            int compare = 0;
            if (leftResult instanceof String) {
                if (rightResult == null) {
                    return this.cmp != Token.EQUAL;
                }
                compare = ((String) leftResult).compareTo((String) rightResult);
            } else if (leftResult instanceof Integer) {
                if (rightResult == null) {
                    return this.cmp != Token.EQUAL;
                }
                if (rightResult instanceof Integer) {
                    compare = ((Integer) leftResult).compareTo((Integer) rightResult);
                } else {
                    compare = Double.valueOf((Integer) leftResult).compareTo((Double) rightResult);
                }
            } else if (leftResult instanceof Double) {
                if (rightResult == null) {
                    return this.cmp != Token.EQUAL;
                }
                if (rightResult instanceof Integer) {
                    compare = ((Double) leftResult).compareTo(((Integer) rightResult).doubleValue());
                } else {
                    compare = ((Double) leftResult).compareTo((Double) rightResult);
                }
            } else if (leftResult == null) {
                return (this.cmp == Token.EQUAL && rightResult == null) || (this.cmp == Token.NOT_EQUAL && rightResult != null);
            } else if (rightResult == null) {
                return this.cmp != Token.EQUAL;
            }

            switch (this.cmp) {
                case Token.EQUAL -> {
                    return compare == 0;
                }
                case Token.NOT_EQUAL -> {
                    return compare != 0;
                }
                case Token.LESS -> {
                    return compare < 0;
                }
                case Token.MORE -> {
                    return compare > 0;
                }
                case Token.LESS_EQUAL -> {
                    return compare <= 0;
                }
                case Token.MORE_EQUAL -> {
                    return compare >= 0;
                }
            }

        } else if (this.rightExpr != null) {
            return this.rightExpr.execute(structs, vars, functions);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (this.cmp == Token.NEGATION) {
            str.append(Token.getLabelValue(Token.NEGATION));
            str.append(Token.getLabelValue(Token.LB));
            str.append(this.right);
            str.append(Token.getLabelValue(Token.RB));
        } else if (this.left != null) {
            str.append(Token.getLabelValue(Token.LB));
            str.append(this.left);
            str.append(Token.getLabelValue(Token.RB));
            str.append(" ");
            str.append(Token.getLabelValue(this.cmp));
            str.append(Token.getLabelValue(Token.LB));
            str.append(" ");
            str.append(this.right);
            str.append(Token.getLabelValue(Token.RB));
        } else if (this.leftExpr != null) {
            str.append(this.leftExpr);
            str.append(" ");
            str.append(Token.getLabelValue(this.cmp));
            str.append(" ");
            str.append(this.rightExpr);
        } else if (this.rightExpr != null) {
            str.append(this.rightExpr);
        }
        return str.toString();
    }
}

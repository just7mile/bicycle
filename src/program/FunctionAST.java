package program;

import lexer.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * AST for function declaration.
 */
public class FunctionAST extends DeclarationAST {

    /**
     * Return type of the function.
     */
    private String type;

    /**
     * Function name.
     */
    private String label;

    /**
     * List of parameters (arguments).
     */
    private List<VariableAST> params;

    /**
     * List of the statements inside function body.
     */
    private List<StatementAST> statements;

    public FunctionAST(String type, int line, int column) {
        super(line, column);
        this.type = type;
        this.params = new LinkedList<>();
    }

    public void addParam(VariableAST p) {
        this.params.add(p);
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

    public List<VariableAST> getParams() {
        return params;
    }

    public void setParams(List<VariableAST> params) {
        this.params = params;
    }

    public List<StatementAST> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementAST> statements) {
        this.statements = statements;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> varList, Map<String, FunctionAST> functions, List<AST> callStack) throws Exception {
        callStack.add(0, this);
        Map<String, VariableAST> vars = new HashMap<>(varList);
        if (types.containsKey(this.label)) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Label '" + this.label + "' is used as type!");
        }
        if (!types.containsKey(this.type)) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Type '" + this.type + "' does not exist!");
        }
        for (VariableAST v : this.params) {
            if (vars.containsKey(v.getLabel())) {
                throw new Exception("Error at line " + v.getLine() + ", column " + v.getColumn() + ": variable '" + v.getLabel() + "' already exists!");
            }
            v.checkSemantics(types, vars, functions, callStack);
            vars.put(v.getLabel(), v);
        }
        IfStatementAST ifStatementAST = null;
        boolean returnExists = false;
        for (StatementAST statement : this.statements) {
            statement.checkSemantics(types, vars, functions, callStack);
            if (statement instanceof IfStatementAST ifStatement) {
                if (ifStatement.getType() == Token.IF) {
                    ifStatementAST = ifStatement;
                } else if (ifStatementAST == null) {
                    throw new Exception("Error at line " + ifStatement.getLine() + ", column " + ifStatement.getColumn() + ": '" + Token.getLabelValue(ifStatement.getType()) + "' statement without 'IF' statement!");
                }
                if (ifStatement.getType() == Token.ELSE) {
                    ifStatementAST = null;
                }
            } else {
                ifStatementAST = null;
                if (statement instanceof ReturnAST) {
                    returnExists = true;
                }
            }
        }
        if (!returnExists && !this.type.equals(Token.getLabelValue(Token.VOID))) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": No '" + Token.getLabelValue(Token.RETURN) + "' found function '" + this.label + "'!");
        }
        callStack.remove(0);
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> varList, Map<String, FunctionAST> functions) throws Exception {
        Map<String, ValueObject> vars = new HashMap<>(varList);
        boolean ifStatementExecuted = true;
        for (StatementAST statement : this.statements) {
            if (statement instanceof IfStatementAST ifStatement) {
                // Found a new if-statement flow.
                if (ifStatement.getType() == Token.IF) {
                    ifStatementExecuted = false;
                }

                Object ret = null;
                if (!ifStatementExecuted) {
                    ret = ifStatement.execute(structs, vars, functions);
                }
                if (ret != null) {
                    return ret;
                }

                // Mark the if statement executed to ignore following else-if/else statements.
                if (ifStatement.getCondition() == null || (Boolean) ifStatement.getCondition().execute(structs, vars, functions)) {
                    ifStatementExecuted = true;
                }
            } else if (statement instanceof ReturnAST ret) {
                return ret.execute(structs, vars, functions);
            } else if (statement instanceof ForLoopAST forLoop) {
                Object ret = forLoop.execute(structs, vars, functions);
                if (ret != null) return ret;
            } else {
                statement.execute(structs, vars, functions);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.type);
        str.append(" ");
        str.append(this.label);
        str.append(" ");
        str.append(Token.getLabelValue(Token.LB));
        for (int i = 0; i < this.params.size(); i++) {
            if (i > 0) {
                str.append(", ");
            }
            str.append(this.params.get(i));
        }
        str.append(Token.getLabelValue(Token.RB));
        str.append(" ");
        str.append(Token.getLabelValue(Token.LP));
        str.append("\n");
        for (StatementAST s : this.statements) {
            str.append(s);
            if (s instanceof VarListAST || s instanceof PrintfAST || s instanceof ReturnAST
                    || s instanceof CallFuncAST || s instanceof AssignmentAST || s instanceof BreakAST) {
                str.append(Token.getLabelValue(Token.SEMICOLON));
            }
            str.append("\n");
        }
        str.append(Token.getLabelValue(Token.RP));
        return str.toString();
    }
}

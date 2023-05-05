package program;

import lexer.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AST for if-statements.
 */
public class IfStatementAST extends StatementAST {
    /**
     * If statement type: if, else if, or else.
     */
    private int type; // IF | ELSEIF | ELSE

    /**
     * If-statement condition.
     */
    private ComparisonAST condition;

    /**
     * List of statements inside if-statement body.
     */
    private List<StatementAST> statements;

    public IfStatementAST(int type, int line, int column) {
        super(line, column);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ComparisonAST getCondition() {
        return condition;
    }

    public void setCondition(ComparisonAST condition) {
        this.condition = condition;
    }

    public List<StatementAST> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementAST> statements) {
        this.statements = statements;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> varList, Map<String, FunctionAST> functions, List<AST> callStack) throws Exception {
        Map<String, VariableAST> vars = new HashMap<>(varList);
        if (this.condition != null) {
            this.condition.checkSemantics(types, vars, functions, callStack);
        }
        IfStatementAST ifStatementAST = null;
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
            }
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> varList, Map<String, FunctionAST> functions) throws Exception {
        Map<String, ValueObject> vars = new HashMap<>(varList);
        if (this.condition == null || (Boolean) this.condition.execute(structs, vars, functions)) {
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
                } else if (statement instanceof BreakAST breakAST) {
                    return breakAST.execute(structs, vars, functions);
                } else {
                    statement.execute(structs, vars, functions);
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(Token.getLabelValue(this.type));
        str.append(" ");
        if (this.condition != null) {
            str.append(Token.getLabelValue(Token.LB));
            str.append(this.condition);
            str.append(Token.getLabelValue(Token.RB));
        }
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

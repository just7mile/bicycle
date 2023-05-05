package program;

import lexer.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AST for for-loops.
 */
public class ForLoopAST extends StatementAST {
    /**
     * Initial assignment (first block) if only one variable initiated.
     */
    private AssignmentAST initAssignment;

    /**
     * List of initial variables assignments (first block) if more than one variable initiated.
     */
    private VarListAST initVariables;

    /**
     * Increment assignment (third block).
     */
    private AssignmentAST increment;

    /**
     * Loop break condition (second block).
     */
    private ComparisonAST condition;

    /**
     * List of statements inside loop body.
     */
    private List<StatementAST> statements;

    public ForLoopAST(int line, int column) {
        super(line, column);
    }

    public AssignmentAST getInitAssignment() {
        return initAssignment;
    }

    public void setInitAssignment(AssignmentAST initAssignment) {
        this.initAssignment = initAssignment;
    }

    public VarListAST getInitVariables() {
        return initVariables;
    }

    public void setInitVariables(VarListAST initVariables) {
        this.initVariables = initVariables;
    }

    public AssignmentAST getIncrement() {
        return increment;
    }

    public void setIncrement(AssignmentAST increment) {
        this.increment = increment;
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
        callStack.add(0, this);
        Map<String, VariableAST> vars = new HashMap<>(varList);
        if (this.initAssignment != null) {
            this.initAssignment.checkSemantics(types, vars, functions, callStack);
        } else if (this.initVariables != null) {
            this.initVariables.checkSemantics(types, vars, functions, callStack);
        }
        if (this.condition != null) {
            this.condition.checkSemantics(types, vars, functions, callStack);
        }
        if (this.increment != null) {
            this.increment.checkSemantics(types, vars, functions, callStack);
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
        callStack.remove(0);

        return null;
    }

    private void runIncrement(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> functions) throws Exception {
        if (this.increment != null) {
            this.increment.execute(structs, vars, functions);
        }
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> varList, Map<String, FunctionAST> functions) throws Exception {
        Map<String, ValueObject> vars = new HashMap<>(varList);
        if (this.initAssignment != null) {
            this.initAssignment.execute(structs, vars, functions);
        } else if (this.initVariables != null) {
            this.initVariables.execute(structs, vars, functions);
        }

        boolean breakAST = false;
        for (; (this.condition == null || (Boolean) this.condition.execute(structs, vars, functions)); this.runIncrement(structs, vars, functions)) {
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
                    if (ret instanceof BreakAST) {
                        breakAST = true;
                        break;
                    }
                    if (ret != null) {
                        return ret;
                    }

                    // Mark the if statement executed to ignore following else-if/else statements.
                    if (ifStatement.getCondition() == null || (Boolean) ifStatement.getCondition().execute(structs, vars, functions)) {
                        ifStatementExecuted = true;
                    }
                } else if (statement instanceof BreakAST) {
                    breakAST = true;
                    break;
                } else if (statement instanceof ReturnAST ret) {
                    return ret.execute(structs, vars, functions);
                } else {
                    statement.execute(structs, vars, functions);
                }
            }
            if (breakAST) break;
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(Token.getLabelValue(Token.FOR));
        str.append(Token.getLabelValue(Token.LB));
        if (this.initAssignment != null) {
            str.append(this.initAssignment);
        } else if (this.initVariables != null) {
            str.append(this.initVariables);
        }
        str.append(Token.getLabelValue(Token.SEMICOLON));
        if (this.condition != null) {
            str.append(" ");
            str.append(this.condition);
        }
        str.append(Token.getLabelValue(Token.SEMICOLON));
        if (this.increment != null) {
            str.append(" ");
            str.append(this.increment);
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

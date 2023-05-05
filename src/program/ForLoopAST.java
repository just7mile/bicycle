package program;

import lexer.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForLoopAST extends AST implements StatementAST {
    private AssignmentAST initAssignment;
    private VarListAST initVariables;
    private AssignmentAST increment;
    private ComparisionAST condition;
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

    public ComparisionAST getCondition() {
        return condition;
    }

    public void setCondition(ComparisionAST condition) {
        this.condition = condition;
    }

    public List<StatementAST> getStatements() {
        return statements;
    }

    public void setStatements(List<StatementAST> statements) {
        this.statements = statements;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> varList, Map<String, FunctionAST> funcs, List<AST> callStack) throws Exception {
        callStack.add(0, this);
        Map<String, VariableAST> vars = new HashMap<>(varList);
        if (this.initAssignment != null) this.initAssignment.checkSemantics(types, vars, funcs, callStack);
        else if (this.initVariables != null) this.initVariables.checkSemantics(types, vars, funcs, callStack);
        if (this.condition != null) this.condition.checkSemantics(types, vars, funcs, callStack);
        if (this.increment != null) this.increment.checkSemantics(types, vars, funcs, callStack);

        IfStatementAST ifStatementAST = null;
        for (StatementAST statement: this.statements) {
            if (statement instanceof IfStatementAST) {
                IfStatementAST ifStatement = (IfStatementAST) statement;
                ifStatement.checkSemantics(types, vars, funcs, callStack);
                if (ifStatement.getType() == Token.IF) ifStatementAST = ifStatement;
                else {
                    if (ifStatementAST == null) {
                        throw new Exception("Error at line " + ifStatement.getLine() + ", column " + ifStatement.getColumn() + ": '" + Token.getLabelValue(ifStatement.getType()) + "' statement without 'IF' statement!");
                    }
                    if (ifStatement.getType() == Token.ELSE) ifStatementAST = null;
                }
            } else {
                ifStatementAST = null;
                if (statement instanceof VarListAST) {
                    VarListAST vs = (VarListAST) statement;
                    vs.checkSemantics(types, vars, funcs, callStack);
                } else if (statement instanceof AssignmentAST) {
                    AssignmentAST assignment = (AssignmentAST) statement;
                    assignment.checkSemantics(types, vars, funcs, callStack);
                } else if (statement instanceof PrintfAST) {
                    PrintfAST printf = (PrintfAST) statement;
                    printf.checkSemantics(types, vars, funcs, callStack);
                } else if (statement instanceof ReturnAST) {
                    ReturnAST ret = (ReturnAST) statement;
                    ret.checkSemantics(types, vars, funcs, callStack);
                } else if (statement instanceof ForLoopAST) {
                    ForLoopAST forLoop = (ForLoopAST) statement;
                    forLoop.checkSemantics(types, vars, funcs, callStack);
                } else if (statement instanceof CallFuncAST) {
                    CallFuncAST callFunc = (CallFuncAST) statement;
                    callFunc.checkSemantics(types, vars, funcs, callStack);
                } else if (statement instanceof BreakAST) {
                    BreakAST breakAST = (BreakAST) statement;
                    breakAST.checkSemantics(types, vars, funcs, callStack);
                }
            }
        }
        callStack.remove(0);

        return null;
    }

    private void runIncrement(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> funcs) throws Exception {
        if (this.increment != null) this.increment.execute(structs, vars, funcs);
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> varList, Map<String, FunctionAST> funcs) throws Exception {
        Map<String, ValueObject> vars = new HashMap<>(varList);
        if (this.initAssignment != null) this.initAssignment.execute(structs, vars, funcs);
        else if (this.initVariables != null) this.initVariables.execute(structs, vars, funcs);

        boolean breakAST = false;
        for (; (this.condition == null ? true : (Boolean) this.condition.execute(structs, vars, funcs)); this.runIncrement(structs, vars, funcs)) {
            boolean ifStatementExecuted = true;
            for (StatementAST statement: this.statements) {
                if (statement instanceof IfStatementAST) {
                    IfStatementAST ifStatement = (IfStatementAST) statement;
                    if (ifStatement.getType() == Token.IF) ifStatementExecuted = false;
                    Object ret = null;
                    if (!ifStatementExecuted) ret = ifStatement.execute(structs, vars, funcs);
                    if (ret != null) {
                        if (ret instanceof BreakAST) {
                            breakAST = true;
                            break;
                        }
                        else return ret;
                    }
                    if (ifStatement.getCondition() == null || (Boolean) ifStatement.getCondition().execute(structs, vars, funcs)) ifStatementExecuted = true;
                } else if (statement instanceof VarListAST) {
                    VarListAST vs = (VarListAST) statement;
                    vs.execute(structs, vars, funcs);
                } else if (statement instanceof AssignmentAST) {
                    AssignmentAST assignment = (AssignmentAST) statement;
                    assignment.execute(structs, vars, funcs);
                } else if (statement instanceof PrintfAST) {
                    PrintfAST printf = (PrintfAST) statement;
                    printf.execute(structs, vars, funcs);
                } else if (statement instanceof ForLoopAST) {
                    ForLoopAST forLoop = (ForLoopAST) statement;
                    forLoop.execute(structs, vars, funcs);
                } else if (statement instanceof CallFuncAST) {
                    CallFuncAST callFunc = (CallFuncAST) statement;
                    callFunc.execute(structs, vars, funcs);
                } else if (statement instanceof ReturnAST) {
                    ReturnAST ret = (ReturnAST) statement;
                    return ret.execute(structs, vars, funcs);
                } else if (statement instanceof BreakAST) {
                    breakAST = true;
                    break;
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
        if (this.initAssignment != null) str.append(this.initAssignment.toString());
        else if (this.initVariables != null) str.append(this.initVariables.toString());
        str.append(Token.getLabelValue(Token.SEMICOLON));
        if (this.condition != null) {
            str.append(" ");
            str.append(this.condition.toString());
        }
        str.append(Token.getLabelValue(Token.SEMICOLON));
        if (this.increment != null) {
            str.append(" ");
            str.append(this.increment.toString());
        }
        str.append(Token.getLabelValue(Token.RB));
        str.append(" ");
        str.append(Token.getLabelValue(Token.LP));
        str.append("\n");
        for (StatementAST s: this.statements) {
            str.append(s.toString());
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

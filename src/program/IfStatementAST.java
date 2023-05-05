package program;

import lexer.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IfStatementAST extends AST implements StatementAST {
    private int type; // IF | ELSEIF | ELSE
    private ComparisionAST condition;
    private List<StatementAST> statements;

    public IfStatementAST(int line, int column) {
        super(line, column);
    }

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
        Map<String, VariableAST> vars = new HashMap<>(varList);
        if (this.condition != null) this.condition.checkSemantics(types, vars, funcs, callStack);
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
                    BreakAST breakAST= (BreakAST) statement;
                    breakAST.checkSemantics(types, vars, funcs, callStack);
                }
            }
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> varList, Map<String, FunctionAST> funcs) throws Exception {
        Map<String, ValueObject> vars = new HashMap<>(varList);
        if (this.condition == null || (Boolean) this.condition.execute(structs, vars, funcs)) {
            boolean ifStatementExecuted = true;
            for (StatementAST statement: this.statements) {
                if (statement instanceof IfStatementAST) {
                    IfStatementAST ifStatement = (IfStatementAST) statement;
                    if (ifStatement.getType() == Token.IF) ifStatementExecuted = false;
                    Object ret = null;
                    if (!ifStatementExecuted) ret = ifStatement.execute(structs, vars, funcs);
                    if (ret != null) return ret;
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
                    BreakAST breakAST = (BreakAST) statement;
                    return breakAST.execute(structs, vars, funcs);
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
            str.append(this.condition.toString());
            str.append(Token.getLabelValue(Token.RB));
        }
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

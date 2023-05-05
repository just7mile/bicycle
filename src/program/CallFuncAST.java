package program;

import lexer.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * AST for function calls.
 */
public class CallFuncAST extends StatementAST {
    /**
     * The function name.
     */
    private String label;

    /**
     * List of function arguments.
     */
    private List<ExpressionAST> args;

    public CallFuncAST(String label, int line, int column) {
        super(line, column);
        this.label = label;
        this.args = new LinkedList<>();
    }

    public void addArg(ExpressionAST a) {
        this.args.add(a);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<ExpressionAST> getArgs() {
        return args;
    }

    public void setArgs(List<ExpressionAST> args) {
        this.args = args;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> functions, List<AST> callStack) throws Exception {
        if (!functions.containsKey(this.label)) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Function '" + this.label + "' does not exist!");
        }
        if (this.label.equals("main")) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Function '" + this.label + "' can not be called!");
        }
        FunctionAST func = functions.get(this.label);
        if (this.args.size() != func.getParams().size()) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Incorrect number of arguments, expected " + func.getParams().size() + ", but found " + this.args.size());
        }
        for (int i = 0; i < this.args.size(); i++) {
            ExpressionAST arg = this.args.get(i);
            String type = arg.checkSemantics(types, vars, functions, callStack);
            if (type != null && !type.equals("null") && !func.getParams().get(i).getType().equals(type)) {
                throw new Exception("Error at line " + arg.getLine() + ", column " + arg.getColumn() + ": Type mismatch, expected " + func.getParams().get(i).getType() + ", but found " + type);
            }
        }
        return func.getType();
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> varList, Map<String, FunctionAST> functions) throws Exception {
        Map<String, ValueObject> vars = new HashMap<>(varList);
        FunctionAST func = functions.get(this.label);
        List<VariableAST> params = func.getParams();
        for (int i = 0; i < this.args.size(); i++) {
            ExpressionAST arg = this.args.get(i);
            VariableAST param = params.get(i);
            Object value = arg.execute(structs, vars, functions);
            if (value == null && param.getValue() != null) {
                value = param.getValue().execute(structs, vars, functions);
            }
            vars.put(param.getLabel(), new ValueObject(value));
        }
        return func.execute(structs, vars, functions);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.label);
        str.append(" ");
        str.append(Token.getLabelValue(Token.LB));
        for (int i = 0; i < this.args.size(); i++) {
            if (i > 0) {
                str.append(Token.getLabelValue(Token.COMMA));
                str.append(" ");
            }
            str.append(this.args.get(i));
        }
        str.append(Token.getLabelValue(Token.RB));
        return str.toString();
    }
}

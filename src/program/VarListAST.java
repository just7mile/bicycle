package program;

import lexer.Token;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VarListAST extends AST implements DeclarationAST, StatementAST {
    private String type;
    private List<VariableAST> variables;

    public VarListAST(String type, int line, int column) {
        super(line, column);
        this.type = type;
        this.variables = new LinkedList<>();
    }

    public void addVariable(VariableAST v) {
        this.variables.add(v);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<VariableAST> getVariables() {
        return variables;
    }

    public void setVariables(List<VariableAST> variables) {
        this.variables = variables;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> funcs, List<AST> callStack) throws Exception {
        if (!types.containsKey(this.type)) {
            throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Type '" + this.type + "' does not exist!");
        }
        for (VariableAST v: this.variables) {
            if (vars.containsKey(v.getLabel())) {
                throw new Exception("Error at line " + v.getLine() + ", column " + v.getColumn() + ": variable '" + v.getLabel() + "' already exists!");
            }
            v.checkSemantics(types, vars, funcs, callStack);
            vars.put(v.getLabel(), v);
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> funcs) throws Exception {
        for (VariableAST v: this.variables) {
            vars.put(v.getLabel(), new ValueObject(v.execute(structs, vars, funcs)));
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.type);
        str.append(" ");
        for (int i = 0; i < this.variables.size(); i++) {
            if (i > 0) {
                str.append(Token.getLabelValue(Token.COMMA));
                str.append(" ");
            }
            str.append(this.variables.get(i).toString());
        }
        return str.toString();
    }
}

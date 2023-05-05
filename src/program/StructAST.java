package program;

import lexer.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * AST for struct.
 */
public class StructAST extends DeclarationAST {
    /**
     * Name of the struct.
     */
    private String label;

    /**
     * Fields of the struct.
     */
    private List<VarListAST> fields;

    public StructAST(String label, int line, int column) {
        super(line, column);
        this.label = label;
        this.fields = new LinkedList<>();
    }

    public void addField(VarListAST field) {
        this.fields.add(field);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<VarListAST> getFields() {
        return fields;
    }

    public void setFields(List<VarListAST> fields) {
        this.fields = fields;
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> varList, Map<String, FunctionAST> functions, List<AST> callStack) throws Exception {
        Map<String, VariableAST> vars = new HashMap<>(varList);
        for (VarListAST vs : this.fields) {
            vs.checkSemantics(types, vars, functions, callStack);
            for (VariableAST v : vs.getVariables()) {
                if (v.getValue() != null && v.getValue().isNewObjectCreation() && v.getValue().getLabel().equals(this.label)) {
                    throw new Exception("Error at line " + v.getValue().getLine() + ", column " + v.getValue().getColumn() + ": This expression will never terminate!");
                }
            }
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> functions) throws Exception {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(Token.getLabelValue(Token.STRUCT));
        str.append(" ");
        str.append(this.label);
        str.append(" ");
        str.append(Token.getLabelValue(Token.LP));
        str.append("\n");
        for (VarListAST v : this.fields) {
            str.append(v);
            str.append(Token.getLabelValue(Token.SEMICOLON));
            str.append("\n");
        }
        str.append(Token.getLabelValue(Token.RP));
        return str.toString();
    }
}

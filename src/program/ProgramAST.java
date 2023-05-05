package program;

import lexer.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The program AST class.
 * It is the root AST.
 */
public class ProgramAST extends AST {

    /**
     * List of struct, variables, and functions declarations.
     */
    private List<DeclarationAST> declarations;

    public ProgramAST(int line, int column) {
        super(line, column);
        this.declarations = new LinkedList<>();
    }

    public void addDeclaration(DeclarationAST d) {
        this.declarations.add(d);
    }

    public List<DeclarationAST> getDeclarations() {
        return declarations;
    }

    public void setDeclarations(List<DeclarationAST> declarations) {
        this.declarations = declarations;
    }

    public void semanticsCheck() throws Exception {
        // Start with the list of primitive types.
        // Custom types (structs) will be added in the order they are declared.
        Map<String, Map<String, String>> types = new HashMap<>() {{
            put(Token.getLabelValue(Token.BOOLEAN), null);
            put(Token.getLabelValue(Token.INTEGER), null);
            put(Token.getLabelValue(Token.DOUBLE), null);
            put(Token.getLabelValue(Token.STRING), null);
            put(Token.getLabelValue(Token.VOID), null);
        }};
        Map<String, VariableAST> vars = new HashMap<>();
        Map<String, FunctionAST> functions = new HashMap<>();
        List<AST> callStack = new LinkedList<>();
        checkSemantics(types, vars, functions, callStack);
    }

    public void execute() throws Exception {
        Map<String, StructAST> structs = new HashMap<>();
        Map<String, ValueObject> vars = new HashMap<>();
        Map<String, FunctionAST> functions = new HashMap<>();
        execute(structs, vars, functions);
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> functions, List<AST> callStack) throws Exception {
        for (DeclarationAST declaration : this.declarations) {
            if (declaration instanceof StructAST struct) {
                if (types.containsKey(struct.getLabel())) {
                    throw new Exception("Error at line " + struct.getLine() + ", column " + struct.getColumn() + ": Type '" + struct.getLabel() + "' already exists");
                }
                Map<String, String> items = new HashMap<>();
                for (VarListAST item : struct.getFields()) {
                    for (VariableAST v : item.getVariables()) {
                        items.put(v.getLabel(), v.getType());
                    }
                }
                types.put(struct.getLabel(), items);
                struct.checkSemantics(types, vars, functions, callStack);
            } else if (declaration instanceof VarListAST vs) {
                vs.checkSemantics(types, vars, functions, callStack);
            } else {
                FunctionAST func = (FunctionAST) declaration;
                if (functions.containsKey(func.getLabel())) {
                    throw new Exception("Error at line " + func.getLine() + ", column " + func.getColumn() + ": Function '" + func.getLabel() + "' already exists");
                }
                if (func.getLabel().equals("main") && func.getParams().size() > 0) {
                    throw new Exception("Error at line " + func.getParams().get(0).getLine() + ", column " + func.getParams().get(0).getColumn() + ": Function 'main' cannot have arguments!");
                }
                functions.put(func.getLabel(), func);
                func.checkSemantics(types, vars, functions, callStack);
            }
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> functions) throws Exception {
        boolean mainExists = false;
        for (DeclarationAST declaration : this.declarations) {
            if (declaration instanceof StructAST struct) {
                structs.put(struct.getLabel(), struct);
            } else if (declaration instanceof VarListAST vs) {
                vs.execute(structs, vars, functions);
            } else {
                FunctionAST func = (FunctionAST) declaration;
                functions.put(func.getLabel(), func);
                if (func.getLabel().equals("main")) {
                    mainExists = true;
                    func.execute(structs, vars, functions);
                }
            }
        }
        if (!mainExists) throw new Exception("Warning: function 'main()' is required in order to execute program...");

        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (DeclarationAST d : this.declarations) {
            str.append(d);
            str.append("\n\n");
        }
        return str.toString();
    }
}

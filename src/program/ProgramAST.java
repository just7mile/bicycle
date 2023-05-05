package program;

import lexer.Token;

import java.util.*;

public class ProgramAST extends AST {
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
        Map<String, Map<String, String>> types = new HashMap<String, Map<String, String>>() {{
            put(Token.getLabelValue(Token.BOOLEAN), null);
            put(Token.getLabelValue(Token.INTEGER), null);
            put(Token.getLabelValue(Token.DOUBLE), null);
            put(Token.getLabelValue(Token.STRING), null);
            put(Token.getLabelValue(Token.VOID), null);
        }};
        Map<String, VariableAST> vars = new HashMap<>();
        Map<String, FunctionAST> funcs = new HashMap<>();
        List<AST> callStack = new LinkedList<>();
        checkSemantics(types, vars, funcs, callStack);
    }

    public void execute() throws Exception {
        Map<String, StructAST> structs = new HashMap<>();
        Map<String, ValueObject> vars = new HashMap<>();
        Map<String, FunctionAST> funcs = new HashMap<>();
        execute(structs, vars, funcs);
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> funcs, List<AST> callStack) throws Exception {
        for (DeclarationAST declaration: this.declarations) {
            if (declaration instanceof StructAST) {
                StructAST struct = (StructAST) declaration;
                if (types.containsKey(struct.getLabel())) {
                    throw new Exception("Error at line " + struct.getLine() + ", column " + struct.getColumn() + ": Type '" + struct.getLabel() + "' already exists");
                }
                Map<String, String> items = new HashMap<>();
                for (VarListAST item: struct.getFields()) {
                    for (VariableAST v: item.getVariables()) {
                        items.put(v.getLabel(), v.getType());
                    }
                }
                types.put(struct.getLabel(), items);
                struct.checkSemantics(types, vars, funcs, callStack);
            } else if (declaration instanceof VarListAST) {
                VarListAST vs = (VarListAST) declaration;
                vs.checkSemantics(types, vars, funcs, callStack);
            } else {
                FunctionAST func = (FunctionAST) declaration;
                if (funcs.containsKey(func.getLabel())) {
                    throw new Exception("Error at line " + func.getLine() + ", column " + func.getColumn() + ": Function '" + func.getLabel() + "' already exists");
                }
                if (func.getLabel().equals("main") && func.getParams().size() > 0) {
                    throw new Exception("Error at line " + func.getParams().get(0).getLine() + ", column " + func.getParams().get(0).getColumn() + ": Function 'main' cannot have arguments!");
                }
                funcs.put(func.getLabel(), func);
                func.checkSemantics(types, vars, funcs, callStack);
            }
        }
        return null;
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> funcs) throws Exception {
        boolean mainExists = false;
        for (DeclarationAST declaration: this.declarations) {
            if (declaration instanceof StructAST) {
                StructAST struct = (StructAST) declaration;
                structs.put(struct.getLabel(), struct);
            } else if (declaration instanceof VarListAST) {
                VarListAST vs = (VarListAST) declaration;
                vs.execute(structs, vars, funcs);
            } else {
                FunctionAST func = (FunctionAST) declaration;
                funcs.put(func.getLabel(), func);
                if (func.getLabel().equals("main")) {
                    mainExists = true;
                    func.execute(structs, vars, funcs);
                }
            }
        }
        if (!mainExists) throw new Exception("Warning: function 'main()' is needed in order to execute program...");

        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (DeclarationAST d: this.declarations) {
            str.append(d.toString());
            str.append("\n\n");
        }
        return str.toString();
    }
}

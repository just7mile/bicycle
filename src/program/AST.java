package program;

import java.util.List;
import java.util.Map;

public abstract class AST {
    private int line, column;

    public AST(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    abstract String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> funcs, List<AST> callSTack) throws Exception;

    abstract Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> funcs) throws Exception;
}

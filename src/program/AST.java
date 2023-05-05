package program;

import java.util.List;
import java.util.Map;

/**
 * The base class for the Abstract Syntax Tree.
 */
public abstract class AST {
    /**
     * Line and column of the start of the AST.
     * Used to locate errors.
     */
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

    /**
     * Validates semantics.
     *
     * @param types     list of types declared so far. It is a map of the type name to the:
     *                  - map of field names to the field types if the type is struct.
     *                  - null otherwise.
     * @param vars      list of variables declared so far. It is a map of the variable name to the variable itself.
     * @param functions list of functions declared so far. It is a map of the function name to the function itself.
     * @param callStack current call stack.
     * @return type of the result.
     * @throws Exception in case of semantic errors.
     */
    abstract String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> functions, List<AST> callStack) throws Exception;

    /**
     * Executes the code.
     *
     * @param structs   list of structs declared so far. It is a map of the struct name to the struct itself.
     * @param vars      list of variables declared so far. It is a map of the variable name to the variable itself.
     * @param functions list of functions declared so far. It is a map of the function name to the function itself.
     * @return the result of execution.
     * @throws Exception in case of runtime errors.
     */
    abstract Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> functions) throws Exception;
}

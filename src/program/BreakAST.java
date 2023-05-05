package program;

import lexer.Token;

import java.util.List;
import java.util.Map;

/**
 * AST for loop breaks.
 */
public class BreakAST extends StatementAST {
    public BreakAST(int line, int column) {
        super(line, column);
    }

    @Override
    String checkSemantics(Map<String, Map<String, String>> types, Map<String, VariableAST> vars, Map<String, FunctionAST> functions, List<AST> callStack) throws Exception {
        if (callStack.get(0) instanceof ForLoopAST) {
            return Token.getLabelValue(Token.BREAK);
        }
        throw new Exception("Error at line " + this.getLine() + ", column " + this.getColumn() + ": Unexpected '" + this + "'!");
    }

    @Override
    Object execute(Map<String, StructAST> structs, Map<String, ValueObject> vars, Map<String, FunctionAST> functions) throws Exception {
        return this;
    }

    @Override
    public String toString() {
        return Token.getLabelValue(Token.BREAK);
    }
}

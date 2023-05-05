package program;

/**
 * Base class for statement ASTs.
 */
public abstract class StatementAST extends AST {
    public StatementAST(int line, int column) {
        super(line, column);
    }
}

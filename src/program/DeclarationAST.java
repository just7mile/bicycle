package program;

/**
 * Base class for declaration ASTs.
 */
public abstract class DeclarationAST extends StatementAST {
    public DeclarationAST(int line, int column) {
        super(line, column);
    }
}
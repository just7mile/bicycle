package parser;

import lexer.Token;
import program.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Parser to convert list of tokens into ASTs.
 */
public class Parser {

    /**
     * Converts tokens into AST.
     *
     * @param tokens list of tokens.
     * @return AST for the program.
     * @throws Exception for the parsing errors.
     */
    public ProgramAST parse(List<Token> tokens) throws Exception {
        ProgramAST program = new ProgramAST(0, 0);

        while (!tokens.isEmpty()) {
            program.addDeclaration(parseDeclaration(tokens));
        }

        return program;
    }

    /**
     * Parses a declaration.
     *
     * @param tokens list of tokens.
     * @return AST for the declaration.
     * @throws Exception for the parsing errors.
     */
    private DeclarationAST parseDeclaration(List<Token> tokens) throws Exception {
        DeclarationAST declaration;
        Token next = tokens.get(0);
        if (next.getLabel() == Token.STRUCT) {
            declaration = parseStruct(tokens);
        } else {
            if (tokens.size() < 2) {
                throwException(next);
            }
            if (tokens.size() < 3) {
                throwException(tokens.get(1));
            }
            next = tokens.get(2);
            if (next.getLabel() == Token.LB) {
                declaration = parseFunc(tokens);
            } else {
                declaration = parseVariableList(tokens);
            }
        }

        return declaration;
    }

    /**
     * Parses a struct.
     *
     * @param tokens list of tokens.
     * @return AST for the struct.
     * @throws Exception for the parsing errors.
     */
    private StructAST parseStruct(List<Token> tokens) throws Exception {
        tokens.remove(0); // struct

        Token next = tokens.remove(0); // <label>
        if (tokens.isEmpty() || Token.isIncorrectLabel(next)) {
            throwException(next);
        }

        StructAST structAST = new StructAST(next.getValue(), next.getLine(), next.getCol());

        next = tokens.remove(0); // {

        if (tokens.isEmpty() || next.getLabel() != Token.LP) {
            throwException(next);
        }

        while (!tokens.isEmpty()) {
            next = tokens.get(0);
            if (next.getLabel() == Token.RP) {
                break;
            }
            next = tokens.get(tokens.size() - 1);
            structAST.addField(parseVariableList(tokens));
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }
        tokens.remove(0); // }

        return structAST;
    }

    /**
     * Parses list of multiple variables declared in one line.
     *
     * @param tokens list of tokens.
     * @return AST for the list of variables.
     * @throws Exception for the parsing errors.
     */
    private VarListAST parseVariableList(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0); // <type>
        if (Token.isNotATypeToken(next) || next.getLabel() == Token.VOID || tokens.isEmpty()) {
            throwException(next);
        }
        VarListAST var = new VarListAST(next.getValue(), next.getLine(), next.getCol());

        while (!tokens.isEmpty()) {
            next = tokens.get(0);
            if (next.getLabel() == Token.SEMICOLON) {
                break;
            }
            if (next.getLabel() == Token.COMMA) {
                tokens.remove(0);
            }
            if (tokens.isEmpty()) {
                throwException(next);
            }
            next = tokens.get(tokens.size() - 1);
            var.addVariable(parseVariable(var.getType(), tokens, true, false));
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }
        tokens.remove(0); // ;

        return var;
    }

    /**
     * Parses a single variable declaration.
     *
     * @param tokens list of tokens.
     * @return AST for the variable.
     * @throws Exception for the parsing errors.
     */
    private VariableAST parseVariable(String type, List<Token> tokens, boolean semicolon, boolean rightBracket) throws Exception {
        Token next = tokens.remove(0);
        if (tokens.isEmpty() || Token.isIncorrectLabel(next)) {
            throwException(next);
        }

        VariableAST variable = new VariableAST(type, next.getValue(), next.getLine(), next.getCol());

        if (!tokens.isEmpty() && tokens.get(0).getLabel() == Token.ASSIGNMENT) {
            next = tokens.remove(0); // =
            if (next.getLabel() != Token.ASSIGNMENT || tokens.isEmpty()) {
                throwException(next);
            }
            variable.setValue(parseExpression(tokens, semicolon, true, rightBracket));
        }

        return variable;
    }

    /**
     * Parses a function.
     *
     * @param tokens list of tokens.
     * @return AST for the function.
     * @throws Exception for the parsing errors.
     */
    private FunctionAST parseFunc(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        if (tokens.isEmpty() || Token.isNotATypeToken(next)) {
            throwException(next);
        }

        FunctionAST func = new FunctionAST(next.getValue(), next.getLine(), next.getCol());

        next = tokens.remove(0);
        if (tokens.isEmpty() || Token.isIncorrectLabel(next)) {
            throwException(next);
        }
        func.setLabel(next.getValue());

        next = tokens.remove(0);
        if (tokens.isEmpty() || next.getLabel() != Token.LB) {
            throwException(next);
        }

        while (!tokens.isEmpty()) {
            next = tokens.get(0);
            if (next.getLabel() == Token.RB) {
                break;
            }
            if (next.getLabel() == Token.COMMA) {
                tokens.remove(0);
            }
            if (tokens.isEmpty()) {
                throwException(next);
            }
            Token type = tokens.remove(0);
            if (tokens.isEmpty() || Token.isNotATypeToken(type)) {
                throwException(type);
            }
            next = tokens.get(tokens.size() - 1);
            VariableAST param = parseVariable(type.getValue(), tokens, false, true);
            param.setParam(true);
            func.addParam(param);
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }
        tokens.remove(0); // )

        next = tokens.remove(0); // {
        if (tokens.isEmpty() || next.getLabel() != Token.LP) {
            throwException(next);
        }

        next = tokens.get(tokens.size() - 1);
        func.setStatements(parseStatements(tokens));
        if (tokens.isEmpty()) {
            throwException(next);
        }
        tokens.remove(0); // }

        return func;
    }

    /**
     * Parses a statement.
     *
     * @param tokens list of tokens.
     * @return AST for the statement.
     * @throws Exception for the parsing errors.
     */
    private List<StatementAST> parseStatements(List<Token> tokens) throws Exception {
        List<StatementAST> statements = new LinkedList<>();

        while (!tokens.isEmpty()) {
            Token statement = tokens.get(0);
            if (statement.getLabel() == Token.RP) {
                break;
            }
            if (tokens.size() < 2) {
                throwException(statement);
            }
            if (statement.getLabel() == Token.FOR) {
                statements.add(parseForLoop(tokens));
            } else if (statement.getLabel() == Token.IF || statement.getLabel() == Token.ELSEIF || statement.getLabel() == Token.ELSE) {
                statements.add(parseIfStatement(tokens));
            } else if (statement.getLabel() == Token.PRINTF) {
                statements.add(parsePrintf(tokens));
            } else if (statement.getLabel() == Token.RETURN) {
                statements.add(parseReturn(tokens));
            } else if (statement.getLabel() == Token.BREAK) {
                tokens.remove(0); // break
                statements.add(new BreakAST(statement.getLine(), statement.getCol()));
                if (tokens.isEmpty()) {
                    throwException(statement);
                }
                statement = tokens.remove(0);
                if (statement.getLabel() != Token.SEMICOLON) {
                    throwException(statement);
                }

            } else if (tokens.get(1).getLabel() == Token.ASSIGNMENT || tokens.get(1).getLabel() == Token.DOT) {
                statements.add(parseAssignment(tokens, Token.SEMICOLON));
            } else if (tokens.get(1).getLabel() == Token.LB) {
                statement = tokens.get(tokens.size() - 1);
                statements.add(parseCallFunc(tokens));
                if (tokens.isEmpty()) {
                    throwException(statement);
                }
                statement = tokens.remove(0);
                if (statement.getLabel() != Token.SEMICOLON) {
                    throwException(statement);
                }
            } else {
                statements.add(parseVariableList(tokens));
            }
        }

        return statements;
    }

    /**
     * Parses a for-loop.
     *
     * @param tokens list of tokens.
     * @return AST for the for-loop.
     * @throws Exception for the parsing errors.
     */
    private ForLoopAST parseForLoop(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        if (tokens.isEmpty()) {
            throwException(next);
        }

        ForLoopAST forLoop = new ForLoopAST(next.getLine(), next.getCol());

        next = tokens.remove(0);

        if (tokens.isEmpty() || next.getLabel() != Token.LB) {
            throwException(next);
        }

        next = tokens.get(0);
        if (next.getLabel() != Token.SEMICOLON) {
            if (tokens.size() == 1) {
                throwException(next);
            }
            int label = tokens.get(1).getLabel();
            next = tokens.get(tokens.size() - 1);
            if (label == Token.ASSIGNMENT || label == Token.DOT) {
                forLoop.setInitAssignment(parseAssignment(tokens, Token.SEMICOLON));
            } else {
                forLoop.setInitVariables(parseVariableList(tokens));
            }

        } else {
            tokens.remove(0);
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }

        next = tokens.get(0);
        if (next.getLabel() != Token.SEMICOLON) {
            next = tokens.get(tokens.size() - 1);
            forLoop.setCondition(parseCompExpression(tokens, true, false));

            if (tokens.isEmpty()) {
                throwException(next);
            }
            next = tokens.remove(0);
            if (next.getLabel() != Token.SEMICOLON) {
                throwException(next);
            }

        } else {
            tokens.remove(0);
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }

        next = tokens.get(0);
        if (next.getLabel() != Token.RB) {
            next = tokens.get(tokens.size() - 1);
            forLoop.setIncrement(parseAssignment(tokens, Token.RB));
        } else {
            tokens.remove(0);
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }

        next = tokens.remove(0); // {
        if (tokens.isEmpty() || next.getLabel() != Token.LP) {
            throwException(next);
        }

        next = tokens.get(tokens.size() - 1);
        forLoop.setStatements(parseStatements(tokens));
        if (tokens.isEmpty()) {
            throwException(next);
        }
        tokens.remove(0); // }

        return forLoop;
    }

    /**
     * Parses a if-statement.
     *
     * @param tokens list of tokens.
     * @return AST for the if-statement.
     * @throws Exception for the parsing errors.
     */
    private IfStatementAST parseIfStatement(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        if (tokens.isEmpty()) {
            throwException(next);
        }
        IfStatementAST ifStatement = new IfStatementAST(next.getLabel(), next.getLine(), next.getCol());

        if (next.getLabel() != Token.ELSE) {
            next = tokens.remove(0);
            if (tokens.isEmpty() || next.getLabel() != Token.LB) {
                throwException(next);
            }
            next = tokens.get(0);
            if (next.getLabel() != Token.RB) {
                next = tokens.get(tokens.size() - 1);
                ifStatement.setCondition(parseCompExpression(tokens, false, true));

                if (tokens.isEmpty()) {
                    throwException(next);
                }
                next = tokens.remove(0);
                if (next.getLabel() != Token.RB) {
                    throwException(next);
                }
            }
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }

        next = tokens.remove(0); // {
        if (tokens.isEmpty() || next.getLabel() != Token.LP) {
            throwException(next);
        }

        next = tokens.get(tokens.size() - 1);
        ifStatement.setStatements(parseStatements(tokens));
        if (tokens.isEmpty()) {
            throwException(next);
        }
        tokens.remove(0); // }

        return ifStatement;
    }

    /**
     * Parses a printf.
     *
     * @param tokens list of tokens.
     * @return AST for the printf.
     * @throws Exception for the parsing errors.
     */
    private PrintfAST parsePrintf(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        PrintfAST printfAST = new PrintfAST(next.getLine(), next.getCol());

        if (tokens.isEmpty()) {
            throwException(next);
        }

        next = tokens.remove(0);
        if (tokens.isEmpty() || next.getLabel() != Token.LB) {
            throwException(next);
        }
        next = tokens.get(0);
        if (next.getLabel() != Token.RB) {
            next = tokens.get(tokens.size() - 1);
            printfAST.setExpression(parseExpression(tokens, false, false, true));
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }
        next = tokens.remove(0);
        if (tokens.isEmpty() || next.getLabel() != Token.RB) {
            throwException(next);
        }

        next = tokens.remove(0);
        if (next.getLabel() != Token.SEMICOLON) {
            throwException(next);
        }

        return printfAST;
    }

    /**
     * Parses a return statement.
     *
     * @param tokens list of tokens.
     * @return AST for the return statement.
     * @throws Exception for the parsing errors.
     */
    private ReturnAST parseReturn(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        ReturnAST returnAST = new ReturnAST(next.getLine(), next.getCol());

        if (tokens.isEmpty()) {
            throwException(next);
        }

        next = tokens.get(0);
        if (next.getLabel() != Token.SEMICOLON) {
            next = tokens.get(tokens.size() - 1);
            returnAST.setExpression(parseExpression(tokens, true, false, false));
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }

        next = tokens.remove(0);
        if (next.getLabel() != Token.SEMICOLON) {
            throwException(next);
        }

        return returnAST;
    }

    /**
     * Parses an assignment.
     *
     * @param tokens list of tokens.
     * @return AST for the assignment.
     * @throws Exception for the parsing errors.
     */
    private AssignmentAST parseAssignment(List<Token> tokens, int terminator) throws Exception {
        Token next = tokens.remove(0);
        Token label = next;
        if (Token.isIncorrectLabel(label) || tokens.isEmpty()) {
            throwException(label);
        }

        String field = null;
        if (tokens.get(0).getLabel() == Token.DOT) {
            next = tokens.remove(0); // .
            if (tokens.isEmpty()) {
                throwException(next);
            }

            next = tokens.remove(0);
            if (tokens.isEmpty() || Token.isIncorrectLabel(next)) {
                throwException(next);
            }
            field = next.getValue();
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }
        next = tokens.remove(0); // =
        if (tokens.isEmpty() || next.getLabel() != Token.ASSIGNMENT) {
            throwException(next);
        }

        AssignmentAST assignment = new AssignmentAST(label.getValue(), field, label.getLine(), label.getCol());

        next = tokens.get(tokens.size() - 1);
        assignment.setExpression(parseExpression(tokens, terminator == Token.SEMICOLON, false, terminator == Token.RB));
        if (tokens.isEmpty()) {
            throwException(next);
        }

        next = tokens.remove(0);
        if (next.getLabel() != terminator) {
            throwException(next);
        }

        return assignment;
    }

    /**
     * Parses a function call.
     *
     * @param tokens list of tokens.
     * @return AST for the function call.
     * @throws Exception for the parsing errors.
     */
    private CallFuncAST parseCallFunc(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        if (tokens.isEmpty() || Token.isIncorrectLabel(next)) {
            throwException(next);
        }

        CallFuncAST func = new CallFuncAST(next.getValue(), next.getLine(), next.getCol());

        next = tokens.remove(0); // (
        if (tokens.isEmpty() || next.getLabel() != Token.LB) {
            throwException(next);
        }

        while (!tokens.isEmpty()) {
            next = tokens.get(0);
            if (next.getLabel() == Token.RB) {
                break;
            }
            if (next.getLabel() == Token.COMMA) {
                tokens.remove(0);
            }
            if (tokens.isEmpty()) {
                throwException(next);
            }
            next = tokens.get(tokens.size() - 1);
            func.addArg(parseExpression(tokens, false, true, true));
        }

        if (tokens.isEmpty()) {
            throwException(next);
        }
        tokens.remove(0); // )

        return func;
    }

    /**
     * Parses a simple expression.
     *
     * @param tokens list of tokens.
     * @return AST for the expression.
     * @throws Exception for the parsing errors.
     */
    private ExpressionAST parseExpression(List<Token> tokens, boolean semicolon, boolean comma, boolean rightBracket) throws Exception {
        List<Token> tokenList = new LinkedList<>();
        Token next = null;
        int counter = 0;

        while (!tokens.isEmpty()) {
            next = tokens.remove(0);
            if (counter == 0 && ((semicolon && next.getLabel() == Token.SEMICOLON)
                    || (comma && next.getLabel() == Token.COMMA) || (rightBracket && next.getLabel() == Token.RB))) {
                break;
            }

            if (next.getLabel() == Token.LB) {
                counter++;
            } else if (next.getLabel() == Token.RB) {
                counter--;
            }
            tokenList.add(next);
            if (!Token.isExpressionToken(next)) {
                throwException(next);
            }
        }

        ExpressionAST result = parseFullExpression(tokenList);
        tokens.add(0, next); // Return terminator

        return result;
    }

    /**
     * Parses a complex expression.
     *
     * @param tokens list of tokens.
     * @return AST for the expression.
     * @throws Exception for the parsing errors.
     */
    private ExpressionAST parseFullExpression(List<Token> tokens) throws Exception {
        int counter = 0;
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            if (token.getLabel() == Token.RB) {
                counter++;
            } else if (token.getLabel() == Token.LB) {
                counter--;
            } else if (counter == 0) {
                int label = token.getLabel();
                if (label == Token.ADDITION || label == Token.SUBTRACTION || label == Token.MOD) {
                    List<Token> leftTokens = new LinkedList<>();
                    for (int j = 0; j < i; j++) {
                        leftTokens.add(tokens.remove(0));
                    }
                    tokens.remove(0); // remove binOp
                    if (tokens.isEmpty() || leftTokens.isEmpty()) {
                        throwException(token);
                    }

                    ExpressionAST expr = new ExpressionAST(label, token.getLine(), token.getCol());
                    expr.setLeft(parseFullExpression(leftTokens));
                    expr.setRight(parseProduction(tokens));

                    return expr;
                }
            }
        }

        return parseProduction(tokens);
    }

    /**
     * Parses a production expression.
     *
     * @param tokens list of tokens.
     * @return AST for the expression.
     * @throws Exception for the parsing errors.
     */
    private ExpressionAST parseProduction(List<Token> tokens) throws Exception {
        int counter = 0;
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            if (token.getLabel() == Token.RB) {
                counter++;
            } else if (token.getLabel() == Token.LB) {
                counter--;
            } else if (counter == 0) {
                int label = token.getLabel();
                if (label == Token.MULTIPLICATION || label == Token.DIVISION) {
                    List<Token> leftTokens = new LinkedList<>();
                    for (int j = 0; j < i; j++) {
                        leftTokens.add(tokens.remove(0));
                    }
                    tokens.remove(0); // remove primOp
                    if (tokens.isEmpty() || leftTokens.isEmpty()) {
                        throwException(token);
                    }

                    ExpressionAST expr = new ExpressionAST(label, token.getLine(), token.getCol());
                    expr.setLeft(parseProduction(leftTokens));
                    expr.setRight(parsePrimary(tokens));

                    return expr;
                }
            }
        }
        return parsePrimary(tokens);
    }

    /**
     * Parses primary part in a production expression.
     *
     * @param tokens list of tokens.
     * @return AST for the expression.
     * @throws Exception for the parsing errors.
     */
    private ExpressionAST parsePrimary(List<Token> tokens) throws Exception {
        Token next = tokens.get(0);
        if (next.getLabel() == Token.LB) {
            tokens.remove(0); // (
            if (tokens.isEmpty()) {
                throwException(next);
            }
            next = tokens.remove(tokens.size() - 1); // Remove )
            if (tokens.isEmpty() || next.getLabel() != Token.RB) {
                throwException(next);
            }
            return parseFullExpression(tokens);
        }

        if (next.getLabel() == Token.NEW) {
            ExpressionAST expr = new ExpressionAST(true, next.getLine(), next.getCol());
            tokens.remove(0); // new
            if (tokens.isEmpty()) {
                throwException(next);
            }
            next = tokens.remove(0); // <label>
            if (tokens.isEmpty() || Token.isIncorrectLabel(next)) {
                throwException(next);
            }
            expr.setLabel(next.getValue());
            next = tokens.remove(0); // (
            if (tokens.isEmpty() || next.getLabel() != Token.LB) {
                throwException(next);
            }
            next = tokens.remove(0); // )
            if (next.getLabel() != Token.RB) {
                throwException(next);
            }
            return expr;
        }

        if (next.getLabel() == Token.NULL) {
            return new ExpressionAST(next.getLine(), next.getCol(), true);
        }

        if (tokens.size() > 1 && tokens.get(1).getLabel() == Token.LB) {
            return new ExpressionAST(parseCallFunc(tokens), next.getLine(), next.getCol());
        }

        if (tokens.size() == 3 && tokens.get(1).getLabel() == Token.DOT) {
            next = tokens.remove(0); // <label>
            if (Token.isIncorrectLabel(next)) {
                throwException(next);
            }
            tokens.remove(0); // .
            Token fieldName = tokens.remove(0);
            if (Token.isIncorrectLabel(fieldName)) {
                throwException(fieldName);
            }
            return new ExpressionAST(next.getValue(), fieldName.getValue(), next.getLine(), next.getCol());
        }

        Object value;
        if (next.getValue().startsWith("\"") && next.getValue().endsWith("\"")) {
            value = next.getValue().substring(1, next.getValue().length() - 1);
        } else if (next.getValue().equals("true")) {
            value = true;
        } else if (next.getValue().equals("false")) {
            value = false;
        } else {
            try {
                value = Integer.parseInt(next.getValue());
            } catch (Exception e) {
                try {
                    value = Double.parseDouble(next.getValue());
                } catch (Exception e1) {
                    if (Token.isIncorrectLabel(next)) {
                        throwException(next);
                    }
                    return new ExpressionAST(next.getValue(), next.getLine(), next.getCol());
                }
            }
        }

        return new ExpressionAST(value, next.getLine(), next.getCol());
    }

    /**
     * Parses a simple comparison expression.
     *
     * @param tokens list of tokens.
     * @return AST for the expression.
     * @throws Exception for the parsing errors.
     */
    private ComparisonAST parseCompExpression(List<Token> tokens, boolean semicolon, boolean rightBracket) throws Exception {
        List<Token> tokenList = new LinkedList<>();
        Token next = null;
        int counter = 0;

        while (!tokens.isEmpty()) {
            next = tokens.remove(0);
            if (counter == 0 && ((semicolon && next.getLabel() == Token.SEMICOLON)
                    || (rightBracket && next.getLabel() == Token.RB))) {
                break;
            }
            if (rightBracket) {
                if (next.getLabel() == Token.LB) {
                    counter++;
                } else if (next.getLabel() == Token.RB) {
                    counter--;
                }
            }
            tokenList.add(next);
            if (!Token.isCompExpressionToken(next)) {
                throwException(next);
            }
        }

        ComparisonAST result = parseFullCompExpression(tokenList);
        tokens.add(0, next); // Return terminator

        return result;
    }

    /**
     * Parses a complex comparison expression.
     *
     * @param tokens list of tokens.
     * @return AST for the expression.
     * @throws Exception for the parsing errors.
     */
    private ComparisonAST parseFullCompExpression(List<Token> tokens) throws Exception {
        int counter = 0;
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            if (token.getLabel() == Token.RB) {
                counter++;
            } else if (token.getLabel() == Token.LB) {
                counter--;
            } else if (counter == 0) {
                int label = token.getLabel();
                if (label == Token.AND || label == Token.OR) {
                    List<Token> leftTokens = new LinkedList<>();
                    for (int j = 0; j < i; j++) {
                        leftTokens.add(tokens.remove(0));
                    }
                    tokens.remove(0); // remove compConcatOp
                    if (tokens.isEmpty() || leftTokens.isEmpty()) {
                        throwException(token);
                    }

                    ComparisonAST comparision = new ComparisonAST(label, token.getLine(), token.getCol());
                    comparision.setLeft(parseFullCompExpression(leftTokens));
                    comparision.setRight(parseCompTerm(tokens));

                    return comparision;
                }
            }
        }

        return parseCompTerm(tokens);
    }

    /**
     * Parses a comparison term.
     *
     * @param tokens list of tokens.
     * @return AST for the expression.
     * @throws Exception for the parsing errors.
     */
    private ComparisonAST parseCompTerm(List<Token> tokens) throws Exception {
        Token next = tokens.get(0);
        if (next.getLabel() == Token.LB) {
            tokens.remove(0);
            if (tokens.isEmpty()) {
                throwException(next);
            }
            next = tokens.remove(tokens.size() - 1);
            if (next.getLabel() != Token.RB || tokens.isEmpty()) {
                throwException(next);
            }

            return parseFullCompExpression(tokens);
        }

        if (next.getLabel() == Token.NEGATION) {
            ComparisonAST comparison = new ComparisonAST(next.getLabel(), next.getLine(), next.getCol());
            tokens.remove(0); // remove '!'
            comparison.setRight(parseFullCompExpression(tokens));

            return comparison;
        }

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            int label = token.getLabel();
            if (label == Token.EQUAL || label == Token.NOT_EQUAL || label == Token.LESS || label == Token.MORE || label == Token.LESS_EQUAL || label == Token.MORE_EQUAL) {
                List<Token> leftTokens = new LinkedList<>();
                for (int j = 0; j < i; j++) {
                    leftTokens.add(tokens.remove(0));
                }
                tokens.remove(0); // remove compOp
                if (tokens.isEmpty() || leftTokens.isEmpty()) {
                    throwException(token);
                }

                ComparisonAST comparison = new ComparisonAST(label, token.getLine(), token.getCol());
                comparison.setLeftExpr(parseFullExpression(leftTokens));
                comparison.setRightExpr(parseFullExpression(tokens));

                return comparison;
            }
        }

        ComparisonAST comparison = new ComparisonAST(next.getLine(), next.getCol());
        comparison.setRightExpr(parseFullExpression(tokens));
        return comparison;
    }

    private void throwException(Token token) throws Exception {
        throw new Exception("Syntax error on line: " + token.getLine() + ", column: " + token.getCol());
    }
}
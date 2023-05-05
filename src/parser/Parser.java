package parser;

import lexer.Token;
import program.*;

import java.util.LinkedList;
import java.util.List;

public class Parser {

    public ProgramAST parse(List<Token> tokens) throws Exception {
        ProgramAST program = new ProgramAST(0, 0);

        while (!tokens.isEmpty()) {
            program.addDeclaration(parseDecl(tokens));
        }

        return program;
    }

    private DeclarationAST parseDecl(List<Token> tokens) throws Exception{
        DeclarationAST decl;
        Token next = tokens.get(0);
        if (next.getLabel() == Token.STRUCT) {
            decl = parseStruct(tokens);
        } else {
            if (tokens.size() < 2) exception(next);
            if (tokens.size() < 3) exception(tokens.get(1));
            next = tokens.get(2);
            if (next.getLabel() == Token.LB) decl = parseFunc(tokens);
            else decl = parseVariableList(tokens);
        }

        return decl;
    }

    private StructAST parseStruct(List<Token> tokens) throws Exception {
        tokens.remove(0); // struct

        Token next = tokens.remove(0); // <label>
        if (tokens.isEmpty() || Token.isIncorrectLabel(next)) exception(next);

        StructAST structAST = new StructAST(next.getValue(), next.getLine(), next.getCol());

        next = tokens.remove(0); // {

        if (tokens.isEmpty() || next.getLabel() != Token.LP) exception(next);

        while(!tokens.isEmpty()) {
            next = tokens.get(0);
            if (next.getLabel() == Token.RP) break;
            next = tokens.get(tokens.size() - 1);
            structAST.addField(parseVariableList(tokens));
        }

        if (tokens.isEmpty()) exception(next);
        tokens.remove(0); // }

        return structAST;
    }

    private VarListAST parseVariableList(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0); // <type>
        if (!Token.isType(next) || next.getLabel() == Token.VOID || tokens.isEmpty()) exception(next);
        VarListAST var = new VarListAST(next.getValue(), next.getLine(), next.getCol());

        while(!tokens.isEmpty()) {
            next = tokens.get(0);
            if (next.getLabel() == Token.SEMICOLON) break;
            if (next.getLabel() == Token.COMMA) tokens.remove(0);
            if (tokens.isEmpty()) exception(next);
            next = tokens.get(tokens.size() - 1);
            var.addVariable(parseVariable(var.getType(), tokens, true, true, false));
        }

        if (tokens.isEmpty()) exception(next);
        tokens.remove(0); // ;

        return var;
    }

    private VariableAST parseVariable(String type, List<Token> tokens, boolean semicolon, boolean comma, boolean rightBracket) throws Exception {
        Token next = tokens.remove(0);
        if (tokens.isEmpty() || Token.isIncorrectLabel(next)) exception(next);

        VariableAST variable = new VariableAST(type, next.getValue(), next.getLine(), next.getCol());

        if (!tokens.isEmpty() && tokens.get(0).getLabel() == Token.ASSIGNMENT) {
            next = tokens.remove(0); // =
            if (next.getLabel() != Token.ASSIGNMENT || tokens.isEmpty()) exception(next);
            variable.setValue(parseExpression(tokens, semicolon, comma, rightBracket));
        }

        return variable;
    }

    private FunctionAST parseFunc(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        if (tokens.isEmpty() || !Token.isType(next)) exception(next);

        FunctionAST func = new FunctionAST(next.getValue(), next.getLine(), next.getCol());

        next = tokens.remove(0);
        if (tokens.isEmpty() || Token.isIncorrectLabel(next)) exception(next);
        func.setLabel(next.getValue());

        next = tokens.remove(0);
        if (tokens.isEmpty() || next.getLabel() != Token.LB) exception(next);

        while (!tokens.isEmpty()) {
            next = tokens.get(0);
            if (next.getLabel() == Token.RB) break;
            if (next.getLabel() == Token.COMMA) tokens.remove(0);
            if (tokens.isEmpty()) exception(next);
            Token type = tokens.remove(0);
            if (tokens.isEmpty() || !Token.isType(type)) exception(type);
            next = tokens.get(tokens.size() - 1);
            VariableAST param = parseVariable(type.getValue(), tokens, false, true, true);
            param.setParam(true);
            func.addParam(param);
        }

        if (tokens.isEmpty()) exception(next);
        tokens.remove(0); // )

        next = tokens.remove(0); // {
        if (tokens.isEmpty() || next.getLabel() != Token.LP) exception(next);

        next = tokens.get(tokens.size() - 1);
        func.setStatements(parseStatements(tokens));
        if (tokens.isEmpty()) exception(next);
        tokens.remove(0); // }

        return func;
    }

    private List<StatementAST> parseStatements(List<Token> tokens) throws Exception {
        List<StatementAST> statements = new LinkedList<>();

        while (!tokens.isEmpty()) {
            Token statement = tokens.get(0);
            if (statement.getLabel() == Token.RP) break;
            if (tokens.size() < 2) exception(statement);
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
                if (tokens.isEmpty()) exception(statement);
                statement = tokens.remove(0);
                if (statement.getLabel() != Token.SEMICOLON) exception(statement);

            } else if (tokens.get(1).getLabel() == Token.ASSIGNMENT || tokens.get(1).getLabel() == Token.DOT) {
                statements.add(parseAssignment(tokens, Token.SEMICOLON));

            } else if (tokens.get(1).getLabel() == Token.LB) {
                statement = tokens.get(tokens.size() - 1);
                statements.add(parseCallFunc(tokens));
                if (tokens.isEmpty()) exception(statement);
                statement = tokens.remove(0);
                if (statement.getLabel() != Token.SEMICOLON) exception(statement);

            } else statements.add(parseVariableList(tokens));
        }

        return statements;
    }

    private ForLoopAST parseForLoop(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        if (tokens.isEmpty()) exception(next);

        ForLoopAST forLoop = new ForLoopAST(next.getLine(), next.getCol());

        next = tokens.remove(0);

        if (tokens.isEmpty() || next.getLabel() != Token.LB) exception(next);

        next = tokens.get(0);
        if (next.getLabel() != Token.SEMICOLON) {
            if (tokens.size() == 1) exception(next);
            int label = tokens.get(1).getLabel();
            next = tokens.get(tokens.size() - 1);
            if (label == Token.ASSIGNMENT || label == Token.DOT) forLoop.setInitAssignment(parseAssignment(tokens, Token.SEMICOLON));
            else forLoop.setInitVariables(parseVariableList(tokens));

        } else tokens.remove(0);

        if (tokens.isEmpty()) exception(next);

        next = tokens.get(0);
        if (next.getLabel() != Token.SEMICOLON) {
            next = tokens.get(tokens.size() - 1);
            forLoop.setCondition(parseCompExpression(tokens, true, false));

            if (tokens.isEmpty()) exception(next);
            next = tokens.remove(0);
            if (next.getLabel() != Token.SEMICOLON) exception(next);

        } else tokens.remove(0);

        if (tokens.isEmpty()) exception(next);

        next = tokens.get(0);
        if (next.getLabel() != Token.RB) {
            next = tokens.get(tokens.size() - 1);
            forLoop.setIncrement(parseAssignment(tokens, Token.RB));
        } else tokens.remove(0);

        if (tokens.isEmpty()) exception(next);

        next = tokens.remove(0); // {
        if (tokens.isEmpty() || next.getLabel() != Token.LP) exception(next);

        next = tokens.get(tokens.size() - 1);
        forLoop.setStatements(parseStatements(tokens));
        if (tokens.isEmpty()) exception(next);
        tokens.remove(0); // }

        return forLoop;
    }

    private IfStatementAST parseIfStatement(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        if (tokens.isEmpty()) exception(next);
        IfStatementAST ifStatement = new IfStatementAST(next.getLabel(), next.getLine(), next.getCol());

        if (next.getLabel() != Token.ELSE) {
            next = tokens.remove(0);
            if (tokens.isEmpty() || next.getLabel() != Token.LB) exception(next);
            next = tokens.get(0);
            if (next.getLabel() != Token.RB) {
                next = tokens.get(tokens.size() - 1);
                ifStatement.setCondition(parseCompExpression(tokens, false, true));

                if (tokens.isEmpty()) exception(next);
                next = tokens.remove(0);
                if (next.getLabel() != Token.RB) exception(next);
            }
        }

        if (tokens.isEmpty()) exception(next);

        next = tokens.remove(0); // {
        if (tokens.isEmpty() || next.getLabel() != Token.LP) exception(next);

        next = tokens.get(tokens.size() - 1);
        ifStatement.setStatements(parseStatements(tokens));
        if (tokens.isEmpty()) exception(next);
        tokens.remove(0); // }

        return ifStatement;
    }

    private PrintfAST parsePrintf(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        PrintfAST printfAST = new PrintfAST(next.getLine(), next.getCol());

        if (tokens.isEmpty()) exception(next);

        next = tokens.remove(0);
        if (tokens.isEmpty() || next.getLabel() != Token.LB) exception(next);
        next = tokens.get(0);
        if (next.getLabel() != Token.RB) {
            next = tokens.get(tokens.size() - 1);
            printfAST.setExpression(parseExpression(tokens, false, false, true));
        }

        if (tokens.isEmpty()) exception(next);
        next = tokens.remove(0);
        if (tokens.isEmpty() || next.getLabel() != Token.RB) exception(next);

        next = tokens.remove(0);
        if (next.getLabel() != Token.SEMICOLON) exception(next);

        return printfAST;
    }

    private ReturnAST parseReturn(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        ReturnAST returnAST = new ReturnAST(next.getLine(), next.getCol());

        if (tokens.isEmpty()) exception(next);

        next = tokens.get(0);
        if (next.getLabel() != Token.SEMICOLON) {
            next = tokens.get(tokens.size() - 1);
            returnAST.setExpression(parseExpression(tokens, true, false, false));
        }

        if (tokens.isEmpty()) exception(next);

        next = tokens.remove(0);
        if (next.getLabel() != Token.SEMICOLON) exception(next);

        return returnAST;
    }

    private AssignmentAST parseAssignment(List<Token> tokens, int terminator) throws Exception {
        Token next = tokens.remove(0);
        Token label = next;
        if (Token.isIncorrectLabel(label) || tokens.isEmpty()) exception(label);

        String field = null;
        if (tokens.get(0).getLabel() == Token.DOT) {
            next = tokens.remove(0); // .
            if (tokens.isEmpty()) exception(next);

            next = tokens.remove(0);
            if (tokens.isEmpty() || Token.isIncorrectLabel(next)) exception(next);
            field = next.getValue();
        }

        if (tokens.isEmpty()) exception(next);
        next = tokens.remove(0); // =
        if (tokens.isEmpty() || next.getLabel() != Token.ASSIGNMENT) exception(next);

        AssignmentAST assignment = new AssignmentAST(label.getValue(), field, label.getLine(), label.getCol());

        next = tokens.get(tokens.size() - 1);
        assignment.setExpression(parseExpression(tokens, terminator == Token.SEMICOLON, false, terminator == Token.RB));
        if (tokens.isEmpty()) exception(next);

        next = tokens.remove(0);
        if (next.getLabel() != terminator) exception(next);

        return assignment;
    }

    private CallFuncAST parseCallFunc(List<Token> tokens) throws Exception {
        Token next = tokens.remove(0);
        if (tokens.isEmpty() || Token.isIncorrectLabel(next)) exception(next);

        CallFuncAST func = new CallFuncAST(next.getValue(), next.getLine(), next.getCol());

        next = tokens.remove(0); // (
        if (tokens.isEmpty() || next.getLabel() != Token.LB) exception(next);

        while (!tokens.isEmpty()) {
            next = tokens.get(0);
            if (next.getLabel() == Token.RB) break;
            if (next.getLabel() == Token.COMMA) tokens.remove(0);
            if (tokens.isEmpty()) exception(next);
            next = tokens.get(tokens.size() - 1);
            func.addArg(parseExpression(tokens, false, true, true));
        }

        if (tokens.isEmpty()) exception(next);
        tokens.remove(0); // )

        return func;
    }

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

            if (next.getLabel() == Token.LB) counter++;
            if (next.getLabel() == Token.RB) counter--;
            tokenList.add(next);
            if (!Token.isExpressionToken(next)) exception(next);
        }

        ExpressionAST  result = parseFullExpression(tokenList);
        tokens.add(0, next); // Return terminator

        return result;
    }

    private ExpressionAST parseFullExpression(List<Token> tokens) throws Exception {
        int counter = 0;
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            if (token.getLabel() == Token.RB) counter++;
            else if (token.getLabel() == Token.LB) counter--;
            else if (counter == 0) {
                int label = token.getLabel();
                if (label == Token.ADDITION || label == Token.SUBTRACTION || label == Token.MOD) {
                    List<Token> leftTokens = new LinkedList<>();
                    for (int j = 0; j < i; j++) leftTokens.add(tokens.remove(0));
                    tokens.remove(0); // remove binOp
                    if (tokens.isEmpty() || leftTokens.isEmpty()) exception(token);

                    ExpressionAST expr = new ExpressionAST(label, token.getLine(), token.getCol());
                    expr.setLeft(parseFullExpression(leftTokens));
                    expr.setRight(parseProduction(tokens));

                    return expr;
                }
            }
        }

        return parseProduction(tokens);
    }

    private ExpressionAST parseProduction(List<Token> tokens) throws Exception {
        int counter = 0;
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            if (token.getLabel() == Token.RB) counter++;
            else if (token.getLabel() == Token.LB) counter--;
            else if (counter == 0) {
                int label = token.getLabel();
                if (label == Token.MULTIPLICATION || label == Token.DIVISION) {
                    List<Token> leftTokens = new LinkedList<>();
                    for (int j = 0; j < i; j++) leftTokens.add(tokens.remove(0));
                    tokens.remove(0); // remove primOp
                    if (tokens.isEmpty() || leftTokens.isEmpty()) exception(token);

                    ExpressionAST expr = new ExpressionAST(label, token.getLine(), token.getCol());
                    expr.setLeft(parseProduction(leftTokens));
                    expr.setRight(parsePrimary(tokens));

                    return expr;
                }
            }
        }
        return parsePrimary(tokens);
    }

    private ExpressionAST parsePrimary(List<Token> tokens) throws Exception {
        Token next = tokens.get(0);
        if (next.getLabel() == Token.LB) {
            tokens.remove(0); // (
            if (tokens.isEmpty()) exception(next);
            next = tokens.remove(tokens.size() - 1); // Remove )
            if (tokens.isEmpty() || next.getLabel() != Token.RB) exception(next);
            return parseFullExpression(tokens);
        }

        if (next.getLabel() == Token.NEW) {
            ExpressionAST expr = new ExpressionAST(true, next.getLine(), next.getCol());
            tokens.remove(0); // new
            if (tokens.isEmpty()) exception(next);
            next = tokens.remove(0); // <label>
            if (tokens.isEmpty() || Token.isIncorrectLabel(next)) exception(next);
            expr.setLabel(next.getValue());
            next = tokens.remove(0); // (
            if (tokens.isEmpty() || next.getLabel() != Token.LB) exception(next);
            next = tokens.remove(0); // Remove )
            if (next.getLabel() != Token.RB) exception(next);
            return expr;
        }

        if (next.getLabel() == Token.NULL) {
            return new ExpressionAST(next.getLine(), next.getCol(), true);
        }

        if (tokens.size() > 1 && tokens.get(1).getLabel() == Token.LB) {
            return new ExpressionAST(parseCallFunc(tokens), next.getLine(), next.getCol());
        }

        if (tokens.size()  == 3 && tokens.get(1).getLabel() == Token.DOT) {
            next = tokens.remove(0); // <label>
            if (Token.isIncorrectLabel(next)) exception(next);
            tokens.remove(0); // .
            Token fieldName = tokens.remove(0);
            if (Token.isIncorrectLabel(fieldName)) exception(fieldName);
            return new ExpressionAST(next.getValue(), fieldName.getValue(), next.getLine(), next.getCol());
        }

        Object value;
        if (next.getValue().startsWith("\"") && next.getValue().endsWith("\"")) value = next.getValue().substring(1, next.getValue().length() - 1);
        else if (next.getValue().equals("true")) value = true;
        else if (next.getValue().equals("false")) value = false;
        else {
            try {
                value = Integer.parseInt(next.getValue());
            } catch (Exception e) {
                try {
                    value = Double.parseDouble(next.getValue());
                } catch (Exception e1) {
                    if (Token.isIncorrectLabel(next)) exception(next);
                    return new ExpressionAST(next.getValue(), next.getLine(), next.getCol());
                }
            }
        }

        return new ExpressionAST(value, next.getLine(), next.getCol());
    }

    private ComparisionAST parseCompExpression(List<Token> tokens, boolean semicolon, boolean rightBracket) throws Exception {
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
                if (next.getLabel() == Token.LB) counter++;
                if (next.getLabel() == Token.RB) counter--;
            }
            tokenList.add(next);
            if (!Token.isCompExpressionToken(next)) exception(next);
        }

        ComparisionAST result = parseFullCompExpression(tokenList);
        tokens.add(0, next); // Return terminator

        return  result;
    }

    private ComparisionAST parseFullCompExpression(List<Token> tokens) throws Exception {
        int counter = 0;
        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);
            if (token.getLabel() == Token.RB) counter++;
            else if (token.getLabel() == Token.LB) counter--;
            else if (counter == 0) {
                int label = token.getLabel();
                if (label == Token.AND || label == Token.OR) {
                    List<Token> leftTokens = new LinkedList<>();
                    for (int j = 0; j < i; j++) leftTokens.add(tokens.remove(0));
                    tokens.remove(0); // remove compConcatOp
                    if (tokens.isEmpty() || leftTokens.isEmpty()) exception(token);

                    ComparisionAST comparision = new ComparisionAST(label, token.getLine(), token.getCol());
                    comparision.setLeft(parseFullCompExpression(leftTokens));
                    comparision.setRight(parseCompTerm(tokens));

                    return comparision;
                }
            }
        }

        return parseCompTerm(tokens);
    }

    private ComparisionAST parseCompTerm(List<Token> tokens) throws Exception {
        Token next = tokens.get(0);
        if (next.getLabel() == Token.LB) {
            tokens.remove(0);
            if (tokens.isEmpty()) exception(next);
            next = tokens.remove(tokens.size() - 1);
            if (next.getLabel() != Token.RB || tokens.isEmpty()) exception(next);

            return parseFullCompExpression(tokens);
        }

        if (next.getLabel() == Token.NEGATION) {
            ComparisionAST comparision = new ComparisionAST(next.getLabel(), next.getLine(), next.getCol());
            tokens.remove(0); // remove '!'
            comparision.setRight(parseFullCompExpression(tokens));

            return comparision;
        }

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            int label = token.getLabel();
            if (label == Token.EQUAL || label == Token.NOT_EQUAL || label == Token.LESS || label == Token.MORE || label == Token.LESS_EQUAL || label == Token.MORE_EQUAL) {
                List<Token> leftTokens = new LinkedList<>();
                for (int j = 0; j < i; j++) leftTokens.add(tokens.remove(0));
                tokens.remove(0); // remove compOp
                if (tokens.isEmpty() || leftTokens.isEmpty()) exception(token);

                ComparisionAST comparision = new ComparisionAST(label, token.getLine(), token.getCol());
                comparision.setLeftExpr(parseFullExpression(leftTokens));
                comparision.setRightExpr(parseFullExpression(tokens));

                return comparision;
            }
        }

        ComparisionAST comparision = new ComparisionAST(next.getLine(), next.getCol());
        comparision.setRightExpr(parseFullExpression(tokens));
        return comparision;
    }

    private void exception(Token token) throws Exception {
        throw new Exception("Syntax error on line: " + token.getLine() +", column: " + token.getCol());
    }
}
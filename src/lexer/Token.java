package lexer;

/**
 * Language token representation.
 */
public class Token {
    public static final int NONE = 0;
    public static final int BOOLEAN = 1;
    public static final int INTEGER = 2;
    public static final int DOUBLE = 3;
    public static final int STRING = 4;
    public static final int STRUCT = 5;
    public static final int LB = 6;
    public static final int RB = 7;
    public static final int LP = 8;
    public static final int RP = 9;
    public static final int ASSIGNMENT = 10;
    public static final int SEMICOLON = 11;
    public static final int COMMA = 12;
    public static final int EQUAL = 13;
    public static final int LESS = 14;
    public static final int MORE = 15;
    public static final int LESS_EQUAL = 16;
    public static final int MORE_EQUAL = 17;
    public static final int DOT = 18;
    public static final int IF = 19;
    public static final int ELSEIF = 20;
    public static final int ELSE = 21;
    public static final int FOR = 22;
    public static final int RETURN = 23;
    public static final int PRINTF = 24;
    public static final int NEW = 25;
    public static final int ADDITION = 26;
    public static final int SUBTRACTION = 27;
    public static final int MULTIPLICATION = 28;
    public static final int DIVISION = 29;
    public static final int MOD = 30;
    public static final int AND = 31;
    public static final int OR = 32;
    public static final int NEGATION = 33;
    public static final int LITERAL = 34;
    public static final int NULL = 35;
    public static final int BREAK = 36;
    public static final int VOID = 37;
    public static final int NOT_EQUAL = 38;

    private int line, col;
    private String value;
    private int label;

    public Token(int line, int col, String value, int label) {
        this.line = line;
        this.col = col;
        this.value = value;
        this.label = label;
    }

    public static int getTokenLabel(String token) {
        return switch (token) {
            case "boolean" -> BOOLEAN;
            case "int" -> INTEGER;
            case "double" -> DOUBLE;
            case "string" -> STRING;
            case "struct" -> STRUCT;
            case "(" -> LB;
            case ")" -> RB;
            case "{" -> LP;
            case "}" -> RP;
            case "=" -> ASSIGNMENT;
            case ";" -> SEMICOLON;
            case "," -> COMMA;
            case "==" -> EQUAL;
            case "<" -> LESS;
            case ">" -> MORE;
            case "<=" -> LESS_EQUAL;
            case ">=" -> MORE_EQUAL;
            case "." -> DOT;
            case "if" -> IF;
            case "elseif" -> ELSEIF;
            case "else" -> ELSE;
            case "for" -> FOR;
            case "return" -> RETURN;
            case "printf" -> PRINTF;
            case "new" -> NEW;
            case "+" -> ADDITION;
            case "-" -> SUBTRACTION;
            case "*" -> MULTIPLICATION;
            case "/" -> DIVISION;
            case "%" -> MOD;
            case "&&" -> AND;
            case "||" -> OR;
            case "!" -> NEGATION;
            case "null" -> NULL;
            case "break" -> BREAK;
            case "void" -> VOID;
            case "!=" -> NOT_EQUAL;
            default -> LITERAL;
        };
    }

    public static String getLabelValue(int label) {
        return switch (label) {
            case BOOLEAN -> "boolean";
            case INTEGER -> "int";
            case DOUBLE -> "double";
            case STRING -> "string";
            case STRUCT -> "struct";
            case LB -> "(";
            case RB -> ")";
            case LP -> "{";
            case RP -> "}";
            case ASSIGNMENT -> "=";
            case SEMICOLON -> ";";
            case COMMA -> ",";
            case EQUAL -> "==";
            case LESS -> "<";
            case MORE -> ">";
            case LESS_EQUAL -> "<=";
            case MORE_EQUAL -> ">=";
            case DOT -> ".";
            case IF -> "if";
            case ELSEIF -> "elseif";
            case ELSE -> "else";
            case FOR -> "for";
            case RETURN -> "return";
            case PRINTF -> "printf";
            case NEW -> "new";
            case ADDITION -> "+";
            case SUBTRACTION -> "-";
            case MULTIPLICATION -> "*";
            case DIVISION -> "/";
            case MOD -> "%";
            case AND -> "&&";
            case OR -> "||";
            case NEGATION -> "!";
            case NULL -> "null";
            case BREAK -> "break";
            case VOID -> "void";
            case NOT_EQUAL -> "!=";
            default -> "";
        };
    }

    public static boolean isIncorrectLabel(Token token) {
        return (token.getValue().charAt(0) > 47 && token.getValue().charAt(0) < 58) || token.getLabel() != Token.LITERAL;
    }

    public static boolean isNotATypeToken(Token token) {
        return switch (token.getLabel()) {
            case BOOLEAN, INTEGER, DOUBLE, STRING, VOID, LITERAL -> false;
            default -> true;
        };
    }

    public static boolean isExpressionToken(Token token) {
        return switch (token.getLabel()) {
            case LB, RB, COMMA, DOT, NEW, ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MOD, NULL, LITERAL -> true;
            default -> false;
        };
    }

    public static boolean isCompExpressionToken(Token token) {
        if (isExpressionToken(token)) {
            return true;
        }
        return switch (token.getLabel()) {
            case EQUAL, NOT_EQUAL, LESS, MORE, LESS_EQUAL, MORE_EQUAL, MOD, AND, OR -> true;
            default -> false;
        };
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

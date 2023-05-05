package lexer;

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
        switch (token) {
            case "boolean": return BOOLEAN;
            case "int": return INTEGER;
            case "double": return DOUBLE;
            case "string": return STRING;
            case "struct": return STRUCT;
            case "(": return LB;
            case ")": return RB;
            case "{": return LP;
            case "}": return RP;
            case "=": return ASSIGNMENT;
            case ";": return SEMICOLON;
            case ",": return COMMA;
            case "==": return EQUAL;
            case "<": return LESS;
            case ">": return MORE;
            case "<=": return LESS_EQUAL;
            case ">=": return MORE_EQUAL;
            case ".": return DOT;
            case "if": return IF;
            case "elseif": return ELSEIF;
            case "else": return ELSE;
            case "for": return FOR;
            case "return": return RETURN;
            case "printf": return PRINTF;
            case "new": return NEW;
            case "+": return ADDITION;
            case "-": return SUBTRACTION;
            case "*": return MULTIPLICATION;
            case "/": return DIVISION;
            case "%": return MOD;
            case "&&": return AND;
            case "||": return OR;
            case "!": return NEGATION;
            case "null": return NULL;
            case "break": return BREAK;
            case "void": return VOID;
            case "!=": return NOT_EQUAL;
        }
        return LITERAL;
    }

    public static String getLabelValue(int label) {
        switch (label) {
            case BOOLEAN: return "boolean";
            case INTEGER: return "int";
            case DOUBLE: return "double";
            case STRING: return "string";
            case STRUCT: return "struct";
            case LB: return "(";
            case RB: return ")";
            case LP: return "{";
            case RP: return "}";
            case ASSIGNMENT: return "=";
            case SEMICOLON: return ";";
            case COMMA: return ",";
            case EQUAL: return "==";
            case LESS: return "<";
            case MORE: return ">";
            case LESS_EQUAL: return "<=";
            case MORE_EQUAL: return ">=";
            case DOT: return ".";
            case IF: return "if";
            case ELSEIF: return "elseif";
            case ELSE: return "else";
            case FOR: return "for";
            case RETURN: return "return";
            case PRINTF: return "printf";
            case NEW: return "new";
            case ADDITION: return "+";
            case SUBTRACTION: return "-";
            case MULTIPLICATION: return "*";
            case DIVISION: return "/";
            case MOD: return "%";
            case AND: return "&&";
            case OR: return "||";
            case NEGATION: return "!";
            case NULL: return "null";
            case BREAK: return "break";
            case VOID: return "void";
            case NOT_EQUAL: return "!=";
        }
        return "";
    }

    public static boolean isIncorrectLabel(Token token) {
        return (token.getValue().charAt(0) > 47 && token.getValue().charAt(0) < 58) || token.getLabel() != Token.LITERAL;
    }

    public static boolean isType(Token token) {
        switch (token.getLabel()) {
            case BOOLEAN:
            case INTEGER:
            case DOUBLE:
            case STRING:
            case VOID:
            case LITERAL: return true;
        }
        return false;
    }

    public static boolean isExpressionToken(Token token) {
        switch (token.getLabel()) {
            case LB:
            case RB:
            case COMMA:
            case DOT:
            case NEW:
            case ADDITION:
            case SUBTRACTION:
            case MULTIPLICATION:
            case DIVISION:
            case MOD:
            case NULL:
            case LITERAL: return true;
        }
        return false;
    }

    public static boolean isCompExpressionToken(Token token) {
        if (isExpressionToken(token)) return true;
        switch (token.getLabel()) {
            case EQUAL:
            case NOT_EQUAL:
            case LESS:
            case MORE:
            case LESS_EQUAL:
            case MORE_EQUAL:
            case MOD:
            case AND:
            case OR: return true;
        }
        return false;
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

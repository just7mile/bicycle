package lexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Tokenizer which parses the provided file into tokens recognized by the language.
 */
public class Tokenizer {
    /**
     * File to tokenize.
     */
    private final File file;

    public Tokenizer(File file) {
        this.file = file;
    }

    /**
     * Tokenizes the file.
     *
     * @return list of tokens.
     * @throws Exception that are threw by the FileReader.
     */
    public List<Token> run() throws Exception {
        List<Token> tokens = new LinkedList<>();
        int ch, row = 1, col = 1;
        String token = "";
        Token tokenObj;
        boolean quote = false;

        BufferedReader reader = new BufferedReader(new FileReader(this.file));
        do {
            ch = reader.read();
            col++;
            if (quote) {
                token += (char) ch;
                if (ch == '"' && (token.length() < 2 || token.charAt(token.length() - 2) != '\\')) {
                    tokenObj = new Token(row, col - token.length(), token, Token.getTokenLabel(token));
                    if (!token.isEmpty()) {
                        tokens.add(tokenObj);
                    }
                    token = "";
                    quote = false;
                }
            } else if (ch == '"') {
                tokenObj = new Token(row, col - 1 - token.length(), token, Token.getTokenLabel(token));
                if (!token.isEmpty()) {
                    tokens.add(tokenObj);
                }
                token = String.valueOf((char) ch);
                quote = true;
            } else if (ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == ';' || ch == ',' || ch == '.') {
                if (ch == '.' && (token.charAt(0) > 47 && token.charAt(0) < 58)) {
                    token += (char) ch;
                    continue;
                }
                tokenObj = new Token(row, col - 1 - token.length(), token, Token.getTokenLabel(token));
                if (!token.isEmpty()) {
                    tokens.add(tokenObj);
                }
                token = String.valueOf((char) ch);
                tokenObj = new Token(row, col - token.length(), token, Token.getTokenLabel(token));
                if (!token.isEmpty()) {
                    tokens.add(tokenObj);
                }
                token = "";
            } else if (ch == '<' || ch == '>' || ch == '!') {
                tokenObj = new Token(row, col - 1 - token.length(), token, Token.getTokenLabel(token));
                if (!token.isEmpty()) {
                    tokens.add(tokenObj);
                }
                token = String.valueOf((char) ch);
            } else if (ch == '=') {
                int flag = 0;
                if (token.equals("<") || token.equals(">") || token.equals("=") || token.equals("!")) {
                    token += (char) ch;
                    flag = 1;
                }
                tokenObj = new Token(row, col - flag - token.length(), token, Token.getTokenLabel(token));
                if (!token.isEmpty()) {
                    tokens.add(tokenObj);
                }
                if (flag == 1) {
                    token = "";
                } else {
                    token = String.valueOf((char) ch);
                }
            } else if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == -1) {
                tokenObj = new Token(row, col - 1 - token.length(), token, Token.getTokenLabel(token));
                if (!token.isEmpty()) {
                    tokens.add(tokenObj);
                }
                if (ch == '\n') {
                    row++;
                    col = 1;
                }
                token = "";
            } else if (token.endsWith("<") || token.endsWith(">") || token.endsWith("=") || token.endsWith("!")) {
                tokenObj = new Token(row, col - 1 - token.length(), token, Token.getTokenLabel(token));
                tokens.add(tokenObj);
                token = String.valueOf((char) ch);
            } else token += (char) ch;
        } while (ch != -1);

        reader.close();

        return tokens;
    }
}

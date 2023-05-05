import lexer.Token;
import lexer.Tokenizer;
import parser.Parser;
import program.ProgramAST;

import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        File file = new File("examples/simple.bcl");
//        File file = new File("examples/linkedList.bcl");
//        File file = new File("examples/map.bcl");
        Tokenizer lexer = new Tokenizer(file);
        Parser parser = new Parser();
        try {
            List<Token> tokens = lexer.run();
            ProgramAST program = parser.parse(tokens);
            program.semanticsCheck();
            program.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

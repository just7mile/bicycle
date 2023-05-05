import lexer.Token;
import lexer.Tokenizer;
import parser.Parser;
import program.ProgramAST;

import java.io.File;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        File file = new File("tests/linkedList.txt");
//        File file = new File("tests/linkedList.txt");
        Tokenizer lexer = new Tokenizer(file);
        Parser parser = new Parser();
        try {
            List<Token> tokens = lexer.run();
            ProgramAST program = parser.parse(tokens);
//            System.out.println("---------------\n");
//            System.out.println(program.toString());
//            System.out.println("---------------\n");
            program.semanticsCheck();
//            System.out.println("---------------\n");
            program.execute();

        } catch (Exception e) {
            System.out.print(e.toString());
        }
    }
}

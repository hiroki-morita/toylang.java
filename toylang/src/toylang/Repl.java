package toylang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Repl {

    public static void main(String[] args) throws IOException {
        final var isr = new InputStreamReader(System.in);
        final var reader = new BufferedReader(isr);

        String line = null;
        while ((line = readLine(reader)) != null) {
            final var lexer = new Lexer(line);
            final var parser = new Parser(lexer);
            final var e = parser.parse();
            System.out.println(e);
            System.out.println(e.eval());
        }
    }

    private static String readLine(BufferedReader reader) throws IOException {
        System.out.print("> ");
        return reader.readLine();
    }

}

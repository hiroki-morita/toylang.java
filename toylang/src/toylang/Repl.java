package toylang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class Repl {

    public static void main(String[] args) throws IOException {
        final var isr = new InputStreamReader(System.in);
        final var reader = new BufferedReader(isr);

        String text = null;
        Env env = new Env();
        while ((text = read(reader)) != null) {
            final var lexer = new Lexer(text);
            final var parser = new Parser(lexer);
            final var e = parser.parse();
            System.out.println(e);
            System.out.println(e.eval(env));
        }
    }

    private static String read(BufferedReader reader) throws IOException {
        final var continues = Pattern.compile("^(?<code>.*)\\\\\\s*$");

        // 1 行目
        System.out.print("> ");
        String text = reader.readLine();
        var m = continues.matcher(text);
        if (m.find()) {
            text = m.group("code");
            // 2 行目以降（末尾に \ がない行が現れるまで続ける）
            while (true) {
                System.out.print("... ");
                final String line = reader.readLine();
                m = continues.matcher(line);
                if (!m.find()) {
                    text += " " + line;
                    break;
                } else {
                    text += " " + m.group("code");
                }
            }
        }

        return text;
    }

}

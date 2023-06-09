package toylang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * ToyLang の REPL（Read-Eval-Print Loop）
 * python とか irb のように，対話環境でプログラムを実行する
 */
public class Repl {

    public static void main(String[] args) throws IOException {
        final var isr = new InputStreamReader(System.in);
        final var reader = new BufferedReader(isr);

        String text = null;
        Env env = new Env();
        while ((text = read(reader)) != null) {
            try {
                final var lexer = new Lexer(text);
                final var parser = new Parser(lexer);
                final var e = parser.parse();
                System.out.println(e);
                System.out.println(e.eval(env));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ユーザからの入力を受け取る
     * 
     * 入力行の末尾に "\" がある場合は次の行を読んで結合する
     */
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

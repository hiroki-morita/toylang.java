package toylang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ToyLang のインタプリタ
 * 
 * プログラムが書かれたファイルを読んで頭から実行していく
 */
public class Interpreter {

    public static void main(String[] args) throws IOException {
        // Eclipse だとコマンドライン引数を指定するのが面倒なので
        // プログラムのファイル名をハードコードする
        final var PROGRAM_NAME = "fibonacci.tl";
        final var FILE_PATH = "src/programs/" + PROGRAM_NAME;

        final var file = Paths.get(FILE_PATH);
        final var text = Files.readString(file);

        Env env = new Env();
        final var lexer = new Lexer(text);
        final var parser = new Parser(lexer);
        while (parser.hasNext()) {
            final var e = parser.parse();
            System.out.println(e.eval(env));
        }
    }

}

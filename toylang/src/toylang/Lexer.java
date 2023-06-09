package toylang;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 字句解析器
 * 
 * ユーザが入力したプログラム（単なる文字列）を読み込んで
 * トークンに変換する
 * 
 * 空白・コメントは無視して実際のプログラムに対応する部分のみを返す
 */
public class Lexer {

    // 空白の正規表現（読み飛ばす）
    private final static String SPACE_REGEX = "[\\n\\r\\t\\x20]+";
    // コメントの正規表現（読み飛ばす）
    private final static String COMMENT_REGEX = "//[^\\r\\n]*|/\\*(?:(?!\\*/).)*\\*/";
    // 読み飛ばす文字列のパターン
    private final static Pattern IGNORE_PATTERN = //
            Pattern.compile("^(?:" + SPACE_REGEX + "|" + COMMENT_REGEX + ")*", Pattern.MULTILINE);

    // 入力文字列
    private final String text;
    // 各トークンのパターン
    private final List<Pattern> patterns;

    // 現在の入力インデックス
    private int pos;

    public Lexer(String text) {
        this.text = text;
        this.pos = 0;
        // Token の定義から正規表現を読み出してコンパイルする
        this.patterns = Arrays.stream(Token.Kind.values()) //
                              .filter(k -> k != Token.Kind.EOF) //
                              .map(k -> Pattern.compile("^" + k.pattern)) //
                              .collect(Collectors.toList());

    }

    /**
     * 次のトークンを求める
     * @return 次のトークン（すべて読み終えていたら EOF）
     */
    public Token next() {
        consumeIgnoreChars();
        if (pos >= text.length()) {
            return new Token.Eof();
        }

        final var tok = tokenize();
        pos += tok.text().length();
        return tok;
    }

    /**
     * pos 直後の空白・コメントを消費して読み飛ばす
     */
    private void consumeIgnoreChars() {
        if (pos >= text.length()) {
            return; // do nothing!
        }
        final var tail = text.substring(pos);
        final var m = IGNORE_PATTERN.matcher(tail);
        if (m.find()) {
            final var s = m.group();
            pos += s.length();
        }
    }

    /**
     * pos 直後のトークンを読んで返す
     * @return 読んだトークン
     */
    private Token tokenize() {
        final String tail = text.substring(pos);

        // すべてのトークンのマッチを試して最長一致するトークンを探す
        String str = "";
        Token.Kind kind = null;
        for (int i = 0; i < patterns.size(); i++) {
            final var p = patterns.get(i);
            final var m = p.matcher(tail);
            if (m.find()) {
                final var found = m.group();
                if (found.length() > str.length()) {
                    str = found;
                    kind = Token.Kind.values()[i];
                }
            }
        }
        // 種類を読んで適切なトークンオブジェクトを返す
        switch (kind) {
        case PLUS:
        case MINUS:
        case STAR:
        case SLASH:
        case LPAREN:
        case RPAREN:
        case EQ:
        case LT:
        case GT:
        case VBAR:
        case ARROW:
        case COMMA:
        case NOT:
        case LET:
        case IN:
        case IF:
        case THEN:
        case ELSE:
        case AND:
        case OR:
        case TRUE:
        case FALSE:
            return new Token.Fixed(kind);
        case INT:
            final int n = Integer.parseInt(str);
            return new Token.Int(n, str);
        case IDENT:
            return new Token.Ident(str);
        default:
            throw new RuntimeException("Unexpected value: " + kind);
        }
    }

}

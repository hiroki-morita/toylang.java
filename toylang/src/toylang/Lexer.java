package toylang;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Lexer {

    // ! 空白のパターン（読み飛ばす ）
    private final static Pattern SPACE_PATTERN = Pattern.compile("^[\\n\\r\\t\\x20]*", Pattern.MULTILINE);

    private final String text; // !< 入力文字列
    private final List<Pattern> patterns; // !< トークンのパターン

    private int pos; // !< 解析位置

    public Lexer(String text) {
        this.text = text;
        this.patterns = Arrays.stream(Token.Kind.values())
                              .filter(k -> k != Token.Kind.EOF)
                              .map(k -> Pattern.compile("^" + k.pattern))
                              .collect(Collectors.toList());
        this.pos = 0;
    }

    public Token next() {
        consumeSpaces();
        if (pos >= text.length()) {
            return new Token.Eof();
        }

        final var tok = tokenize();
        pos += tok.text()
                  .length();
        return tok;
    }

    private void consumeSpaces() {
        if (pos >= text.length()) {
            return; // do nothing!
        }
        final var tail = text.substring(pos);
        final var m = SPACE_PATTERN.matcher(tail);
        if (m.find()) {
            final var s = m.group();
            pos += s.length();
        }
    }

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
        case ADDOP:
        case SUBOP:
        case MULOP:
        case DIVOP:
        case LPAREN:
        case RPAREN:
        case EQ:
        case LT:
        case GT:
        case ANDAND:
        case OROR:
        case NOT:
        case LET:
        case IN:
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

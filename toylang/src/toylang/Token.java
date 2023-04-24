package toylang;

/**
 * プログラムが含みうるトークン（文字のかたまり）
 */
public interface Token {

    /**
     * トークンの種類を返す
     * @return トークンの種類
     */
    Kind kind();

    /**
     * トークンに対応する入力中の文字列を返す
     * @return 実際の文字列
     */
    String text();

    /**
     * トークンの種類
     *
     * トークナイズのための正規表現もここで定義する
     */
    public enum Kind {
        PLUS("+", "\\+"), // "+"
        MINUS("-", "-"), // "-"
        STAR("*", "\\*"), // "*"
        SLASH("/", "/"), // "/"
        LPAREN("(", "\\("), // "("
        RPAREN(")", "\\)"), // ")"
        EQ("=", "="), // "="
        LT("<", "<"), // "<"
        GT(">", ">"), // ">"
        VBAR("|", "\\|"), // "|"
        ARROW("=>", "=>"), // "=>"
        COMMA(",", ","), // ","
        NOT("not", "not"), // "not"
        LET("let", "let"), // "let"
        IN("in", "in"), // "in"
        IF("if", "if"), // "if"
        THEN("then", "then"), // "then"
        ELSE("else", "else"), // "else"
        AND("and", "and"), // "and"
        OR("or", "or"), // "or"
        TRUE("true", "true"), // "true"
        FALSE("false", "false"), // "false"
        INT(null, "(?:0|[1-9][0-9]*)"), // 整数リテラル（負の値は表現しない）
        IDENT(null, "[a-zA-Z_][a-zA-Z0-9_]*"), // 識別子
        EOF(null, null); // EOF

        final String fixedText; // 予約語・固定のトークンの文字列（可変の場合 nul）
        final String pattern; // トークンがマッチする正規表現

        private Kind(String fixedText, String pattern) {
            this.fixedText = fixedText;
            this.pattern = pattern;
        }

        /**
         * このトークンが k と等しくないか判定する
         * @param k トークンの種類
         * @return k と等しくなければ true
         */
        public boolean not(Kind k) {
            return k != this;
        }

        /**
         * このトークンが指定されたトークンに含まれるか判定する
         * @param ks トークンの種類（> 0）
         * @return 含まれるとき true
         */
        public boolean in(Kind... ks) {
            for (var k : ks) {
                if (k == this) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 予約語・固定長トークン
     */
    public class Fixed implements Token {
        final Kind k;

        public Fixed(Kind k) {
            assert k.fixedText != null;
            this.k = k;
        }

        @Override
        public Kind kind() {
            return k;
        }

        @Override
        public String text() {
            return k.fixedText;
        }

        @Override
        public String toString() {
            return String.format("Token [%s]", k);
        }
    }

    /**
     * 整数リテラル
     */
    public class Int implements Token {
        final int n; // 対応する値
        final String str; // 実際の文字列

        public Int(int n, String str) {
            this.n = n;
            this.str = str;
        }

        @Override
        public Kind kind() {
            return Kind.INT;
        }

        @Override
        public String text() {
            return str;
        }

        @Override
        public String toString() {
            return String.format("Token [Int(%s)]", str);
        }
    }

    /**
     * 識別子（変数名）
     */
    public class Ident implements Token {
        final String name; // 名前

        public Ident(String name) {
            this.name = name;
        }

        @Override
        public Kind kind() {
            return Kind.IDENT;
        }

        @Override
        public String text() {
            return name;
        }

        @Override
        public String toString() {
            return String.format("Token [Ident(%s)]", name);
        }
    }

    /**
     * 入力の終わり
     * 
     * トークナイザが入力をすべて読んだときに返すダミーのトークン
     */
    public class Eof implements Token {
        @Override
        public Kind kind() {
            return Kind.EOF;
        }

        @Override
        public String text() {
            return "";
        }

        @Override
        public String toString() {
            return "Token [EOF]";
        }

    }

}

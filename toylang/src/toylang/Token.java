package toylang;

public interface Token {

    Kind kind();

    String text();

    public enum Kind {
        ADDOP("+", "\\+"), // "+"
        SUBOP("-", "-"), // "-"
        MULOP("*", "\\*"), // "*"
        DIVOP("/", "/"), // "/"
        LPAREN("(", "\\("), // "("
        RPAREN(")", "\\)"), // ")"
        EQ("=", "="), // "="
        LT("<", "<"), // "<"
        GT(">", ">"), // ">"
        ANDAND("&&", "&&"), // "&&"
        OROR("||", "\\|\\|(?!\\s*=>)"), // "||", not followed by "=>"
        VBAR("|", "\\|"), // "|"
        ARROW("=>", "=>"), // "=>"
        COMMA(",", ","), // ","
        NOT("not", "not"), // "not"
        LET("let", "let"), // "let"
        IN("in", "in"), // "in"
        IF("if", "if"), // "if"
        THEN("then", "then"), // "then"
        ELSE("else", "else"), // "else"
        TRUE("true", "true"), // "true"
        FALSE("false", "false"), // "false"
        INT(null, "-?(0|[1-9][0-9]*)"), // 整数リテラル
        IDENT(null, "[a-zA-Z_][a-zA-Z0-9_]*"), // 識別子
        EOF(null, null); // EOF

        final String fixedText;
        final String pattern;

        private Kind(String fixedText, String pattern) {
            this.fixedText = fixedText;
            this.pattern = pattern;
        }

        public boolean not(Kind k) {
            return k != this;
        }

        public boolean in(Kind... ks) {
            for (var k : ks) {
                if (k == this) {
                    return true;
                }
            }
            return false;
        }
    }

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

    public class Int implements Token {
        final int n;
        final String str;

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

    public class Ident implements Token {
        final String name;

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

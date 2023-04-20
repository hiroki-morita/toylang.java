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
        INT(null, "-?(0|[1-9][0-9]*)"), // 整数リテラル
        EOF(null, null); // EOF

        final String fixedText;
        final String pattern;

        private Kind(String fixedText, String pattern) {
            this.fixedText = fixedText;
            this.pattern = pattern;
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

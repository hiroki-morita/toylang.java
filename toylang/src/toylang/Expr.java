package toylang;

public interface Expr {

    int eval();

    @Override
    String toString();

    public class Int implements Expr {
        final int n;

        public Int(int n) {
            this.n = n;
        }

        @Override
        public int eval() {
            return n;
        }

        @Override
        public String toString() {
            return String.format("[Int %d]", n);
        }
    }

    public class BinOp implements Expr {
        public enum Kind {
            ADD("+"), // 加算
            SUB("-"), // 減算
            MUL("*"), // 乗算
            DIV("/"); // 除算

            final String text;

            private Kind(String text) {
                this.text = text;
            }
        }

        final Kind kind;
        final Expr left;
        final Expr right;

        public BinOp(Kind kind, Expr left, Expr right) {
            this.kind = kind;
            this.left = left;
            this.right = right;
        }

        @Override
        public int eval() {
            final var l = left.eval();
            final var r = right.eval();
            switch (kind) {
            case ADD:
                return l + r;
            case SUB:
                return l - r;
            case MUL:
                return l * r;
            case DIV:
                return l / r;
            default:
                throw new RuntimeException("unreachable!");
            }
        }

        @Override
        public String toString() {
            return String.format("[%s %s %s]", kind.text, left, right);
        }
    }

}

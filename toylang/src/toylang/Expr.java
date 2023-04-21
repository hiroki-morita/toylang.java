package toylang;

public interface Expr {

    int eval(Env env);

    @Override
    String toString();

    public class Int implements Expr {
        final int n;

        public Int(int n) {
            this.n = n;
        }

        @Override
        public int eval(Env env) {
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
        public int eval(Env env) {
            final var l = left.eval(env);
            final var r = right.eval(env);
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

    public class Let implements Expr {
        final String ident;
        final Expr e;
        final Expr suc;

        public Let(String ident, Expr e) {
            this.ident = ident;
            this.e = e;
            this.suc = null;
        }

        public Let(String ident, Expr e, Expr suc) {
            this.ident = ident;
            this.e = e;
            this.suc = suc;
        }

        @Override
        public int eval(Env env) {
            final int v = e.eval(env);
            env.add(ident, v);
            if (suc == null) {
                return v;
            } else {
                return suc.eval(env);
            }
        }

        @Override
        public String toString() {
            if (suc == null) {
                return String.format("[Let %s %s in %s]", ident, e, suc);
            } else {
                return String.format("[Let %s %s]", ident, e);
            }
        }

    }

    public class Ident implements Expr {
        final String name;

        public Ident(String name) {
            this.name = name;
        }

        @Override
        public int eval(Env env) {
            final var found = env.find(name);
            if (found == null) {
                final var msg = String.format("ident '%s' not defined.", name);
                throw new RuntimeException(msg);
            }
            return found;
        }

        @Override
        public String toString() {
            return String.format("[Ident %s]", name);
        }

    }

}

package toylang;

public interface Expr {

    Value eval(Env env);

    @Override
    String toString();

    public class Int implements Expr {
        final int n;

        public Int(int n) {
            this.n = n;
        }

        @Override
        public Value eval(Env env) {
            return new Value.Int(n);
        }

        @Override
        public String toString() {
            return String.format("[Int %d]", n);
        }
    }

    public class Bool implements Expr {
        final boolean b;

        public Bool(boolean b) {
            this.b = b;
        }

        @Override
        public Value eval(Env env) {
            return new Value.Bool(b);
        }

        @Override
        public String toString() {
            return String.format("[Bool %b]", b);
        }
    }

    public class BinOp implements Expr {
        public enum Kind {
            ADD("+", Value.Op.ADD), // 加算
            SUB("-", Value.Op.SUB), // 減算
            MUL("*", Value.Op.MUL), // 乗算
            DIV("/", Value.Op.DIV), // 除算
            EQ("=", Value.Op.EQ), // 比較（等価）
            LT("<", Value.Op.LT), // 比較（小なり）
            GT(">", Value.Op.GT), // 比較（大なり）
            AND("&&", Value.Op.AND), // 論理積
            OR("||", Value.Op.OR); // 論理和

            final String text;
            final Value.Op valOp;

            private Kind(String text, Value.Op valOp) {
                this.text = text;
                this.valOp = valOp;
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
        public Value eval(Env env) {
            final var l = left.eval(env);
            final var r = right.eval(env);
            return l.apply(kind.valOp, r);
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
        public Value eval(Env env) {
            final var v = e.eval(env);
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
                return String.format("[Let %s %s]", ident, e);
            } else {
                return String.format("[Let %s %s in %s]", ident, e, suc);
            }
        }

    }

    public class Ident implements Expr {
        final String name;

        public Ident(String name) {
            this.name = name;
        }

        @Override
        public Value eval(Env env) {
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

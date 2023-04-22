package toylang;

public interface Expr {

    Value eval(Env env);

    @Override
    String toString();

    public class Unit implements Expr {
        @Override
        public Value eval(Env env) {
            return Value.Unit.get();
        }

        @Override
        public String toString() {
            return "[Unit]";
        }
    }

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
            ADD("+", Value.BinOp.ADD), // 加算
            SUB("-", Value.BinOp.SUB), // 減算
            MUL("*", Value.BinOp.MUL), // 乗算
            DIV("/", Value.BinOp.DIV), // 除算
            EQ("=", Value.BinOp.EQ), // 比較（等価）
            LT("<", Value.BinOp.LT), // 比較（小なり）
            GT(">", Value.BinOp.GT), // 比較（大なり）
            AND("&&", Value.BinOp.AND), // 論理積
            OR("||", Value.BinOp.OR); // 論理和

            final String text;
            final Value.BinOp valOp;

            private Kind(String text, Value.BinOp valOp) {
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
            return l.applyBinOp(kind.valOp, r);
        }

        @Override
        public String toString() {
            return String.format("[%s %s %s]", kind.text, left, right);
        }
    }

    public class UnaryOp implements Expr {
        public enum Kind {
            NOT("not", Value.UnaryOp.NOT); // 論理否定

            final String text;
            final Value.UnaryOp valOp;

            private Kind(String text, Value.UnaryOp valOp) {
                this.text = text;
                this.valOp = valOp;
            }
        }

        final Kind kind;
        final Expr e;

        public UnaryOp(Kind kind, Expr e) {
            this.kind = kind;
            this.e = e;
        }

        @Override
        public Value eval(Env env) {
            final var v = e.eval(env);
            return v.applyUnaryOp(kind.valOp);
        }

        @Override
        public String toString() {
            return String.format("[%s %s]", kind.text, e);
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

    public class If implements Expr {
        final Expr cond;
        final Expr then;
        final Expr els;

        public If(Expr cond, Expr then, Expr els) {
            this.cond = cond;
            this.then = then;
            this.els = els;
        }

        @Override
        public Value eval(Env env) {
            final var condVal = cond.eval(env);
            if (!(condVal instanceof Value.Bool)) {
                final var msg = String.format("if cond must be Bool, but found '%s'.", condVal);
                throw new RuntimeException(msg);
            }

            final boolean c = ((Value.Bool) condVal).b;
            if (c) {
                return then.eval(env);
            } else if (els == null) {
                return Value.Unit.get();
            } else {
                return els.eval(env);
            }
        }

        @Override
        public String toString() {
            if (els == null) {
                return String.format("[If %s %s]", cond, then);
            } else {
                return String.format("[If %s %s %s]", cond, then, els);
            }
        }
    }

    public class Func implements Expr {
        final String arg;
        final Expr e;

        public Func(String arg, Expr e) {
            this.arg = arg;
            this.e = e;
        }

        @Override
        public Value eval(Env env) {
            // TODO: env を deepcopy しないとバグる？（shallow copy は必須）
            return new Value.Closure(arg, e, env.copy());
        }

        @Override
        public String toString() {
            return String.format("[Fn (%s) => %s]", arg, e);
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

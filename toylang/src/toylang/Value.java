package toylang;

public interface Value {

    public enum BinOp {
        ADD, // +
        SUB, // -
        MUL, // *
        DIV, // /
        EQ, // =
        LT, // <
        GT, // >
        AND, // &&
        OR, // ||
        APPLY, // （関数適用）
    }

    public enum UnaryOp {
        NOT, // not
    }

    Value applyBinOp(BinOp op, Value other);

    Value applyUnaryOp(UnaryOp op);

    @Override
    String toString();

    public class Unit implements Value {
        private static Unit SINGLETON = new Unit();

        private Unit() {
        }

        public static Unit get() {
            return SINGLETON;
        }

        @Override
        public Value applyBinOp(BinOp op, Value other) {
            final var msg = String.format("cannot '%s' %s %s.", op, this, other);
            throw new RuntimeException(msg);
        }

        @Override
        public Value applyUnaryOp(UnaryOp op) {
            final var msg = String.format("cannot '%s' %s.", op, this);
            throw new RuntimeException(msg);
        }

        @Override
        public String toString() {
            return "()";
        }
    }

    public class Int implements Value {
        final int n;

        public Int(int n) {
            this.n = n;
        }

        @Override
        public Value applyBinOp(BinOp op, Value other) {
            if (!(other instanceof Int)) {
                final var msg = String.format("cannot '%s' %s %s.", op, this, other);
                throw new RuntimeException(msg);
            }

            final Int o = (Int) (other);
            switch (op) {
            case ADD:
                return new Int(n + o.n);
            case SUB:
                return new Int(n - o.n);
            case MUL:
                return new Int(n * o.n);
            case DIV:
                return new Int(n * o.n);
            case EQ:
                return new Bool(n == o.n);
            case LT:
                return new Bool(n < o.n);
            case GT:
                return new Bool(n > o.n);
            default:
                final var msg = String.format("cannot '%s' %s %s.", op, this, other);
                throw new RuntimeException(msg);
            }
        }

        @Override
        public Value applyUnaryOp(UnaryOp op) {
            switch (op) {
            // no operators can be applied to Int
            default:
                final var msg = String.format("cannot '%s' %s.", op, this);
                throw new RuntimeException(msg);
            }
        }

        @Override
        public String toString() {
            return Integer.toString(n);
        }
    }

    public class Bool implements Value {
        final boolean b;

        public Bool(boolean b) {
            this.b = b;
        }

        @Override
        public Value applyBinOp(BinOp op, Value other) {
            if (!(other instanceof Bool)) {
                final var msg = String.format("cannot '%s' %s %s.", op, this, other);
                throw new RuntimeException(msg);
            }

            final Bool o = (Bool) (other);
            switch (op) {
            case EQ:
                return new Bool(b == o.b);
            case AND:
                return new Bool(b && o.b);
            case OR:
                return new Bool(b || o.b);
            default:
                final var msg = String.format("cannot '%s' %s %s.", op, this, other);
                throw new RuntimeException(msg);
            }
        }

        @Override
        public Value applyUnaryOp(UnaryOp op) {
            switch (op) {
            case NOT: {
                return new Value.Bool(!b);
            }
            default:
                final var msg = String.format("cannot '%s' %s.", op, this);
                throw new RuntimeException(msg);
            }
        }

        @Override
        public String toString() {
            return Boolean.toString(b);
        }
    }

    public class Closure implements Value {
        final String arg;
        final Expr expr;
        final Env env;

        public Closure(String arg, Expr expr, Env env) {
            this.arg = arg;
            this.expr = expr;
            this.env = env;
        }

        @Override
        public Value applyBinOp(BinOp op, Value other) {
            switch (op) {
            case APPLY:
                final var param = other; // 実引数
                return expr.eval(env.with(arg, param));
            default:
                final var msg = String.format("cannot '%s' %s %s.", op, this, other);
                throw new RuntimeException(msg);
            }
        }

        @Override
        public Value applyUnaryOp(UnaryOp op) {
            switch (op) {
            default:
                final var msg = String.format("cannot '%s' %s.", op, this);
                throw new RuntimeException(msg);
            }
        }

        @Override
        public String toString() {
            return String.format("[Clos (%s) => %s, %s]", arg, expr, env);
        }
    }

}

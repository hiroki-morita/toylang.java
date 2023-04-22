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
    }

    public enum UnaryOp {
        NOT, // not
    }

    Value applyBinOp(BinOp op, Value other);

    Value applyUnaryOp(UnaryOp op);

    @Override
    String toString();

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

}

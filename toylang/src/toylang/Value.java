package toylang;

public interface Value {

    public enum Op {
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

    Value apply(Op op, Value other);

    @Override
    String toString();

    public class Int implements Value {
        final int n;

        public Int(int n) {
            this.n = n;
        }

        @Override
        public Value apply(Op op, Value other) {
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
        public Value apply(Op op, Value other) {
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
        public String toString() {
            return Boolean.toString(b);
        }
    }

}

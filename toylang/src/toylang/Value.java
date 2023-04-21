package toylang;

public interface Value {

    public enum Op {
        ADD, // +
        SUB, // -
        MUL, // *
        DIV, // /
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
            default:
                final var msg = String.format("cannot '%s' %s %s.", op, this, other);
                throw new IllegalArgumentException(msg);
            }
        }

        @Override
        public String toString() {
            return Integer.toString(n);
        }
    }

}

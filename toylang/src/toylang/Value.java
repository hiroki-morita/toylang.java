package toylang;

/**
 * これ以上簡約できない値
 */
public interface Value {

    /**
     * 二項演算の演算子の種類
     */
    public enum BinOp {
        ADD, // 加算
        SUB, // 減算
        MUL, // 乗算
        DIV, // 除算
        EQ, // 等価判定
        LT, // 比較（小なり）
        GT, // 比較（大なり）
        AND, // 論理積
        OR, // 論理和
    }

    /**
     * 単項演算の演算子の種類
     */
    public enum UnaryOp {
        NOT, // 論理否定
        MINUS, // 符号反転
    }

    /**
     * 二項演算を適用する
     * @param op 演算子
     * @param other もう片方のオペランド
     * @return 演算結果の Value
     */
    Value applyBinOp(BinOp op, Value other);

    /**
     * 単項演算を適用する
     * @param op 演算子
     * @return 演算結果の Value
     */
    Value applyUnaryOp(UnaryOp op);

    /**
     * 関数適用が可能な値（クロージャ）
     *
     * ふつう関数適用は "apply" だが演算子の適用にも使っているので
     * "call" を使う
     */
    public interface Callable extends Value {
        /**
         * 引数 param を適用した値を計算する
         * @param param 実引数
         * @return 計算結果の Value
         */
        Value call(Value param);
    }

    @Override
    String toString();

    /**
     * Unit 型の唯一の値（Java の null みたいな）
     */
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

    /**
     * 整数型の値
     */
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
            case MINUS:
                return new Value.Int(-n);
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

    /**
     * 論理値型の値
     */
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

    /**
     * クロージャ（関数とそれが評価されたときの環境）
     */
    public class Closure implements Value, Callable {
        final String arg;
        final Expr fn;
        final Env env;

        public Closure(String arg, Expr fn, Env env) {
            this.arg = arg;
            this.fn = fn;
            this.env = env;
        }

        @Override
        public Value applyBinOp(BinOp op, Value other) {
            switch (op) {
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
        public Value call(Value param) {
            if (arg == null) { // nullary
                return fn.eval(new Env(env));
            } else {
                return fn.eval(env.with(arg, param));
            }
        }

        @Override
        public String toString() {
            return String.format("[Clos (%s) => %s, %s]", arg, fn, env);
        }
    }

}

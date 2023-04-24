package toylang;

/**
 * 式の木構造（抽象構文木）
 *
 * これを eval() することで計算を進める
 */
public interface Expr {

    /**
     * 式を評価して値を求める
     * @param env 評価するための環境（名前→値のマップ）
     * @return 評価結果の値
     */
    Value eval(Env env);

    @Override
    String toString();

    /**
     * Unit 型に対応する式
     */
    public class Unit implements Expr {
        @Override
        public Value eval(Env env) {
            // Unit 型の Value をそのまま返す
            return Value.Unit.get();
        }

        @Override
        public String toString() {
            return "[Unit]";
        }
    }

    /**
     * 整数型に対応する式
     */
    public class Int implements Expr {
        final int n; // 値

        public Int(int n) {
            this.n = n;
        }

        @Override
        public Value eval(Env env) {
            // 値 n を持つ Value をそのまま返す
            return new Value.Int(n);
        }

        @Override
        public String toString() {
            return String.format("[Int %d]", n);
        }
    }

    /**
     * 論理値型の式
     */
    public class Bool implements Expr {
        final boolean b;

        public Bool(boolean b) {
            this.b = b;
        }

        @Override
        public Value eval(Env env) {
            // 値 b を持つ Value をそのまま返す
            return new Value.Bool(b);
        }

        @Override
        public String toString() {
            return String.format("[Bool %b]", b);
        }
    }

    /**
     * 変数の参照
     */
    public class Ident implements Expr {
        final String name; // 参照する変数名

        public Ident(String name) {
            this.name = name;
        }

        @Override
        public Value eval(Env env) {
            // 環境を変数名で検索してその値を返す
            final var found = env.find(name);
            if (found == null) {
                // 見つからない -> 未定義の値
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

    /**
     * 二項演算
     */
    public class BinOp implements Expr {
        /**
         * 演算の種類
         */
        public enum Kind {
            ADD("+", Value.BinOp.ADD), // 加算
            SUB("-", Value.BinOp.SUB), // 減算
            MUL("*", Value.BinOp.MUL), // 乗算
            DIV("/", Value.BinOp.DIV), // 除算
            EQ("=", Value.BinOp.EQ), // 比較（等価）
            LT("<", Value.BinOp.LT), // 比較（小なり）
            GT(">", Value.BinOp.GT), // 比較（大なり）
            AND("and", Value.BinOp.AND), // 論理積
            OR("or", Value.BinOp.OR); // 論理和

            final String text; // 出力する際の文字列
            final Value.BinOp valOp; // Value の対応する演算の種類

            private Kind(String text, Value.BinOp valOp) {
                this.text = text;
                this.valOp = valOp;
            }
        }

        final Kind kind; // 演算の種類
        final Expr left; // 左のオペランド
        final Expr right; // 右のオペランド

        public BinOp(Kind kind, Expr left, Expr right) {
            this.kind = kind;
            this.left = left;
            this.right = right;
        }

        @Override
        public Value eval(Env env) {
            // 左右両方を eval() して値に簡約し，それらの演算結果を返す
            // TODO: 短絡評価の実装（a and b で a = false なら b は簡約せず false を返す，等）
            final var l = left.eval(env);
            final var r = right.eval(env);
            return l.applyBinOp(kind.valOp, r);
        }

        @Override
        public String toString() {
            return String.format("[%s %s %s]", kind.text, left, right);
        }
    }

    /**
     * 単項演算
     */
    public class UnaryOp implements Expr {
        /**
         * 演算の種類
         */
        public enum Kind {
            NOT("not", Value.UnaryOp.NOT), // 論理否定
            MINUS("-", Value.UnaryOp.MINUS); // 符号反転

            final String text; // 出力する際の文字列
            final Value.UnaryOp valOp; // Value の対応する演算の種類

            private Kind(String text, Value.UnaryOp valOp) {
                this.text = text;
                this.valOp = valOp;
            }
        }

        final Kind kind; // 演算の種類
        final Expr e; // オペランド

        public UnaryOp(Kind kind, Expr e) {
            this.kind = kind;
            this.e = e;
        }

        @Override
        public Value eval(Env env) {
            // オペランドを簡約して演算を適用
            final var v = e.eval(env);
            return v.applyUnaryOp(kind.valOp);
        }

        @Override
        public String toString() {
            return String.format("[%s %s]", kind.text, e);
        }
    }

    /**
     * 変数の束縛
     * 
     * 評価は eager に行う；let x = e を評価するときに
     * 式 e を評価してしまう．
     */
    public class Let implements Expr {
        final String ident; // 名前
        final Expr e; // 束縛する式
        final Expr suc; // in ... で続く式（maybe null）

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
            // 式を評価してその値を環境に突っ込む
            final var v = e.eval(env);
            env.add(ident, v);
            // in ... で続くならそちらも評価
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

    /**
     * 条件分岐
     */
    public class If implements Expr {
        final Expr cond; // 条件式
        final Expr then; // cond = true のときに評価される式
        final Expr els; // cond = false のときに評価される式

        public If(Expr cond, Expr then, Expr els) {
            this.cond = cond;
            this.then = then;
            this.els = els;
        }

        @Override
        public Value eval(Env env) {
            // 条件式を評価
            final var condVal = cond.eval(env);
            // 条件式は論理値でなければならない
            if (!(condVal instanceof Value.Bool)) {
                final var msg = String.format("if cond must be Bool, but found '%s'.", condVal);
                throw new RuntimeException(msg);
            }

            // 条件式の評価結果で then/els のいずれかを評価
            final boolean c = ((Value.Bool) condVal).b;
            if (c) {
                return then.eval(env);
            } else if (els == null) {
                // c = false で else 節がないときは () を返す
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

    /**
     * 関数
     * 
     * カリー化するため引数は 1 つのみとする．
     */
    public class Func implements Expr {
        final String arg; // 引数名（maybe null）
        final Expr e; // 関数の中身

        public Func(String arg, Expr e) {
            this.arg = arg;
            this.e = e;
        }

        @Override
        public Value eval(Env env) {
            // いまの環境でクロージャを作成（中身 e の評価はしない！）
            // TODO: 環境を shallow/deep copy しなくてもよいか調査
            // コピーが必要な場合は再帰関数のために let rec を導入する必要がある
            return new Value.Closure(arg, e, env);
        }

        @Override
        public String toString() {
            return String.format("[Fn (%s) => %s]", arg, e);
        }
    }

    /**
     * 関数適用
     */
    public class Apply implements Expr {
        final Expr fn; // 適用する関数
        final Expr param; // 実引数

        public Apply(Expr fn, Expr param) {
            this.fn = fn;
            this.param = param;
            assert this.param != null : "param must not be null; use Unit.";
        }

        @Override
        public Value eval(Env env) {
            // fn を評価して関数の実体（クロージャ）を取り出す
            final var fnVal = fn.eval(env);
            // fn の評価結果がクロージャでなければ型エラー
            if (!(fnVal instanceof Value.Callable)) {
                final var msg = String.format("apply failed: '%s' is not a function.", fnVal);
                throw new RuntimeException(msg);
            }
            // param を評価して関数に与える
            final var callable = (Value.Callable) (fnVal);
            return callable.call(param.eval(env));
        }

        @Override
        public String toString() {
            return String.format("[apply %s %s]", fn, param);
        }
    }

}

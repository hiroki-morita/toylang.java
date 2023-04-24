package toylang;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 式を eval() するための環境（名前→値のマップ）
 * 
 * ネスト構造を持ち，"外側の環境"を保持する．
 * 名前に対応する値を検索するときは最も内側から検索し，
 * なければ外側の環境を検索する
 */
public class Env {
    // この階層での名前→値のマップ
    private final Map<String, Value> map;
    // 外側の環境（最外では null）
    private final Env outer;

    // toString() 中 true にする（無限再帰を防ぐため）
    private boolean nowStringify = false;

    private Env(Map<String, Value> map, Env outer) {
        this.map = map;
        this.outer = outer;
    }

    /**
     * デフォルトコンストラクタ（空の環境を生成する）
     */
    public Env() {
        this.map = new HashMap<>();
        this.outer = null;
    }

    /**
     * コンストラクタ
     * @param outer 外側の環境
     */
    public Env(Env outer) {
        this.map = new HashMap<>();
        this.outer = outer;
    }

    /**
     * 環境を名前で検索して対応する値を返す
     * @param key 名前
     * @return 対応する値，なければ null
     */
    public Value find(String key) {
        final var found = map.get(key);
        if (found == null && outer != null) {
            return outer.find(key);
        }
        return found;
    }

    /**
     * 環境のこの階層に名前→値の対応を追加する
     * @param key 名前
     * @param val key に対応する値
     */
    public void add(String key, Value val) {
        map.put(key, val);
    }

    /**
     * 最奥に key→val の対応を持つ環境を生成する
     * @param key 名前
     * @param val key に対応する値
     * @return key→val の対応のみを持ち，this を外側に持つ環境
     */
    public Env with(String key, Value val) {
        final var m = Map.of(key, val);
        return new Env(m, this);
    }

    /**
     * この階層のみをコピーする
     * @return この階層のみコピーした環境（外側は同じオブジェクト）
     */
    public Env copy() {
        return new Env(new HashMap<>(map), outer);
    }

    @Override
    public String toString() {
        if (nowStringify) {
            return "...";
        }
        nowStringify = true;

        final var otr = outer == null ? "{}" : outer.toString();
        final var kvs = map.keySet().stream() //
                           .map(k -> String.format("%s:%s", k, map.get(k))) //
                           .collect(Collectors.joining(","));
        final var s = String.format("{%s, %s}", kvs, otr);

        nowStringify = false;
        return s;
    }
}

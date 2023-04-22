package toylang;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Env {
    private final Map<String, Value> map;
    private final Env outer;

    private Env(Map<String, Value> map, Env outer) {
        this.map = map;
        this.outer = outer;
    }

    public Env() {
        this.map = new HashMap<>();
        this.outer = null;
    }

    public Env(Env outer) {
        this.map = new HashMap<>();
        this.outer = outer;
    }

    public Value find(String key) {
        final var found = map.get(key);
        if (found == null && outer != null) {
            return outer.find(key);
        }
        return found;
    }

    public void add(String key, Value val) {
        map.put(key, val);
    }

    public Env with(String key, Value val) {
        final var m = Map.of(key, val);
        return new Env(m, this);
    }

    @Override
    public String toString() {
        final var otr = outer == null ? "{}" : outer.toString();
        final var kvs = map.keySet().stream() //
                           .map(k -> String.format("%s:%s", k, map.get(k))) //
                           .collect(Collectors.joining(","));
        return String.format("{%s, %s}", kvs, otr);
    }
}

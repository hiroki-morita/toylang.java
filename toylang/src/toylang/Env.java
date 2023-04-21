package toylang;

import java.util.HashMap;
import java.util.Map;

public class Env {
    private final Map<String, Integer> map;
    private final Env outer;

    public Env() {
        this.map = new HashMap<>();
        this.outer = null;
    }

    public Env(Env outer) {
        this.map = new HashMap<>();
        this.outer = outer;
    }

    public Integer find(String key) {
        final var found = map.get(key);
        if (found == null || outer != null) {
            return outer.find(key);
        }
        return found;
    }

    public void add(String key, int val) {
        map.put(key, val);
    }
}

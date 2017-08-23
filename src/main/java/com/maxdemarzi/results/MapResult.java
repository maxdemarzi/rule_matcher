package com.maxdemarzi.results;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MapResult {
    private static final MapResult EMPTY = new MapResult(Collections.emptyMap());
    public final Map<String, Set<String>> value;

    public MapResult(Map<String, Set<String>> value) {
        this.value = value;
    }

    public static MapResult empty() {
        return EMPTY;
    }
}

package com.maxdemarzi.results;

import org.neo4j.graphdb.Path;

import java.util.Collections;
import java.util.Map;

public class MapResult {
    private static final MapResult EMPTY = new MapResult(Collections.emptyMap());
    public final Map<String, Object> value;

    public MapResult(Map<String, Object> value) {
        this.value = value;
    }

    public MapResult(Path path) {
        this.value = path.endNode().getAllProperties();
    }

    public static MapResult empty() {
        return EMPTY;
    }
}

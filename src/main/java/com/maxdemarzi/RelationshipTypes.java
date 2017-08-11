package com.maxdemarzi;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    HAS_ATTRIBUTE,
    HAS_RULE,
    IN_PATH
}

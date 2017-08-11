package com.maxdemarzi;

import com.maxdemarzi.results.NodeResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Matcher {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    @Procedure(name = "com.maxdemarzi.matcher", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.matcher(username) - find matching rules")
    public Stream<NodeResult> match(@Name("username") String username) throws IOException {
        // We start by finding the user
        Node user = db.findNode(Labels.User, "username", username);
        if (user != null) {
            // Gather all of their attributes in to a Set
            Set<Node> userAttributes = new HashSet<>();
            Set<String> attributes = new HashSet<>();
            Set<Node> paths = new HashSet<>();
            Set<Node> rules = new HashSet<>();

            for (Relationship r : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                userAttributes.add(r.getEndNode());
                attributes.add((String)r.getEndNode().getProperty("id"));
            }

            for (Node attribute : userAttributes) {
                for (Relationship r : attribute.getRelationships(Direction.OUTGOING, RelationshipTypes.IN_PATH)) {
                    String path = (String)r.getProperty("path");
                    String[] ids = path.split("[!&]");
                    //String[] rels = path.split("[^&^!]");
                    char[] rels = path.replaceAll("[^&^!]", "").toCharArray();
                    boolean valid = true;

                    if (ids.length > 1) {
                        for (int i = 0; i < rels.length; i++) {
                            if (rels[i] == '&') {
                                if (!attributes.contains(ids[1+i])) {
                                    valid = false;
                                    break;
                                }
                            } else {
                                if (attributes.contains(ids[1+i])) {
                                    valid = false;
                                    break;
                                }
                            }
                        }
                    }

                    if (valid) {
                        paths.add(r.getEndNode());
                    }
                }
            }

            for (Node path : paths) {
                for (Relationship r : path.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_RULE)) {
                    rules.add(r.getEndNode());
                }
            }
            return rules.stream().map(NodeResult::new);
        }

        return null;
    }
}

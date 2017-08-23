package com.maxdemarzi;

import com.maxdemarzi.results.NodeResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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

    @Procedure(name = "com.maxdemarzi.match.user", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.match.user(username) - find matching rules")
    public Stream<NodeResult> matchUser(@Name("username") String username) throws IOException {
        // We start by finding the user
        Node user = db.findNode(Labels.User, "username", username);
        if (user != null) {
            // Gather all of their attributes in to a Set
            Set<Node> userAttributes = new HashSet<>();
            Collection<String> attributes = new HashSet<>();

            for (Relationship r : user.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS)) {
                userAttributes.add(r.getEndNode());
                attributes.add((String)r.getEndNode().getProperty("id"));
            }
            // Find the rules
            Set<Node> rules = findRules(attributes, userAttributes);
            return rules.stream().map(NodeResult::new);
        }

        return null;
    }

    @Procedure(name = "com.maxdemarzi.match.attributes", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.match.attributes([attributes]) - find matching rules")
    public Stream<NodeResult> matchAttributes(@Name("attributes") List<String> attributes) throws IOException {

        // We will gather all of the attribute nodes in to a Set
        Set<Node> userAttributes = new HashSet<>();

        // We start by finding the attributes
        for (String id : attributes) {
            Node attribute = db.findNode(Labels.Attribute, "id", id);
            if (attribute != null) {
                userAttributes.add(attribute);
            }
        }
        // Find the rules
        Set<Node> rules = findRules(attributes, userAttributes);
        return rules.stream().map(NodeResult::new);
    }

    private Set<Node> findRules(@Name("attributes") Collection<String> attributes, Set<Node> userAttributes) {
        Set<Node> rules = new HashSet<>();
        Set<Node> paths = new HashSet<>();

        for (Node attribute : userAttributes) {
            for (Relationship r : attribute.getRelationships(Direction.OUTGOING, RelationshipTypes.IN_PATH)) {
                // Get the "path" property
                String path = (String)r.getProperty("path");

                // Split it up by attribute and by & and !
                String[] ids = path.split("[!&]");
                char[] rels = path.replaceAll("[^&^!]", "").toCharArray();

                // Assume path is valid unless we find a reason to fail it
                boolean valid = true;

                // Since our starting attribute is how we got here we skip checking it.
                if (ids.length > 1) {
                    for (int i = 0; i < rels.length; i++) {
                        if (rels[i] == '&') {
                            // Fail if attribute is not there
                            if (!attributes.contains(ids[1+i])) {
                                valid = false;
                                break;
                            }
                        } else {
                            // Fail if attribute is there but should NOT be
                            if (attributes.contains(ids[1+i])) {
                                valid = false;
                                break;
                            }
                        }
                    }
                }
                // If we made it add it to the set of valid paths
                if (valid) {
                    paths.add(r.getEndNode());
                }
            }
        }
        // For each valid path get the rules
        for (Node path : paths) {
            for (Relationship r : path.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS_RULE)) {
                rules.add(r.getEndNode());
            }
        }
        return rules;
    }
}

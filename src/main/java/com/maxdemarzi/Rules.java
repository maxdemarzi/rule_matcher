package com.maxdemarzi;

import com.maxdemarzi.quine.BooleanExpression;
import com.maxdemarzi.results.MapResult;
import com.maxdemarzi.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class Rules {
    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    // This procedure creates a rule, ex. ("Rule 1", "(0 & 1) | (2 & 3)")
    @Procedure(name = "com.maxdemarzi.rules.create", mode = Mode.WRITE)
    @Description("CALL com.maxdemarzi.rules.create(id, formula) - create a rule")
    public Stream<StringResult> create(@Name("id") String id, @Name("formula") String formula) throws IOException {
        // See if the rule already exists
        Node rule = db.findNode(Labels.Rule, "id", id);
        if (rule == null) {
            // Create the rule
            rule = db.createNode(Labels.Rule);
            rule.setProperty("id", id);
            rule.setProperty("formula", formula);

            // Use the expression to find the required paths
            BooleanExpression boEx = new BooleanExpression(formula);
            boEx.doTabulationMethod();
            boEx.doQuineMcCluskey();
            boEx.doPetricksMethod();

            // Create a relationship from the lead attribute node to the path nodes
            for (String path : boEx.getPathExpressions()) {
                Node pathNode = db.findNode(Labels.Path, "id", path);
                if (pathNode == null) {
                    // Create the path node if it doesn't already exist
                    pathNode = db.createNode(Labels.Path);
                    pathNode.setProperty("id", path);

                    // Create the attribute nodes if they don't already exist
                    String[] attributes = path.split("[!&]");
                    for (int i = 0; i < attributes.length; i++) {
                        String attributeId = attributes[i];
                        Node attribute = db.findNode(Labels.Attribute, "id", attributeId);
                        if (attribute == null) {
                            attribute = db.createNode(Labels.Attribute);
                            attribute.setProperty("id", attributeId);
                        }
                        // Create the relationship between the lead attribute node to the path node
                        if (i == 0) {
                            Relationship inPath = attribute.createRelationshipTo(pathNode, RelationshipTypes.IN_PATH);
                            inPath.setProperty("path", path);
                        }
                    }

                }

                // Create a relationship between the path and the rule
                pathNode.createRelationshipTo(rule, RelationshipTypes.HAS_RULE);
            }
        }

        return Stream.of(new StringResult("Rule " + formula + " created."));
    }

    // This procedure returns attributes needed to satisfy a rule, ex. "(0 & 1) | (2 & 3)", ['2']
    @Procedure(name = "com.maxdemarzi.rules.satisfy", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.rules.satisfy(formula, attributes) - satisfy a rule")
    public Stream<MapResult> satisfy(@Name("formula") String formula, @Name("attributes") List<String> attributes) throws IOException {
        // Use the expression to find the required paths
        BooleanExpression boEx = new BooleanExpression(formula);
        boEx.doTabulationMethod();
        boEx.doQuineMcCluskey();
        boEx.doPetricksMethod();
        ArrayList<Map<String, Set<String>>> results = new ArrayList<>();

        for (String path : boEx.getPathExpressions()) {
            String[] ids = path.split("[!&]");
            char[] rels = path.replaceAll("[^&^!]", "").toCharArray();
            Set<String> missing = new HashSet<>();
            Set<String> remove = new HashSet<>();

            if (!attributes.contains(ids[0])) {
                missing.add(ids[0]);
            }

            if (ids.length > 1) {
                for (int i = 0; i < rels.length; i++) {
                    if (rels[i] == '&') {
                        if (!attributes.contains(ids[1+i])) {
                            missing.add(ids[1+i]);
                        }
                    } else {
                        if (attributes.contains(ids[1+i])) {
                            remove.add(ids[1+i]);
                        }
                    }
                }
            }

            HashMap<String, Set<String>> result = new HashMap<>();
            result.put("missing", missing);
            result.put("remove", remove);
            results.add(result);

        }
        return results.stream().map(MapResult::new);
    }

}

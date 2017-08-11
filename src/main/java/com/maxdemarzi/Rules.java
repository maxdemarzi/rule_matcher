package com.maxdemarzi;

import com.maxdemarzi.quine.BooleanExpression;
import com.maxdemarzi.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.io.IOException;
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
                    // Create the relationship from the first node in the path to the path node.
                    pathNode = db.createNode(Labels.Path);
                    pathNode.setProperty("id", path);

                    String attribute = path.split("[!&]")[0];
                    Node start = db.findNode(Labels.Attribute, "name", attribute);

                    Relationship inPath = start.createRelationshipTo(pathNode, RelationshipTypes.IN_PATH);
                    inPath.setProperty("path", path);
                }

                // Create a relationship between the path and the rule
                pathNode.createRelationshipTo(rule, RelationshipTypes.HAS_RULE);
            }
        }

        return Stream.of(new StringResult("Rule " + formula + " created."));
    }
}

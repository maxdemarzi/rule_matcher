package com.maxdemarzi.schema;

import com.maxdemarzi.Labels;
import com.maxdemarzi.results.StringResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.io.IOException;
import java.util.stream.Stream;

public class Schema {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;

    @Procedure(name = "com.maxdemarzi.schema.generate", mode = Mode.SCHEMA)
    @Description("CALL com.maxdemarzi.schema.generate() - generate schema")

    public Stream<StringResult> generate() throws IOException {
        org.neo4j.graphdb.schema.Schema schema = db.schema();
        if (!schema.getIndexes(Labels.Attribute).iterator().hasNext()) {
            schema.constraintFor(Labels.Attribute)
                    .assertPropertyIsUnique("name")
                    .create();
        }
        if (!schema.getIndexes(Labels.Path).iterator().hasNext()) {
            schema.constraintFor(Labels.Path)
                    .assertPropertyIsUnique("id")
                    .create();
        }
        if (!schema.getIndexes(Labels.Rule).iterator().hasNext()) {
            schema.constraintFor(Labels.Rule)
                    .assertPropertyIsUnique("id")
                    .create();
        }
        if (!schema.getIndexes(Labels.User).iterator().hasNext()) {
            schema.constraintFor(Labels.User)
                    .assertPropertyIsUnique("username")
                    .create();
        }
        return Stream.of(new StringResult("Schema Generated"));
    }
}

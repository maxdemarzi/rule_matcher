package com.maxdemarzi;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertEquals;

public class RulesTest {

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withProcedure(Rules.class);

    @Test
    public void testCreateRule() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY1);
        String results = response.get("results").get(0).get("data").get(0).get("row").get(0).asText();
        assertEquals("Rule (a1 & a2) | (a3 & a4) created.", results);
    }

    private static final Map QUERY1 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.rules.create('first rule', '(a1 & a2) | (a3 & a4)') yield value return value")));


    @Test
    public void testCreateRuleWithNot() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY2);
        String results = response.get("results").get(0).get("data").get(0).get("row").get(0).asText();
        assertEquals("Rule (a1 & a2) | (a3 & !a4) created.", results);
    }

    private static final Map QUERY2 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.rules.create('second rule', '(a1 & a2) | (a3 & !a4)') yield value return value")));

    @Test
    public void testCreateRuleWithMissingAttributes() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY3);
        String results = response.get("results").get(0).get("data").get(0).get("row").get(0).asText();
        assertEquals("Rule (a1 & a2) | (a3 & a7) created.", results);
    }

    private static final Map QUERY3 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.rules.create('second rule', '(a1 & a2) | (a3 & a7)') yield value return value")));


    private static final String MODEL_STATEMENT =
            "CREATE (user1:User {username:'max'})" +
            "CREATE (user2:User {username:'jeff'})" +
            "CREATE (user3:User {username:'srikant'})" +
            "CREATE (user4:User {username:'stephen'})" +
            "CREATE (user5:User {username:'todd'})" +
            "CREATE (user6:User {username:'tyler'})" +

            "CREATE (a1:Attribute {id:'a1'})" +
            "CREATE (a2:Attribute {id:'a2'})" +
            "CREATE (a3:Attribute {id:'a3'})" +
            "CREATE (a4:Attribute {id:'a4'})" +
            "CREATE (a5:Attribute {id:'a5'})" +
            "CREATE (a6:Attribute {id:'a6'})" +

            "CREATE (user1)-[:HAS]->(a1)" +
            "CREATE (user1)-[:HAS]->(a2)" +

            "CREATE (user2)-[:HAS]->(a3)" +
            "CREATE (user2)-[:HAS]->(a4)" +

            "CREATE (user3)-[:HAS]->(a1)" +
            "CREATE (user3)-[:HAS]->(a3)" +

            "CREATE (user4)-[:HAS]->(a1)" +

            "CREATE (user5)-[:HAS]->(a5)" +

            "CREATE (user6)-[:HAS]->(a5)" +
            "CREATE (user6)-[:HAS]->(a6)";
}

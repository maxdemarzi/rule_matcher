package com.maxdemarzi;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertEquals;

public class MatchAttributesTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withFixture(MODEL_STATEMENT)
            .withProcedure(Matcher.class);

    @Test
    public void testMatcher() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY1);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(2, count);
        Map rule1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), Map.class);
        assertEquals("(a1&a2) | (a3&a4)", rule1.get("id"));
        Map rule2 = mapper.convertValue(response.get("results").get(0).get("data").get(1).get("row").get(0), Map.class);
        assertEquals("(a1&a2) | (a3& !a4)", rule2.get("id"));
    }

    private static final Map QUERY1 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.match.attributes(['a1','a2']) yield node return node")));


    @Test
    public void testMatcherNotRule() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY2);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(1, count);
        Map rule2 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), Map.class);
        assertEquals("(a1&a2) | (a3& !a4)", rule2.get("id"));
    }

    private static final Map QUERY2 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.match.attributes(['a1','a3']) yield node return node")));

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
                    "CREATE (user6)-[:HAS]->(a6)" +

                    "CREATE (p1:Path {id:'a1&a2'})" +
                    "CREATE (p2:Path {id:'a3&a4'})" +
                    "CREATE (r1:Rule {id:'(a1&a2) | (a3&a4)'})" +
                    "CREATE (a1)-[:IN_PATH {path:'a1&a2'}]->(p1)" +
                    "CREATE (a3)-[:IN_PATH {path:'a3&a4'}]->(p2)" +
                    "CREATE (p1)-[:HAS_RULE]->(r1)" +
                    "CREATE (p2)-[:HAS_RULE]->(r1)" +
                    "CREATE (r2:Rule {id:'(a1&a2) | (a3& !a4)'})" +
                    "CREATE (p1)-[:HAS_RULE]->(r2)" +
                    "CREATE (p3:Path {id:'a3!a4'})" +
                    "CREATE (a3)-[:IN_PATH {path:'a3!a4'}]->(p3)" +
                    "CREATE (p3)-[:HAS_RULE]->(r2)";

}

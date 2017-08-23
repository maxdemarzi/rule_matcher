package com.maxdemarzi;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.harness.junit.Neo4jRule;
import org.neo4j.test.server.HTTP;

import java.util.ArrayList;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertEquals;

public class RulesSatisfyTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withProcedure(Rules.class);

    @Test
    public void testQualifyRule() throws Exception {
        HTTP.Response response = HTTP.POST(neo4j.httpURI().resolve("/db/data/transaction/commit").toString(), QUERY1);
        int count = response.get("results").get(0).get("data").size();
        assertEquals(2, count);
        Map path1 = mapper.convertValue(response.get("results").get(0).get("data").get(0).get("row").get(0), Map.class);
        assertEquals(new ArrayList<String>() {{add("a2");}}, path1.get("missing"));
        assertEquals(new ArrayList<String>(), path1.get("remove"));
        Map path2 = mapper.convertValue(response.get("results").get(0).get("data").get(1).get("row").get(0), Map.class);
        assertEquals(new ArrayList<String>() {{add("a3");}}, path2.get("missing"));
        assertEquals(new ArrayList<String>() {{add("a4");}}, path2.get("remove"));
    }

    private static final Map QUERY1 =
            singletonMap("statements", singletonList(singletonMap("statement",
                    "CALL com.maxdemarzi.rules.satisfy('(a1 & a2) | (a3 & !a4)', ['a1', 'a4']) yield value return value")));
}

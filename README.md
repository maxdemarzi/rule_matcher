# Rule Matcher
Rule Matcher Stored Procedures

This project requires Neo4j 3.2.x

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/matcher-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/rule-matcher-1.0-SNAPSHOT.jar neo4j-enterprise-3.2.3/plugins/.


Edit your Neo4j/conf/neo4j.conf file by adding this line:

    dbms.security.procedures.unrestricted=com.maxdemarzi.*    

Restart your Neo4j Server.

Create the Schema by running this stored procedure:

    CALL com.maxdemarzi.schema.generate
    
Create some test data:

    CREATE (user1:User {username:'max'})
    CREATE (user2:User {username:'jeff'})
    CREATE (user3:User {username:'srikant'})
    CREATE (user4:User {username:'stephen'})
    CREATE (user5:User {username:'todd'})
    CREATE (user6:User {username:'tyler'})
    
    CREATE (a1:Attribute {id:'a1'})
    CREATE (a2:Attribute {id:'a2'})
    CREATE (a3:Attribute {id:'a3'})
    CREATE (a4:Attribute {id:'a4'})
    CREATE (a5:Attribute {id:'a5'})
    CREATE (a6:Attribute {id:'a6'})
    
    CREATE (user1)-[:HAS]->(a1)
    CREATE (user1)-[:HAS]->(a2)
    
    CREATE (user2)-[:HAS]->(a3)
    CREATE (user2)-[:HAS]->(a4)
    
    CREATE (user3)-[:HAS]->(a1)
    CREATE (user3)-[:HAS]->(a3)
    
    CREATE (user4)-[:HAS]->(a1)
    
    CREATE (user5)-[:HAS]->(a5)
    
    CREATE (user6)-[:HAS]->(a5)
    CREATE (user6)-[:HAS]->(a6)
    

Create a rule:
    
    CALL com.maxdemarzi.rules.create('Rule 1', '(a1 & a2) | (a3 & a4)')  # (a1 and a2) or (a3 and a4)
    CALL com.maxdemarzi.rules.create('Rule 2', '(a1 & a2) | (a3 & !a4)') # (a1 and a2) or (a3 and NOT a4)

Match a user:

    CALL com.maxdemarzi.match.user('max') yield node return node
    CALL com.maxdemarzi.match.user('srikant') yield node return node

Match attributes:

    CALL com.maxdemarzi.match.attributes(['a1','a2']) yield node return node
    CALL com.maxdemarzi.match.attributes(['a1','a3']) yield node return node


See if attributes satisfy a rule:

    CALL com.maxdemarzi.rules.satisfy('(a1 & a2) | (a3 & !a4)', ['a1', 'a4']) yield value return value
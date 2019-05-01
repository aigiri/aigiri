CREATE (a:Person {firstname:'Amitabh', lastname:'Bachchan'})
CREATE (j:Person {firstname:'Jaya', lastname:'Bachchan'})
CREATE (a1:Person {firstname:'Abhishek', lastname:'Bachchan'})

CREATE (h:Person {firstname:'Harivansh Rai', lastname:'Bachchan'})
CREATE (t:Person {firstname:'Teji', lastname:'Bachchan'})
CREATE (h)-[:SPOUSE]->(t)
CREATE (a)-[:PARENT]->(h)
CREATE (a)-[:PARENT]->(t)

 
CREATE (a)-[:SPOUSE]->(j)
CREATE (a1)-[:PARENT]->(a)
CREATE (a1)-[:PARENT]->(j)

CREATE (i:Person {firstname:'Aishwarya', lastname:'Bachchan'})
CREATE (k:Person {firstname:'Krishnaraj', lastname:'Rai'})
CREATE (v:Person {firstname:'Vrinda', lastname:'Rai'})

CREATE (a1)-[:SPOUSE]->(i)
CREATE (k)-[:SPOUSE]->(v)
CREATE (i)-[:PARENT]->(k)
CREATE (i)-[:PARENT]->(v)

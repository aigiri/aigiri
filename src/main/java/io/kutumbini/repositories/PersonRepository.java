package io.kutumbini.repositories;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import io.kutumbini.domain.entity.Person;

//@RepositoryRestResource()
public interface PersonRepository extends Neo4jRepository<Person, Long> {
	
	List<Person> findAll();
	
	List<Person> findByUserIdIn(Iterable<Long> delegatorIds);

	Iterable<Long> deleteByUserIdIn(Iterable<Long> id);
	
	@Query("MATCH (p:Person {id: {0}, userId: {1}}) DETACH DELETE p")
	void delete(Long id, Long userId);
	
	// TODO ygiri make sure delegation results are based on the direction of the DELEGATE relation
    // persons owned by the user or delegated to them by another user
//    @Query("MATCH (p:Person)--(:User {email: {0}})  OPTIONAL MATCH (q:Person)--(:User)-[*1..2]->(:User {email: {0}}) RETURN p,q")
//    @Query("MATCH (:Person)-[r*0..1]-(p:Person)--(:User)-[*0..2]->(:User {email: {0}}) RETURN p,r")
//    @Query("MATCH (p:Person)--(:User)-[*0..2]->(:User {email: {0}}) RETURN p")
//	List<Person> userEditableFamily(String userEmail);

    // persons owned by the user and their relations
//    @Query("MATCH (:User {email: {0}})--(p:Person)-[r*]-(q:Person) RETURN p,q,r LIMIT {1}")
//    @Query("MATCH (:User {email: {0}})--(:Person)-[*]-(p:Person) RETURN p LIMIT {1}")
//    @Query("MATCH (:User {email: {0}})-[r:OWNED_BY]-(p:Person) RETURN p,r LIMIT {1}")
//    @Query("MATCH (:User {email: {0}})--(p:Person)-[r*]-(q:Person) RETURN p,q,r LIMIT {1}")
//	List<Person> userExtendedFamily(String userEmail, int limit);
    
//    @Query("MATCH (p:Person {firstname: {1}, lastname: {2}})--(:User)-[*0..2]->(:User {email: {0}}) RETURN p")
//    List<Person> findPersons(String userEmail, String firstname, String lastname);
//
//    @Query("MATCH (:Family {nodeId: {1}})-[:CHILD]->(p:Person {firstname: {2}, lastname: {3}})--(:User)-[*0..2]->(:User {email: {0}}) RETURN p")
//	List<Person> findChild(String email, Long familyNodeId, String firstname, String lastname);
    
}
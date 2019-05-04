package io.kutumbini.repositories;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.kutumbini.domain.entity.Person;

@RepositoryRestResource()
public interface FamilyRepository extends Neo4jRepository<Person, Long> {
	
	// TODO ygiri make sure delegation results are based on the direction of the DELEGATE relation
    // persons owned by the user or delegated to them by another user
//    @Query("MATCH (p:Person)--(:User {email: {0}})  OPTIONAL MATCH (q:Person)--(:User)-[*1..2]->(:User {email: {0}}) RETURN p,q")
//    @Query("MATCH (:Person)-[r*0..1]-(p:Person)--(:User)-[*0..2]->(:User {email: {0}}) RETURN p,r")
    @Query("MATCH (p:Person)--(:User)-[*0..2]->(:User {email: {0}}) RETURN p")
	List<Person> userEditableFamily(String userEmail);

    // persons owned by the user and their relations
    @Query("MATCH (:User {email: {0}})--(p:Person)-[r*]-(q:Person) RETURN p,q,r LIMIT {1}")
//    @Query("MATCH (:User {email: {0}})--(:Person)-[*]-(p:Person) RETURN p LIMIT {1}")
	List<Person> userExtendedFamily(String userEmail, int limit);
    
    @Query("MATCH (p:Person {firstname: {1}, lastname: {2}})--(:User)-[*0..2]->(:User {email: {0}}) RETURN p")
    List<Person> findPersons(String userEmail, String firstname, String lastname);
    
}
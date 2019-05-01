package io.kutumbini.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.kutumbini.domain.entity.Person;

@RepositoryRestResource()
public interface PersonRepository extends Neo4jRepository<Person, Long> {
	
	// TODO ygiri document what the user experience is when the 'limit' is exceeded in a query result
	// TODO ygiri Define App Events to track occurrences and log them, e.g., an occurrence of 'limit' breach
	//@Depth(2)
    @Query("MATCH (p:Person)-[r*0..1]-(:Person) RETURN p,r LIMIT {limit}")
	List<Person> persons(@Param("limit") int limit);

    @Query("MATCH (p:Person {lastname: {lastname}}) RETURN p")
	Collection<Person> findByLastname(@Param("lastname") String lastname);

    // persons owned by the user or delegated to them by another user
//    @Query("MATCH (p:Person)--(:User {email: {0}})  OPTIONAL MATCH (q:Person)--(:User)-[*1..2]->(:User {email: {0}}) RETURN p,q")
    @Query("MATCH (:Person)-[r*0..1]-(p:Person)--(:User)-[*0..2]->(:User {email: {0}}) RETURN p,r")
	List<Person> userEditableFamily(String userEmail);

    // persons owned by the user and their relations
    @Query("MATCH (u:User {email: {0}})--(p:Person)-[r*]-(q:Person) RETURN p,q,r LIMIT {1}")
	List<Person> userExtendedFamily(String userEmail, int limit);
    
    @Query("MATCH (p:Person {firstname: {1}, lastname: {2}})--(:User)-[*0..2]->(:User {email: {0}}) RETURN p")
    Optional<Person> findPerson(String userEmail, String firstname, String lastname);
    
}
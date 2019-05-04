package io.kutumbini.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.kutumbini.domain.entity.Person;

@RepositoryRestResource()
public interface PublicRepository extends Neo4jRepository<Person, Long> {
	
	// TODO ygiri document what the user experience is when the 'limit' is exceeded in a query result
	// TODO ygiri Define App Events to track occurrences and log them, e.g., an occurrence of 'limit' breach
	//@Depth(2)
    @Query("MATCH (p:Person)-[r*0..1]-(:Person) RETURN p,r LIMIT {limit}")
	List<Person> persons(@Param("limit") int limit);

    @Query("MATCH (p:Person {lastname: {lastname}}) RETURN p")
	Collection<Person> findByLastname(@Param("lastname") String lastname);    
}
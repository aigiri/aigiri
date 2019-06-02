package io.kutumbini.repositories;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import io.kutumbini.domain.entity.Family;

//@RepositoryRestResource()
public interface FamilyRepository extends Neo4jRepository<Family, Long> {
	
//	@Depth(3)
	List<Family> findByUserId(long userId);
	
//	@Query("MATCH (f:Family)-[r]-(:Person) RETURN f,r,p") // without the @Query this runs into OOM Exception
    Iterable<Family> findAll();
	
//	@Query("MATCH (:Person)<-[:PARENT]-(f:Family)-[:PARENT]->(:Person)--(:User)-[*0..2]->(:User {email: {0}}) RETURN f")
//	@Query("MATCH (:User {email: {0}})<-[*0..2]-(:User)--(f:Family)--(p:Person) RETURN f,p")
	List<Family> findByUserIdIn(Iterable<Long> userIds);

	List<Family> findByIdIn(Iterable<Long> idsList);

	//	@Query("MATCH (:Person)<-[:PARENT]-(f:Family)-[:PARENT]->(:Person)--(:User)-[*0..2]->(:User {email: {0}}) RETURN f")
//	@Query("MATCH (f:Family {userId: {0}}) "
//			+ "UNION MATCH (f:Family {userId: {0}}), (f)-[*]-(g:Family) "
//			+ "RETURN f.id")
	@Query("MATCH (f:Family {userId: {0}})-[*]-(g:Family) RETURN f,g")
	List<Family> findConnectedFamilyIds(long userId);

	@Query("MATCH (f:Family)-[:PARENT]-(:Person {firstname: {1}, lastname: {2}}), (f:Family)-[:PARENT]-(:Person {firstname: {3}, lastname: {4}})"
			+ " WHERE f.userId = {0} RETURN f")
	List<Family> find(long userId, String husbandFirstname, String husbandLastname, String wifeFirstname, String wifeLastname);

}
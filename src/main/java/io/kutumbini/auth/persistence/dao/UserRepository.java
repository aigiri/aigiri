package io.kutumbini.auth.persistence.dao;

import java.util.Optional;

import org.springframework.data.neo4j.annotation.Depth;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;

import io.kutumbini.auth.persistence.model.User;

public interface UserRepository extends Neo4jRepository<User, Long> {

//    @Query("MATCH (u:User) WHERE u.email = {email} RETURN u")
//	@Depth(5)
	User findByEmail(@Param("email") String email);
    
	// includes self
//    @Query("MATCH (u:User)-[:DELEGATE_FULL*0..2]->(:User {email: {0}}) RETURN u")
	@Depth(2)
	Optional<User> findById(long id);

}

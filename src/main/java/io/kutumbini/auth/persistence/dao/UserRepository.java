package io.kutumbini.auth.persistence.dao;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Service;

import io.kutumbini.auth.persistence.model.User;

@Service
@RepositoryRestResource()
public interface UserRepository extends Neo4jRepository<User, Long> {

    @Query("MATCH (u:User) WHERE u.email = {email} RETURN u")
	public User findByEmail(@Param("email") String email);
    
    // includes self
    @Query("MATCH (u:User)-[:DELEGATE_FULL*0..2]->(:User {email: {0}}) RETURN u")
	List<User> twoDegreeDelegatees(String userEmail);

}

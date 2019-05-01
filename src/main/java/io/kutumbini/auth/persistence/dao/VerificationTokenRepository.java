package io.kutumbini.auth.persistence.dao;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.auth.persistence.model.VerificationToken;

@RepositoryRestResource()
public interface VerificationTokenRepository extends Neo4jRepository<VerificationToken, Long> {

    public VerificationToken findByToken(String token);

    public VerificationToken findByUser(User user);

//    public Stream<VerificationToken> findAllByExpiryDateLessThan(Date now);

//    public void deleteByExpiryDateLessThan(Date now);

//    public void deleteAllExpiredSince(Date now);

}

package io.kutumbini.auth.persistence.dao;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import io.kutumbini.auth.persistence.model.GrantVerificationToken;
import io.kutumbini.auth.persistence.model.User;

// TODO ygiri set a schedule to purge periodically, or keep for grant audits indefinitely?
@RepositoryRestResource()
public interface GrantVerificationTokenRepository extends Neo4jRepository<GrantVerificationToken, Long> {

    public GrantVerificationToken findByToken(String token);

    public GrantVerificationToken findByGrantor(User grantor);

//    public Stream<GrantVerificationToken> findAllByExpiryDateLessThan(Date now);

//    public void deleteByExpiryDateLessThan(Date now);

//    public void deleteAllExpiredSince(Date now);

}

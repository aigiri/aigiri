package io.kutumbini.test;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.kutumbini.auth.persistence.dao.VerificationTokenRepository;
import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.auth.persistence.model.VerificationToken;
import io.kutumbini.config.AppConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppConfig.class)
@DataNeo4jTest
@Transactional
public class VerificationTokenRepositoryTest {

	@Autowired
	private VerificationTokenRepository tokenRepository;

	@Autowired
	private TestData data;

	@Before
	public void setUp() {
		data.setup();
	}

	@Test
	public void save() {
		final String uuid = UUID.randomUUID().toString();
		User user = data.u_amitabh;
		final VerificationToken token = new VerificationToken(uuid, user);
		tokenRepository.save(token);
		
		long tokenCount = tokenRepository.count();
		assertEquals(1, tokenCount);
	}

}
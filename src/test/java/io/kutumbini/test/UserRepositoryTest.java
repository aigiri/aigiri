package io.kutumbini.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.kutumbini.auth.persistence.dao.UserRepository;
import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.config.AppConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppConfig.class)
@DataNeo4jTest
@Transactional
public class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JUnitTestData data;

	@Before
	public void setUp() {
		data.setup();
	}

	@Test
	public void findByEmail() {
		User user = userRepository.findByEmail(data.u_abhishek.getEmail());
		assertEquals(data.u_abhishek, user);
	}

	@Test
	public void twoDegreeDelegatees() {
		Optional<User> u_abhishek_delegatees = userRepository.findById(data.u_abhishek.getId());
		assertNotNull(u_abhishek_delegatees.get().getDelegatedInComingFull().iterator().next().getDelegatedInComingFull().size());
	}

}
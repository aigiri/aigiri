package io.kutumbini.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import io.kutumbini.domain.entity.Person;
import io.kutumbini.repositories.PersonRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppConfig.class)
@DataNeo4jTest
@Transactional
public class PersonRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private JUnitTestData data;
	
	@Before
	public void setUp() {
		data.setup();
	}

//	@Test
//	public void userRequired() {
//		// should not be able to save Person without User set on it
//		String lastname = "Doe";
//		personRepository.save(new Person(null, lastname, null));
//		Collection<Person> result = publicRepository.findByLastname(lastname);
//		assertEquals("result size", 0, result.size());
//	}
	
	@Test
	public void addAndDeletePerson() {
		User user = new User();
		user.setEmail("usr@ema.il");
		userRepository.save(user);
		
		Person p = new Person(user.getId());
		personRepository.save(p);
		long id = p.getId();
		assertTrue(id>0);
		
		Optional<Person> q = personRepository.findById(id);
		assertTrue(id == q.get().getId());
		
		personRepository.delete(p);
		Optional<Person> r = personRepository.findById(id);
		assertFalse(r.isPresent());
	}
	
}
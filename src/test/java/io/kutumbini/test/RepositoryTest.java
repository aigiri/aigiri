package io.kutumbini.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
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
public class RepositoryTest {

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TestData data;

	@Before
	public void setUp() {
		data.setup();
	}

	@Test
	public void users() {
		long userCount = userRepository.count();
		assertEquals(5, userCount);
	}

	@Test
	public void persons() {
		Collection<Person> persons = personRepository.persons(1000);
		assertEquals(4, persons.size());
	}

	@Test
	public void userRequired() {
		// should not be able to save Person without User set on it
		String lastname = "Doe";
		personRepository.save(new Person(null, lastname, null));
		Collection<Person> result = personRepository.findByLastname(lastname);
		assertEquals("result size", 0, result.size());
	}
	
	@Test
	public void testFindByLastname() {
		String lastname = "Bachchan";
		Collection<Person> result = personRepository.findByLastname(lastname);
		assertEquals("result size", 3, result.size());
	}

	@Test
	public void twoDegreeDelegatees() {
		Collection<User> u_abhishek_delegatees = userRepository.twoDegreeDelegatees(data.u_abhishek.getEmail());
		assertEquals("u_abhishek_delegatees", 3, u_abhishek_delegatees.size());
	}

	@Test
	public void userEditableFamily() {
		Collection<Person> u_amitabh_persons = personRepository.userEditableFamily(data.u_amitabh.getEmail());
		assertEquals("u_amitabh_persons", 1, u_amitabh_persons.size());

		Collection<Person> u_abhishek_persons = personRepository.userEditableFamily(data.u_abhishek.getEmail());
		assertEquals("u_abhishek_persons", 3, u_abhishek_persons.size());

		Optional<Person> optional = u_abhishek_persons.stream().filter(p -> p.getFirstname().equals("Abhishek")).findAny();
		assertTrue(optional.isPresent());
		assertEquals("relations", 2, optional.get().getRelations().size());

		Optional<Person> optnl = u_abhishek_persons.stream().filter(p -> p.getFirstname().equals("Amitabh")).findAny();
		assertTrue(optnl.isPresent());
		assertEquals("relations", 1, optnl.get().getRelations().size());
	}

	@Test
	public void userExtendedFamily() {
		Collection<Person> u_amitabh_persons = personRepository.userExtendedFamily(data.u_amitabh.getEmail(), 1000);
		assertEquals("u_amitabh_persons", 3, u_amitabh_persons.size());

		Optional<Person> optional = u_amitabh_persons.stream().filter(p -> p.getFirstname().equals("Abhishek")).findAny();
		assertTrue(optional.isPresent());
		assertEquals("relations", 2, optional.get().getRelations().size());
}

}
package io.kutumbini.test;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.kutumbini.config.AppConfig;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.repositories.PersonRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppConfig.class)
@DataNeo4jTest
@Transactional
public class PersonRepositoryTest {

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private JUnitTestData data;

	@Before
	public void setUp() {
		data.setup();
	}

	@Test
	public void findPersons() {
		List<Person> persons = personRepository.findPersons(data.u_amitabh.getEmail(), "Amitabh", "Bachchan");
		assertEquals(1, persons.size());
	}
	
	@Test
	public void userEditableFamily() {
		Collection<Person> u_amitabh_persons = personRepository.userEditableFamily(data.u_amitabh.getEmail());
		assertEquals("u_amitabh_persons", 1, u_amitabh_persons.size());

		Collection<Person> u_abhishek_persons = personRepository.userEditableFamily(data.u_abhishek.getEmail());
		assertEquals("u_abhishek_persons", 3, u_abhishek_persons.size());
	}

	@Test
	public void userExtendedFamily() {
		Collection<Person> persons = personRepository.userExtendedFamily(data.u_amitabh.getEmail(), 1000);
		assertEquals(3, persons.size());
	}

}
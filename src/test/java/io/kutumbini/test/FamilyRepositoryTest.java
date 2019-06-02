package io.kutumbini.test;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import io.kutumbini.config.AppConfig;
import io.kutumbini.domain.entity.Family;
import io.kutumbini.repositories.FamilyRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppConfig.class)
@DataNeo4jTest
@Transactional
public class FamilyRepositoryTest {

	@Autowired
	private FamilyRepository familyRepository;

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
	public void find() {
		String husbandFirstname = "Amitabh";
		String husbandLastname = "Bachchan";
		String wifeFirstname = "Jaya";
		String wifeLastname = "Bachchan";
		List<Family> families = familyRepository.find(data.u_amitabh.getId(), husbandFirstname, husbandLastname, wifeFirstname, wifeLastname);
		assertEquals(1, families.size());
	}
	
	@Test
	public void findAll() {
		Iterable<Family> families = familyRepository.findAll();
		AtomicInteger index = new AtomicInteger(0);
		families.forEach(f -> index.getAndIncrement());
		assertEquals(3, index.get());
	}
		
	@Test
	public void findOwnedFamilies() {
		List<Family> families = familyRepository.findByUserId(data.u_amitabh.getId());
		assertEquals(2, families.size());
	}
		
	@Test
	public void findOwnedFamilies1() {
		List<Family> families = familyRepository.findByUserId(data.u_abhishek.getId());
		assertEquals(1, families.size());
	}

}
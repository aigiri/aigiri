package io.kutumbini.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
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

import io.kutumbini.auth.persistence.dao.UserRepository;
import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.config.AppConfig;
import io.kutumbini.domain.entity.Family;
import io.kutumbini.domain.entity.Gender;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.repositories.FamilyRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppConfig.class)
@DataNeo4jTest
@Transactional
public class FamilyRepositoryTest {

	@Autowired
	private UserRepository userRepository;

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
		assertEquals(5, index.get());
	}
		
	@Test
	public void findByUserId() {
		List<Family> families = familyRepository.findByUserId(data.u_amitabh.getId());
		assertEquals(2, families.size());
	}
		
	@Test
	public void findByUserId1() {
		List<Family> families = familyRepository.findByUserId(data.u_abhishek.getId());
		assertEquals(1, families.size());
	}
	
	@Test
	public void findConnectedFamilyIds() {
		User user = new User();
		user.setEmail("loadtester@k");
		userRepository.save(user);
		Family family = new Family();
		family.setUserId(user.getId());
		Person h = new Person("hf", "hl", Gender.M);
		Person w = new Person("wf", "wl", Gender.F);
		family.addParent(h);
		family.addParent(w);
		familyRepository.save(family);
		List<Family> ids = familyRepository.findConnectedFamilyIds(user.getId());
		assertEquals(1, ids.size());
	}

	@Test
	public void findConnectedFamilyIds2() {
		List<Family> ids = familyRepository.findConnectedFamilyIds(data.u_aishwarya.getId());
		assertEquals(4, ids.size());
	}

	@Test
	public void findByUserIdIn() {
		List<Family> ids = familyRepository.findByUserIdIn(Arrays.asList(new Long[]{data.u_aishwarya.getId(), data.u_abhishek.getId()}));
		assertEquals(2, ids.size());
	}

	@Test
	public void findByIdIn() {
		List<Family> families = familyRepository.findByUserId(data.u_amitabh.getId());
		Long fid1 = families.get(0).getId();
//		List<Family> ids = familyRepository.findById(fid1);
//		assertEquals(2, ids.size());
	}

}
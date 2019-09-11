package io.kutumbini.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import io.kutumbini.domain.entity.Person;
import io.kutumbini.repositories.FamilyRepository;
import io.kutumbini.services.FamilyTreeService;

// @Configuration class holding property values. In this case, for me, it worked using the @ContextConfiguration, 
// plus the @TestPropertySource("prop-file") and @EnableConfigurationProperties(Conf.class) – acaruci Nov 7 '18 at 13:01 

@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootTest(classes = AppConfig.class)
@DataNeo4jTest
@Transactional
public class FamilyTreeServiceTest {

	@Autowired
	private FamilyTreeService familyTreeService;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FamilyRepository familyRepository;

	// this does the data setup
	@Autowired
	private JUnitTestData data;
	
	@Before
	public void setUp() {
		data.setup();
	}
	
	@Test
	public void updateFamily() {
		Person p1 = familyTreeService.createPerson(data.u_homer);
		Person p2 = familyTreeService.createPerson(data.u_homer);
		Person p3 = familyTreeService.createPerson(data.u_homer);
		Family family = new Family(data.u_homer.getId());
		family.addChild(p1);
		family.addParent(p2);
		family.addParent(p3);
		Family f = familyTreeService.saveFamily(family);
		// modify family and save
		f.getParents().remove(p3);
		List<Family> families = new ArrayList<Family>();
		families.add(f);
		familyTreeService.saveFamilies(families);
		List<Family> families2 = familyTreeService.getEditableFamlies(data.u_homer);
		Family f2 = families2.stream().filter(g -> g.getId().longValue() == f.getId().longValue()).findAny().orElse(null);
		assertEquals(1, f2.getParents().size());
	}
 
	@Test
	public void addDeletePerson() {
		List<Person> persons = familyTreeService.getEditablePersons(data.u_homer);
		Person p = familyTreeService.createPerson(data.u_homer);
		List<Person> personsAfterAdd = familyTreeService.getEditablePersons(data.u_homer);
		assertEquals("after add", persons.size() + 1, personsAfterAdd.size());
		familyTreeService.deletePerson(p.getId(), data.u_homer);
		List<Person> personsAfterDelete = familyTreeService.getEditablePersons(data.u_homer);
		assertEquals("after delete", persons.size(), personsAfterDelete.size());
	}
 
//	@Test(expected = ValidationException.class)
//	public void illegalParentCycle() {
//		List<Person> persons = familyRepository.findPersons(data.u_abhishek.getEmail(), "Abhishek", "Bachchan");
//		assertTrue(persons.size() == 1);
//		Person abhishek = persons.get(0);
//		
//		Optional<Person> optional1 = abhishek.getParents().stream().filter(p -> p.getFirstname().equals("Amitabh")).findAny();
//		assertTrue(optional1.isPresent());
//		Person amitabh = optional1.get();
//		
//		// illegal
//		amitabh.addParent(abhishek);
//	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void publicTreeD3() {
		Map<String, Object> map = familyTreeService.publicTreeD3();
		Collection nodes = (Collection) map.get("nodes");
		Collection links = (Collection) map.get("links");
		assertEquals("number of nodes", 19, nodes.size());
		assertEquals("number of links", 17, links.size());
	}
	
//	@Test
//	public void userEditableTreeD3() {
//		Map<String, Object> map = familyTreeService.viewExtendedFamilyData(data.u_abhishek);
//		Collection nodes = (Collection) map.get("nodes");
//		Collection links = (Collection) map.get("links");
//		assertEquals("number of nodes", 10, nodes.size());
//		assertEquals("number of links", 9, links.size());
//	}
//
	@Test
	public void userExtendedFamilyData() {
		Map<String, Object> map = familyTreeService.viewExtendedFamilyData(data.u_abhishek);
		Collection nodes = (Collection) map.get("nodes");
		Collection links = (Collection) map.get("links");
		assertEquals("number of nodes", 13, nodes.size());
		assertEquals("number of links", 12, links.size());
	}

	@Test
	public void userExtendedFamilyData2() {
		User user = new User();
		user.setEmail("ramakrishna@ntr");
		userRepository.save(user);
		String husbandFirstname = "Taraka Rama Rao";
		String husbandLastname = "Nandamuri";
		String wifeFirstname = "Basavatarakam";
		String wifeLastname = "Nandamuri";
		familyTreeService.createFamily(user, husbandFirstname, husbandLastname, wifeFirstname, wifeLastname);
		Map<String, Object> map = familyTreeService.viewExtendedFamilyData(user);
		Collection nodes = (Collection) map.get("nodes");
		Collection links = (Collection) map.get("links");
		assertEquals("number of nodes", 3, nodes.size());
		assertEquals("number of links", 2, links.size());
	}
	
	@Test
	public void createFamily() {
		User user = new User();
		user.setEmail("ramakrishna@ntr");
		userRepository.save(user);
		String husbandFirstname = "Taraka Rama Rao";
		String husbandLastname = "Nandamuri";
		String wifeFirstname = "Basavatarakam";
		String wifeLastname = "Nandamuri";
		familyTreeService.createFamily(user, husbandFirstname, husbandLastname, wifeFirstname, wifeLastname);
		List<Family> families = familyRepository.find(user.getId(), husbandFirstname, husbandLastname, wifeFirstname, wifeLastname);
		assertEquals(1, families.size());
	}
	
	@Test
	public void getDelegatorIds() {
		Set<Long> ids = familyTreeService.getDelegatorIds(data.u_abhishek);
		assertEquals(3, ids.size());
	}
	
	@Test
	public void findConnectedFamilyIds() {
		List<Family> ids = familyRepository.findConnectedFamilyIds(data.u_homer.getId());
		assertEquals(1, ids.size());
	}
	
	@Test
	public void findConnectedFamilyIds1() {
		List<Family> ids = familyRepository.findConnectedFamilyIds(data.u_abhishek.getId());
		assertEquals(4, ids.size());
	}
	
	@Test
	public void findConnectedFamilyIds2() {
		List<Family> ids = familyRepository.findConnectedFamilyIds(data.u_aishwarya.getId());
		assertEquals(4, ids.size());
	}
	
}
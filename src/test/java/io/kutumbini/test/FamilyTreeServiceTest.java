package io.kutumbini.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

//import com.fasterxml.jackson.databind.ObjectMapper;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.config.AppConfig;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.repositories.FamilyRepository;
import io.kutumbini.repositories.PublicRepository;
import io.kutumbini.services.FamilyTreeService;
import io.kutumbini.validation.ValidationException;
import io.kutumbini.web.data.KNode;

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
	private FamilyRepository familyRepository;
	
	@Autowired
	private PublicRepository publicRepository;
	
	// this does the data setup
	@Autowired
	private TestData data;
	
	@Before
	public void setUp() {
		data.setup();
	}
 
	@Test
	public void findPersons() {
		List<Person> persons = familyRepository.findPersons(data.u_amitabh.getEmail(), "Amitabh", "Bachchan");
		assertEquals(1, persons.size());
	}
	
	@Test(expected = ValidationException.class)
	public void illegalParentCycle() {
		List<Person> persons = familyRepository.findPersons(data.u_abhishek.getEmail(), "Abhishek", "Bachchan");
		assertTrue(persons.size() == 1);
		Person abhishek = persons.get(0);
		
		Optional<Person> optional1 = abhishek.getParents().stream().filter(p -> p.getFirstname().equals("Amitabh")).findAny();
		assertTrue(optional1.isPresent());
		Person amitabh = optional1.get();
		
		// illegal
		amitabh.addParent(abhishek);
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void publicTreeD3() {
		Map<String, Object> map = familyTreeService.publicTreeD3(1000);
		Collection nodes = (Collection) map.get("nodes");
		Collection links = (Collection) map.get("links");
		assertEquals("number of nodes", 4, nodes.size());
		assertEquals("number of links", 4, links.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void publicTree() throws Exception {
		String json = familyTreeService.publicTree(1000);
		// assert against some person
		List<Person> plist = publicRepository.persons(1);
		assertTrue(json.contains(plist.get(0).getFullname()));
	}

	@Test
	public void userEditableTree() throws Exception {
		String json = familyTreeService.userEditableTree(data.u_abhishek, 1000);		
		assertTrue(json.contains("Abhishek"));
		assertTrue(json.contains("Jaya"));
		assertTrue(json.contains("Amitabh"));
	}

	@Test
	public void userExtendedTree() throws Exception {
		String json = familyTreeService.userExtendedTree(data.u_abhishek, 1000);		
		assertTrue(json.contains("Abhishek"));
		assertTrue(json.contains("Jaya"));
		assertTrue(json.contains("Amitabh"));
	}

	@Test
	public void convertToKnodes() throws Exception {
		Collection<Person> u_amitabh_persons = familyRepository.userExtendedFamily(data.u_amitabh.getEmail(), 1000);
		Collection<KNode> knodes = familyTreeService.convertToKnodes(u_amitabh_persons);		
		assertEquals("u_amitabh_knodes", 2, knodes.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void toJson() throws Exception {
		List<Person> persons = new ArrayList<>();
		Person p = new Person("John", "Doe", new User());
		persons.add(p);
		String json = familyTreeService.toJson(persons);
		assertTrue(json.contains(p.getFullname()));
//		ObjectMapper mapper = new ObjectMapper();
//		Map<String,Object> map = mapper.readValue(json, Map.class);
//		assertTrue(containsValue(map, p.getFullname()));
	}
	
	// recursive
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean containsValue(Map<String,Object> map, String value) {
		boolean contains = false;
		Collection<Object> values = map.values();
		for (Object v : values) {
			if (v instanceof String) {
				contains = ((String)v).contains(value);
			}
			else if (v instanceof Map) {
				contains = containsValue((Map)v, value);
			}
			else if (v instanceof Collection) {
				contains = ((Collection)v).stream().anyMatch(x -> (x instanceof Map && containsValue((Map)x, value)));
			}
			
			if (contains) break;
		}
		return contains;
	}
}
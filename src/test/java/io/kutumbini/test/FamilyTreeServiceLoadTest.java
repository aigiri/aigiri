package io.kutumbini.test;

import static org.junit.Assert.assertEquals;

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
import io.kutumbini.domain.entity.Gender;
import io.kutumbini.repositories.FamilyRepository;
import io.kutumbini.services.FamilyTreeService;

// @Configuration class holding property values. In this case, for me, it worked using the @ContextConfiguration, 
// plus the @TestPropertySource("prop-file") and @EnableConfigurationProperties(Conf.class) – acaruci Nov 7 '18 at 13:01 

@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootTest(classes = AppConfig.class)
@DataNeo4jTest
@Transactional
public class FamilyTreeServiceLoadTest {

	@Autowired
	private FamilyTreeService familyTreeService;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FamilyRepository familyRepository;

	// this does the data setup
	@Autowired
	private JUnitTestData data;
	
	private static int LIMIT = 5000;
	
	@Before
	public void setUp() {
		data.setup();
	}
 
	@Test
	public void loadtest() {
		Long start = System.currentTimeMillis();
		User user = new User();
		user.setEmail("loadtester@k");
		userRepository.save(user);
		Family f = createFamily(user, null, 0);
		for (int index=1; index<LIMIT; index++) {
			f = createFamily(user, f, index);
		}
		System.out.println("Time in seconds on SAVE: " + (System.currentTimeMillis() - start)/1000);
		
		start = System.currentTimeMillis();
		Map<String, Object> map = familyTreeService.viewExtendedFamilyData(user);
		System.out.println("Time in seconds on FETCH: " + (System.currentTimeMillis() - start)/1000);
		Collection nodes = (Collection) map.get("nodes");
		Collection links = (Collection) map.get("links");
		assertEquals("number of nodes", 3*LIMIT, nodes.size());
		assertEquals("number of links", 3*LIMIT-1, links.size());
	}
	
	private Family createFamily(User user, Family g, int index) {
		String husbandFirstname = "h_" + index;
		String husbandLastname = "h_" + index;
		String wifeFirstname = "w_" + index;
		String wifeLastname = "w_" + index;
		Family f = familyTreeService.createFamily(user, husbandFirstname, husbandLastname, wifeFirstname, wifeLastname);
		if (g != null) familyTreeService.addChild(f.getParents().iterator().next().getId(), g.getId());
		return f;
	}
		
}
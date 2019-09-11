package io.kutumbini.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.kutumbini.auth.persistence.dao.UserRepository;
import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Family;
import io.kutumbini.services.FamilyTreeService;

@Configuration
@Profile("dev")
public class DevData {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FamilyTreeService familyTreeService;

//	@PostConstruct
	private void loadData() throws IOException {
		Path path = Paths.get("src/test/resources/test.cypher");
//	    String cypher = Files.readString(path);
		String cypher = new String(Files.readAllBytes(path));
	    
	    // Make sure this db instance is the same as the one loaded elsewhere, e.g., by PersonRepository
	    GraphDatabaseFactory graphDbFactory = new GraphDatabaseFactory();
	    GraphDatabaseService graphDb = graphDbFactory.newEmbeddedDatabase(new File("data/devgraph.db"));
	    graphDb.execute(cypher);
	}

//	@PostConstruct
	private void setup() {
		int LIMIT = 1000;
		Long start = System.currentTimeMillis();
		User user = new User();
		user.setEmail("loadtester@k");
		userRepository.save(user);
//		Family f = createFamily(user, null, 0);
//		for (int index=1; index<LIMIT; index++) {
//			f = createFamily(user, f, index);
//		}
//		System.out.println("Time in seconds on SAVE: " + (System.currentTimeMillis() - start)/1000);
//		
//		start = System.currentTimeMillis();
//		Map<String, Object> map = familyTreeService.viewExtendedFamilyData(user);
//		System.out.println("Time in seconds on FETCH: " + (System.currentTimeMillis() - start)/1000);
	}
	
}

package io.kutumbini.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.kutumbini.auth.persistence.dao.UserRepository;
import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.repositories.PersonRepository;

@Configuration
@Profile("dev")
public class DevData {

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private UserRepository userRepository;
	
	public User u_amitabh = new User();
	public User u_jaya = new User();
	public User u_abhishek = new User();
	public User u_gabbar = new User();
	public User u_aishwarya = new User();

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
		
		//**************************************************** set up users
		// no delegated nodes
		u_amitabh.setEmail("amitabh@bollywood");
		
		// DELEGATED.FULL from u_amitabh
		u_jaya.setEmail("jaya@bollywood");
		u_jaya.addDelagatedFull(u_amitabh);
		
		// DELEGATED.FULL from u_jaya 
		u_abhishek.setEmail("abhishek@bollywood");
		u_abhishek.addDelagatedFull(u_jaya);
		
		// DELEGATED.PERIPHERAL from u_abhishek 
		u_aishwarya.setEmail("aishwarya@bollywood");
		u_aishwarya.addDelagatedPeripheral(u_abhishek);
		
		u_gabbar.setEmail("gabbar@bollywood");
		
		userRepository.save(u_amitabh);
		userRepository.save(u_jaya);
		userRepository.save(u_abhishek);
		userRepository.save(u_aishwarya);
		userRepository.save(u_gabbar);
		
		//*************************************************** set up person nodes
		Person p_amitabh = new Person("Amitabh", "Bachchan", u_amitabh);		
		Person p_jaya = new Person("Jaya", "Bachchan", u_jaya);
		p_jaya.addSpouse(p_amitabh);
		Person p_abhishek = new Person("Abhishek", "Bachchan", u_abhishek);
		p_abhishek.addParent(p_jaya);
		p_abhishek.addParent(p_amitabh);

		// isolated node
		Person p_gabbar = new Person("Gabbar", "Singh", u_gabbar);

		personRepository.save(p_jaya);
		personRepository.save(p_amitabh);
		personRepository.save(p_abhishek);
		personRepository.save(p_gabbar);
		
	}
}

package io.kutumbini.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.kutumbini.auth.persistence.dao.UserRepository;
import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.repositories.FamilyRepository;

@Component
public class TestData {
	
	@Autowired
	private FamilyRepository personRepository;

	@Autowired
	private UserRepository userRepository;
	
	public User u_amitabh = null;
	public User u_jaya = null;
	public User u_abhishek = null;
	public User u_gabbar = null;
	public User u_aishwarya = null;

	public void setup() {
		
		u_amitabh = new User();
		u_jaya = new User();
		u_abhishek = new User();
		u_gabbar = new User();
		u_aishwarya = new User();

		//**************************************************** set up users
		// no delegated nodes
		u_amitabh.setEmail("amitabh@bollywood");
		
		// DELEGATED.FULL from u_amitabh
		u_jaya.setEmail("jaya@bollywood");
		u_jaya.addDelegatedInComingFull(u_amitabh);
		
		// DELEGATED.FULL from u_jaya 
		u_abhishek.setEmail("abhishek@bollywood");
		u_abhishek.addDelegatedInComingFull(u_jaya);
		
		// DELEGATED.PERIPHERAL from u_abhishek 
		u_aishwarya.setEmail("aishwarya@bollywood");
		u_aishwarya.addDelegatedInComingPeripheral(u_abhishek);
		
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
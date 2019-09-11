package io.kutumbini.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.kutumbini.auth.persistence.dao.UserRepository;
import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Family;
import io.kutumbini.domain.entity.Gender;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.repositories.FamilyRepository;

@Component
public class JUnitTestData {
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FamilyRepository familyRepository;

	public User u_amitabh = null;
	public User u_jaya = null;
	public User u_abhishek = null;
	public User u_aishwarya = null;
	public User u_homer = null;

	public void setup() {
		//**************************************************** set up users
		// no delegated nodes
		u_amitabh = new User();
		u_amitabh.setEmail("amitabh@bollywood");
		
		// DELEGATED.FULL from u_amitabh
		u_jaya = new User();
		u_jaya.setEmail("jaya@bollywood");
		u_jaya.addDelegatedInComingFull(u_amitabh);
		
		// DELEGATED.FULL from u_jaya 
		u_abhishek = new User();
		u_abhishek.setEmail("abhishek@bollywood");
		u_abhishek.addDelegatedInComingFull(u_jaya);
		
		// DELEGATED.PERIPHERAL from u_abhishek 
		u_aishwarya = new User();
		u_aishwarya.setEmail("aishwarya@bollywood");
		u_aishwarya.addDelegatedInComingPeripheral(u_abhishek);
		
		u_homer = new User();
		u_homer.setEmail("homer@fox");
		
		userRepository.save(u_amitabh);
		userRepository.save(u_jaya);
		userRepository.save(u_abhishek);
		userRepository.save(u_aishwarya);
		userRepository.save(u_homer);
		
		//*************************************************** set up person nodes
		Person p_amitabh = new Person("Amitabh", "Bachchan", Gender.Male, u_amitabh.getId());		
		Person p_jaya = new Person("Jaya", "Bachchan", Gender.Female, u_amitabh.getId());
		Person p_harivansh = new Person("Harivansh", "Bachchan", Gender.Male, u_amitabh.getId());
		Person p_teji = new Person("Teji", "Bachchan", Gender.Female, u_amitabh.getId());
		Person p_abhishek = new Person("Abhishek", "Bachchan", Gender.Male, u_amitabh.getId());
		Person p_aishwarya = new Person("Aishwarya", "Bachchan", Gender.Female, u_aishwarya.getId());
		Person p_aaradhya = new Person("Aaradhya", "Bachchan", Gender.Female, u_aishwarya.getId());
		Person p_krishnaraj = new Person("Krishnaraj", "Rai", Gender.Male, u_aishwarya.getId());
		Person p_vrinda = new Person("Vrinda", "Rai", Gender.Female, u_aishwarya.getId());

		Person p_homer = new Person("Homer", "Simpson", Gender.Male, u_homer.getId());
		Person p_marge = new Person("Marge", "Simpson", Gender.Female, u_homer.getId());
		Person p_bart = new Person("Bart", "Simpson", Gender.Male, u_homer.getId());
		Person p_maggie = new Person("Maggie", "Simpson", Gender.Female, u_homer.getId());
		Person p_lisa = new Person("Lisa", "Simpson", Gender.Female, u_homer.getId());


		Family f = new Family(u_amitabh.getId());
		f.addParent(p_teji);
		f.addParent(p_harivansh);
		f.addChild(p_amitabh);

		Family f1 = new Family(u_amitabh.getId());
		f1.addParent(p_jaya);
		f1.addParent(p_amitabh);
		f1.addChild(p_abhishek);

		Family f2 = new Family(u_abhishek.getId());
		f2.addParent(p_abhishek);
		f2.addParent(p_aishwarya);
		f2.addChild(p_aaradhya);

		Family f3 = new Family(u_aishwarya.getId());
		f3.addParent(p_krishnaraj);
		f3.addParent(p_vrinda);
		f3.addChild(p_aishwarya);

		Family simpsons = new Family(u_homer.getId());
		simpsons.addParent(p_homer);
		simpsons.addParent(p_marge);
		simpsons.addChild(p_bart);
		simpsons.addChild(p_maggie);
		simpsons.addChild(p_lisa);

		familyRepository.save(f);
		familyRepository.save(f1);
		familyRepository.save(f2);
		familyRepository.save(f3);
		familyRepository.save(simpsons);
	}

}
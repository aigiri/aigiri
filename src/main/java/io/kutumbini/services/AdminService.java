package io.kutumbini.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kutumbini.auth.persistence.dao.UserRepository;
import io.kutumbini.auth.persistence.model.Role;
import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.repositories.FamilyRepository;

@Service
public class AdminService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FamilyRepository personRepository;

	
	@Transactional
	public void addRole(User user, Role role) {
		user.addRole(role);
		userRepository.save(user);
	}

	// TODO ygiri remove this method
	@Transactional
	public void  deleteAllUsers() {
		userRepository.deleteAll(userRepository.findAll());
	}

	// TODO ygiri remove this method
	@Transactional
	public void  deleteAllPersons() {
		personRepository.deleteAll(personRepository.findAll());
	}

	@Transactional(readOnly = true)
	public Map<String, Object>  userTreeD3(int limit) {
		Iterable<User> users = userRepository.findAll();
		return toD3ForceMap(users);
	}

	private Map<String, Object> toD3ForceMap(Iterable<User> users) {
		Map<User, Integer> userIndex = new HashMap<>();
		List<Map<String, Object>> nodes = new ArrayList<>();
		AtomicInteger index = new AtomicInteger(0);
		users.forEach(user -> {userIndex.put(user, index.getAndIncrement());
								nodes.add(map("label", "user" + "[" + user.getRoles() + "]", "email", user.getEmail()));});

		List<Map<String, Object>> rels = new ArrayList<>();
		users.forEach(u -> u.getDelegatedInComingFull().forEach(r -> rels.add(map("source", userIndex.get(u), "target", userIndex.get(r)))));
				
		return map("nodes", nodes, "links", rels);
	}

	
	private Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
		Map<String, Object> result = new HashMap<String, Object>(2);
		result.put(key1, value1);
		result.put(key2, value2);
		return result;
	}	
		
}

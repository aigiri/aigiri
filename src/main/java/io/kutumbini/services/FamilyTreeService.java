package io.kutumbini.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kutumbini.auth.persistence.dao.UserRepository;
import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Family;
import io.kutumbini.domain.entity.Gender;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.repositories.FamilyRepository;
import io.kutumbini.validation.ValidationException;

@Service
public class FamilyTreeService {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FamilyRepository familyRepository;

	@Transactional(readOnly = true)
	public Map<String, Object> userEditableTreeD3(User user) {
		Set<Long> delegatorIds = getDelegatorIds(user);
		List<Family> families = familyRepository.findByUserIdIn(delegatorIds);
		return toD3ForceMap(families);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> userExtendedTreeD3(User user) {
		// this one does not populate relations
		List<Family> families = familyRepository.findConnectedFamilyIds(user.getId());
		List<Long> extendedIds = new ArrayList<Long>();
		families.forEach(f -> extendedIds.add(f.getId()));
		List<Family> populatedFamilies = familyRepository.findByIdIn(extendedIds);
		return toD3ForceMap(populatedFamilies);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> publicTreeD3() {
		Iterable<Family> families = familyRepository.findAll();
		return toD3ForceMap(families);
	}
	
	private Map<String, Object> toD3ForceMap(Iterable<Family> families) {
		List<Map<String, Object>> nodes = new ArrayList<>();
		List<Map<String, Object>> rels = new ArrayList<>();
		Map<Person, Integer> piMap = new HashMap<Person, Integer>();
		AtomicInteger index = new AtomicInteger(-1);
		families.forEach(f -> {
			// family node is the source
			nodes.add(map(new String[]{"name", "label"}, new Object[]{f.getName(), "family"}));
			int sourceIndex = index.incrementAndGet();
			
			// each person node is the target
			f.getParents().forEach(p -> {
				Integer pi = piMap.get(p);
				if (pi == null) {
					nodes.add(map(new String[]{"name", "label", "gender"}, new Object[]{p.getFirstname(), "person", p.getGender().name()}));
					pi = index.incrementAndGet();
					piMap.put(p, pi);
				}
				rels.add(map(new String[]{"label", "source", "target"}, new Object[]{"parent", sourceIndex, pi}));
				});

			f.getChildren().forEach(c -> {
				Integer pi = piMap.get(c);
				if (pi == null) {
					nodes.add(map(new String[]{"name", "label", "gender"}, new Object[]{c.getFirstname(), "person", c.getGender().name()}));
					pi = index.incrementAndGet();
					piMap.put(c, pi);
				}
				rels.add(map(new String[]{"label", "source", "target"}, new Object[]{"child", sourceIndex, pi}));
				});
			});
		
		return map(new String[]{"nodes", "links"}, new Object[]{nodes, rels});
	}

	// keys and values arrays should have the same length
	private Map<String, Object> map(String[] keys, Object[] values) {
		Map<String, Object> result = new HashMap<String, Object>(keys.length);
		for (int i=0; i<keys.length; i++) {
			result.put(keys[i], values[i]);
		}
		return result;
	}

	@Transactional
	public void createFamily(User user, String husbandFirstname, String husbandLastname, String wifeFirstname, String wifeLastname) {
		Person husband = new Person(husbandFirstname, husbandLastname, Gender.M);
		Person wife = new Person(wifeFirstname, wifeLastname, Gender.F);
		createFamily(husband, wife, user);
	}
		
	private void createFamily(Person husband, Person wife, User user) {
		// the order of saving below is important!
		Family family = new Family();
		family.setUserId(user.getId());
		family.addParent(wife);
		family.addParent(husband);
//		wife.addFamily(family);
//		husband.addFamily(family);
		familyRepository.save(family);
	}
	
	// TODO ygiri should be in a transaction
	public void addChild(User user, String firstname, String lastname, Gender gender, Long familyNodeId) {
		Family family = null;
		Optional<Family> optional = familyRepository.findById(familyNodeId);
		if (optional.isPresent()) {
			family = optional.get();
		} else {
			throw new ValidationException("There is no family with id " + familyNodeId);
		}

		Person child = new Person(firstname, lastname, gender);
		
		if (!family.getChildren().contains(child)) {
//			child.setBornIn(family);
			family.addChild(child);
			familyRepository.save(family);
		} else {
			throw new ValidationException("Person already exists in family with id " + familyNodeId);
		}
	}
	
	/**
	 * 
	 * @return IDs of the users that delegated to the passed user up to two degrees 
	 */
	public Set<Long> getDelegatorIds(User user) {
		Optional<User> ouser = userRepository.findById(user.getId());
		User u = ouser.get();
		Set<Long> delegateIds = new HashSet<Long>();
		// degree 0
		delegateIds.add(u.getId());
		// degree 1
		u.getDelegatedInComingFull().forEach(u1 -> {
			delegateIds.add(u1.getId());
			// degree 2
			u1.getDelegatedInComingFull().forEach(u2 -> delegateIds.add(u2.getId()));
		});
		return delegateIds;
	}

}

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
import io.kutumbini.repositories.PersonRepository;
import io.kutumbini.validation.ValidationException;

@Service
public class FamilyTreeService {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private FamilyRepository familyRepository;

	@Transactional
	public Map<String, Object> editFamilyData(User user) {
		Set<Long> delegatorIds = getDelegatorIds(user);
		List<Family> families = familyRepository.findByUserIdIn(delegatorIds);
		return toD3ForceEditMap(families);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> viewExtendedFamilyData(User user) {
		Set<Long> delegatorIds = getDelegatorIds(user);
//		List<Family> families = familyRepository.findByUserIdIn(delegatorIds);
		Iterable<Family> families = familyRepository.findAll();
		return toD3ForceViewMap(families);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> publicTreeD3() {
		Iterable<Family> families = familyRepository.findAll();
		return toD3ForceViewMap(families);
	}
	
	private Map<String, Object> toD3ForceEditMap(Iterable<Family> families) {
		List<Map<String, Object>> nodes = new ArrayList<>();
		List<Map<String, Object>> rels = new ArrayList<>();
		Map<Person, Integer> piMap = new HashMap<Person, Integer>();
		AtomicInteger index = new AtomicInteger(-1);
		families.forEach(f -> {			
			// each person node is the target
			f.getParents().forEach(p -> {
				Integer pi = piMap.get(p);
				if (pi == null) {
					nodes.add(map(new String[]{"firstname", "lastname", "gender"}, 
							new Object[]{p.getFirstname(), p.getLastname(), p.getGender().name()}));
					pi = index.incrementAndGet();
					piMap.put(p, pi);
				}
				
				final Integer fpi = pi;

				f.getChildren().forEach(c -> {
					Integer ci = piMap.get(c);
					if (ci == null) {
						nodes.add(map(new String[]{"firstname", "lastname", "gender"}, 
								new Object[]{c.getFirstname(), c.getLastname(), c.getGender().name()}));
						ci = index.incrementAndGet();
						piMap.put(c, ci);
					}
					rels.add(map(new String[]{"source", "target"}, new Object[]{ci, fpi}));
					});
			});
		});
		return map(new String[]{"nodes", "links"}, new Object[]{nodes, rels});
	}

	private Map<String, Object> toD3ForceViewMap(Iterable<Family> families) {
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
					nodes.add(map(new String[]{"name", "label", "gender"}, 
							new Object[]{p.getFirstname(), "person", p.getGender().name()}));
					pi = index.incrementAndGet();
					piMap.put(p, pi);
				}
				rels.add(map(new String[]{"familyid", "label", "source", "target"}, new Object[]{f.getId(), "parent", sourceIndex, pi}));
				});

			f.getChildren().forEach(c -> {
				Integer pi = piMap.get(c);
				if (pi == null) {
					nodes.add(map(new String[]{"name", "label", "gender"}, 
							new Object[]{c.getFirstname(), "person", c.getGender().name()}));
					pi = index.incrementAndGet();
					piMap.put(c, pi);
				}
				rels.add(map(new String[]{"familyid", "label", "source", "target"}, new Object[]{f.getId(), "child", sourceIndex, pi}));
				});
			});
		
		return map(new String[]{"nodes", "links"}, new Object[]{nodes, rels});
	}

	private Map<String, Object> toD3ForceView2Map(Iterable<Family> families) {
		List<Map<String, Object>> nodes = new ArrayList<>();
		List<Map<String, Object>> rels = new ArrayList<>();
		Map<Person, Integer> piMap = new HashMap<Person, Integer>();
		
		AtomicInteger index = new AtomicInteger(-1);
		// to determine singles iterate through all families and gather parents
		// the remaining ones would be singles
		families.forEach(f -> f.getParents().forEach(p -> {
										Integer pi = index.incrementAndGet();
										piMap.put(p, pi);
										nodes.add(map(new String[]{"name", "label", "gender", "single"}, 
												new Object[]{p.getFirstname(), "person", p.getGender().name(), "false"}));
									}));
		
		families.forEach(f -> {
			// family node is the source
			nodes.add(map(new String[]{"name", "label"}, new Object[]{f.getName(), "family"}));
			int sourceIndex = index.incrementAndGet();
			
			// each person node is the target
			f.getParents().forEach(p -> {
				Integer pi = piMap.get(p);
				rels.add(map(new String[]{"familyid", "label", "source", "target"}, new Object[]{f.getId(), "parent", sourceIndex, pi}));
				});

			f.getChildren().forEach(c -> {
				Integer pi = piMap.get(c);
				if (pi == null) {
					nodes.add(map(new String[]{"name", "label", "gender", "single"}, 
							new Object[]{c.getFirstname(), "person", c.getGender().name(), "true"}));
					pi = index.incrementAndGet();
					piMap.put(c, pi);
				}
				rels.add(map(new String[]{"familyid", "label", "source", "target"}, new Object[]{f.getId(), "child", sourceIndex, pi}));
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
	public Family createFamily(User user, String husbandFirstname, String husbandLastname, String wifeFirstname, String wifeLastname) {
//		check if it exists already
		List<Family> flist = familyRepository.find(user.getId(), husbandFirstname, husbandLastname, wifeFirstname, wifeLastname);
		if (!flist.isEmpty()) {
			throw new ValidationException("Family already exits: " + user.getId() 
					+ "::" + husbandFirstname + " " + husbandLastname
					+ "::" + wifeFirstname + " " + wifeLastname );
		}

		Person husband = new Person(husbandFirstname, husbandLastname, Gender.M);
		Person wife = new Person(wifeFirstname, wifeLastname, Gender.F);
		return createFamily(husband, wife, user);
	}
		
	private Family createFamily(Person husband, Person wife, User user) {
		Family family = new Family();
		family.setUserId(user.getId());
		family.addParent(wife);
		family.addParent(husband);
		return familyRepository.save(family);
	}
	
	@Transactional
	public void addChild(Long childNodeId, Long familyNodeId) {
		Family family = null;
		Optional<Family> ofamily = familyRepository.findById(familyNodeId);
		if (ofamily.isPresent()) {
			family = ofamily.get();
		} else {
			throw new ValidationException("There is no family with id " + familyNodeId);
		}

		Person child = null;
		Optional<Person> ochild = personRepository.findById(childNodeId);
		if (ochild.isPresent()) {
			child = ochild.get();
		} else {
			throw new ValidationException("There is no person with id " + childNodeId);
		}

		if (!family.getChildren().contains(child)) {
			family.addChild(child);
			familyRepository.save(family);
		} else {
			throw new ValidationException("Person already exists in family with id " + familyNodeId);
		}
	}
	
	@Transactional
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

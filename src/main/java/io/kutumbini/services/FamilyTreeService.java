package io.kutumbini.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
	public List<Person> getEditablePersons(User user) {
		Set<Long> delegatorIds = getDelegatorIds(user);
		return personRepository.findByUserIdIn(delegatorIds);
	}

	@Transactional
	public List<Family> getEditableFamlies(User user) {
		Set<Long> delegatorIds = getDelegatorIds(user);
		return familyRepository.findByUserIdIn(delegatorIds);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> viewExtendedFamilyData(User user) {
		Set<Long> delegatorIds = getDelegatorIds(user);
		List<Family> families = familyRepository.findByUserIdIn(delegatorIds);
		List<Person> persons = personRepository.findByUserIdIn(delegatorIds);
		return toD3ForceViewMap(families, persons);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> publicTreeD3() {
		List<Family> families = familyRepository.findAll();
		List<Person> persons = personRepository.findAll();
		return toD3ForceViewMap(families, persons);
	}

	private Map<String, Object> toD3ForceViewMap(List<Family> families, List<Person> persons) {
		List<Person> orphans = persons.stream().filter(p -> !families.stream().anyMatch(f -> f.isMember(p)))
				.collect(Collectors.toList());
		List<Map<String, Object>> nodes = new ArrayList<>();
		List<Map<String, Object>> rels = new ArrayList<>();
		Map<Person, Integer> piMap = new HashMap<Person, Integer>();
		AtomicInteger index = new AtomicInteger(-1);
		families.forEach(f -> {
			// family node is the source
			nodes.add(map(new String[] { "name", "label" }, new Object[] { f.getName(), "family" }));
			int sourceIndex = index.incrementAndGet();

			// each person node is the target
			f.getParents().forEach(p -> {
				Integer pi = piMap.get(p);
				if (pi == null) {
					nodes.add(map(new String[] { "name", "label", "gender" },
							new Object[] { p.getFirstname(), "person", p.getGender().name() }));
					pi = index.incrementAndGet();
					piMap.put(p, pi);
				}
				rels.add(map(new String[] { "familyid", "label", "source", "target" },
						new Object[] { f.getId(), "parent", sourceIndex, pi }));
			});

			f.getChildren().forEach(c -> {
				Integer pi = piMap.get(c);
				if (pi == null) {
					nodes.add(map(new String[] { "name", "label", "gender" },
							new Object[] { c.getFirstname(), "person", c.getGender().name() }));
					pi = index.incrementAndGet();
					piMap.put(c, pi);
				}
				rels.add(map(new String[] { "familyid", "label", "source", "target" },
						new Object[] { f.getId(), "child", sourceIndex, pi }));
			});
		});

		orphans.forEach(o -> {
			nodes.add(map(new String[] { "name", "label", "gender" },
					new Object[] { o.getFirstname(), "person", o.getGender().name() }));
		});

		return map(new String[] { "nodes", "links" }, new Object[] { nodes, rels });
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
			nodes.add(map(new String[] { "name", "label", "gender", "single" },
					new Object[] { p.getFirstname(), "person", p.getGender().name(), "false" }));
		}));

		families.forEach(f -> {
			// family node is the source
			nodes.add(map(new String[] { "name", "label" }, new Object[] { f.getName(), "family" }));
			int sourceIndex = index.incrementAndGet();

			// each person node is the target
			f.getParents().forEach(p -> {
				Integer pi = piMap.get(p);
				rels.add(map(new String[] { "familyid", "label", "source", "target" },
						new Object[] { f.getId(), "parent", sourceIndex, pi }));
			});

			f.getChildren().forEach(c -> {
				Integer pi = piMap.get(c);
				if (pi == null) {
					nodes.add(map(new String[] { "name", "label", "gender", "single" },
							new Object[] { c.getFirstname(), "person", c.getGender().name(), "true" }));
					pi = index.incrementAndGet();
					piMap.put(c, pi);
				}
				rels.add(map(new String[] { "familyid", "label", "source", "target" },
						new Object[] { f.getId(), "child", sourceIndex, pi }));
			});
		});

		return map(new String[] { "nodes", "links" }, new Object[] { nodes, rels });
	}

	// keys and values arrays should have the same length
	private Map<String, Object> map(String[] keys, Object[] values) {
		Map<String, Object> result = new HashMap<String, Object>(keys.length);
		for (int i = 0; i < keys.length; i++) {
			result.put(keys[i], values[i]);
		}
		return result;
	}

	@Transactional
	public Family createFamily(User user, String husbandFirstname, String husbandLastname, String wifeFirstname,
			String wifeLastname) {
//		check if it exists already
		List<Family> flist = familyRepository.find(user.getId(), husbandFirstname, husbandLastname, wifeFirstname,
				wifeLastname);
		if (!flist.isEmpty()) {
			throw new ValidationException("Family already exits: " + user.getId() + "::" + husbandFirstname + " "
					+ husbandLastname + "::" + wifeFirstname + " " + wifeLastname);
		}

		Person husband = new Person(husbandFirstname, husbandLastname, Gender.Male, user.getId());
		Person wife = new Person(wifeFirstname, wifeLastname, Gender.Female, user.getId());
		return createFamily(husband, wife, user);
	}

	private Family createFamily(Person husband, Person wife, User user) {
		Family family = new Family(user.getId());
		family.addParent(wife);
		family.addParent(husband);
		return familyRepository.save(family);
	}

	@Transactional
	public Person createPerson(User user) {
		Person p = new Person(user.getId());
		personRepository.save(p);
		return p;
	}

	@Transactional
	public void deleteFamily(long fid, User user) {
		// make sure the family belongs to the user
		List<Long> userIds = new ArrayList<Long>();
		userIds.add(user.getId());
		List<Family> families = familyRepository.findByUserIdIn(userIds);
		Optional<Family> family = families.stream().filter(f -> f.getId() == fid).findAny();
		if (family.isPresent()) {
			familyRepository.delete(family.get());
		} else {
			throw new SecurityException("Family with id " + fid + " does not belong to user " + user);
		}
	}

	@Transactional
	public void deletePerson(long pid, User user) {
		// make sure the person belongs to the user
		List<Long> userIds = new ArrayList<Long>();
		userIds.add(user.getId());
		List<Person> persons = personRepository.findByUserIdIn(userIds);
		Person person = persons.stream().filter(p -> p.getId() == pid).findAny().orElse(null);
		if (person != null) {
			// delete the person from families
			List<Family> families = familyRepository.findByUserIdIn(userIds);
			families.forEach(f -> {
				f.getParents().remove(person);
				if (f.isValid()) {
					familyRepository.save(f);
				} else {
					// delete invalid family
					familyRepository.delete(f);
				}
			});
			personRepository.delete(person);
		} else {
			throw new SecurityException("Person with id " + pid + " does not belong to user " + user);
		}
	}

	private void cascadeDeletePerson(Person person, User user) {
		List<Long> userIds = new ArrayList<Long>();
		userIds.add(user.getId());
		List<Family> families = familyRepository.findByUserIdIn(userIds);
		// when person is parent
		families.forEach(f -> {
			f.getParents().remove(person);
			if (f.isValid()) {
				familyRepository.save(f);
			} else {
				// delete invalid family
				familyRepository.delete(f);
			}
		});

		// when person is child
		families.forEach(f -> {
			f.getChildren().remove(person);
			if (f.isValid()) {
				familyRepository.save(f);
			} else {
				// delete invalid family
				familyRepository.delete(f);
			}
		});
	}

	@Transactional
	public void deleteFamilyTree(User user) {
		List<Long> userIds = new ArrayList<Long>();
		userIds.add(user.getId());
		familyRepository.deleteByUserIdIn(userIds);
		personRepository.deleteByUserIdIn(userIds);
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

	@Transactional
	public void savePersons(List<Person> persons) {
		persons.forEach(p -> personRepository.save(p));
	}

	@Transactional
	public Family saveFamily(Family family) {
		return familyRepository.save(family);
	}

	@Transactional
	public void saveFamilies(List<Family> families) {
		families.forEach(f -> familyRepository.save(f));
	}

}

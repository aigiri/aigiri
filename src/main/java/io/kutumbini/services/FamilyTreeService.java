package io.kutumbini.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.domain.relationship.RELATION;
import io.kutumbini.repositories.FamilyRepository;
import io.kutumbini.repositories.PublicRepository;
import io.kutumbini.validation.ValidationException;
import io.kutumbini.web.data.KNode;

@Service
public class FamilyTreeService {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private PublicRepository publicRepository;

	@Autowired
	private FamilyRepository personRepository;

	@Transactional(readOnly = true)
	public Map<String, Object> publicTreeD3(int limit) {
		List<Person> persons = publicRepository.persons(limit);
		return toD3ForceMap(persons);
	}

	@Transactional(readOnly = true)
	public String userEditableTree(User user, int limit) {
		List<Person> persons = personRepository.userEditableFamily(user.getEmail());
		return toJson(persons);
	}

	@Transactional(readOnly = true)
	public String userExtendedTree(User user, int limit) {
		List<Person> persons = personRepository.userExtendedFamily(user.getEmail(), limit);
//		return toJson(persons);
		return knodesToJson(convertToKNodes(persons));
	}

	@Transactional(readOnly = true)
	public String publicTree(int limit) {
		Collection<Person> persons = publicRepository.persons(limit);
		return toJson(persons);
	}

	private Collection<Person> getRootNodes(Collection<Person> persons) {
		Set<Person> roots = new HashSet<>();
		persons.forEach(p -> roots.addAll(p.getRootAncestors()));
		return roots;
	}

	private Collection<KNode> getRootKNodes(Collection<KNode> knodes) {
		Set<Person> rootPersons = new HashSet<>();
		knodes.forEach(knode -> {
			rootPersons.addAll(knode.x.getRootAncestors());
			if (knode.y != null)
				rootPersons.addAll(knode.y.getRootAncestors());
		});
		// for a knode to be a root, both knode.x and knode.y have to be roots
		return knodes.stream().filter(knode -> rootPersons.contains(knode.x) && rootPersons.contains(knode.y))
				.collect(Collectors.toList());
	}

	private Map<String, Object> toD3ForceMap(List<Person> persons) {
		List<Map<String, Object>> nodes = new ArrayList<>();
		persons.forEach(p -> nodes.add(map(new String[]{"name", "label"}, new Object[]{p.getFullname(), "person"})));

		persons.forEach(p -> {
		});
		List<Map<String, Object>> rels = new ArrayList<>();
		persons.forEach(p -> p.getParents()
				.forEach(r -> rels.add(map(new String[]{"label", "source", "target"}, new Object[]{"parent", persons.indexOf(p), persons.indexOf(r)}))));
		persons.forEach(p -> p.getSpouses()
				.forEach(r -> rels.add(map(new String[]{"label", "source", "target"}, new Object[]{"spouse", persons.indexOf(p), persons.indexOf(r)}))));
		
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

	private void knodesToJson(Collection<KNode> knodes, Collection<KNode> rootKNodes, StringBuilder sb) {
		sb.append("[");
		boolean first = true;
		for (KNode knode : rootKNodes) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append("{");
			sb.append("\"id\": ").append("\"" + knode.getIdString() + "\"");
			sb.append(", \"name\": ").append("\"" + knode.getName() + "\"");
			Collection<KNode> children = getChildren(knode, knodes);
			if (!children.isEmpty()) {
				sb.append(", \"children\": ");
				knodesToJson(knodes, children, sb);
			}
			sb.append("}");
		}
		sb.append("]");
	}

	private void toJson(Collection<Person> persons, Collection<Person> rootNodes, StringBuilder sb) {
		sb.append("[");
		boolean first = true;
		for (Person p : rootNodes) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append("{");
			sb.append("\"id\": ").append("\"" + p.getId() + "\"");
			sb.append(", \"name\": ").append("\"" + p.getFullname() + "\"");
			List<Person> children = getChildren(p, persons);
			if (!children.isEmpty()) {
				sb.append(", \"children\": ");
				toJson(persons, children, sb);
			}
			sb.append("}");
		}
		sb.append("]");
	}

	private List<Person> getChildren(Person p, Collection<Person> persons) {
		List<Person> children = new ArrayList<>();
		persons.forEach(r -> {
			if (r.getParents().contains(p))
				children.add(r);
		});
		return children;

	}

	private Set<Person> getPersons(Collection<KNode> knodes) {
		Set<Person> persons = new HashSet<Person>();
		knodes.forEach(knode -> {
			persons.add(knode.x);
			if (knode.y != null)
				persons.add(knode.y);
		});
		return persons;
	}

	private Collection<KNode> getChildren(KNode knode, Collection<KNode> knodes) {
		Set<Person> persons = getPersons(knodes);
		List<Person> xchildren = getChildren(knode.x, persons);
		Set<KNode> knodeChildren = convertToKNodes(xchildren);
		if (knode.y != null) {
			List<Person> ychildren = getChildren(knode.y, persons);
			knodeChildren.addAll(convertToKNodes(ychildren));
		}
		return knodeChildren;
	}

	/**
	 * JSON sample format: "[" + "{" + "\"name\": \"Top Level\"," + "\"children\":
	 * [" + "{" + "\"name\": \"Level 2: A\"," + "\"children\": [" + "{" + "\"name\":
	 * \"Son of A\"," + "}," + "{" + "\"name\": \"Daughter of A\"," + "}" + "]" +
	 * "}," + "{" + "\"name\": \"Level 2: B\"," + "}" + "]" + "}" + "]";
	 */
	public String toJson(Collection<Person> persons) {
		StringBuilder sb = new StringBuilder();
		toJson(persons, getRootNodes(persons), sb);

		// TODO ygiri make this invisible in UI
		// add dummy top node ( UI limitation - requires a single top node )
		String treeData = "[{ \"id\":\"-1\", \"name\": \"Dummy Top Node\", \"children\": " + sb.toString() + "}]";
		return treeData;
	}

	public String knodesToJson(Collection<KNode> knodes) {
		StringBuilder sb = new StringBuilder();
		knodesToJson(knodes, getRootKNodes(knodes), sb);

		// TODO ygiri make this invisible in UI
		// add dummy top node ( UI limitation - requires a single top node )
		String treeData = "[{ \"id\":\"-1\", \"name\": \"Dummy Top Node\", \"children\": " + sb.toString() + "}]";
		return treeData;
	}

	// TODO ygiri should be in a transaction
	public void addPerson(User user, String firstname, String lastname, Long nodeId, String relation, Long toNodeId) {

		Person person = null;
		if (nodeId != null) {
			Optional<Person> optional = personRepository.findById(nodeId);
			if (optional.isPresent()) {
				person = optional.get();
			} else {
				throw new ValidationException("There is no person with id " + nodeId);
			}
		} else {
			person = new Person(firstname, lastname, user);
			person.setUser(user);
		}

		// add relation
		if (toNodeId != null) {
			Optional<Person> related = personRepository.findById(toNodeId);
			if (related.isPresent()) {
				if (relation.equals(RELATION.PARENT)) {
					person.addParent(related.get());
				} else if (relation.equals(RELATION.SPOUSE)) {
					person.addSpouse(related.get());
				} else {
					throw new ValidationException("RELATION not recognized: " + relation);
				}
			} else {
				throw new ValidationException("There is no person with id " + toNodeId);
			}
		}
		personRepository.save(person);
	}

	public Map<String, Object> userExtendedTreeD3(User user, int limit) {
		List<Person> persons = personRepository.userExtendedFamily(user.getEmail(), limit);
		return toD3ForceMap(persons);
	}

	public Set<KNode> convertToKNodes(Collection<Person> persons) {
		Map<Person, KNode> knodesMap = new HashMap<Person, KNode>();
		for (Person p : persons) {
			if (p.getSpouses().isEmpty()) {
				// single
				knodesMap.put(p, new KNode(p, null));
			} else {
				for (Person s : p.getSpouses()) {
					if (knodesMap.containsKey(s)) {
						// nothing to do, p would be in knode that s belongs to
					} else {
						KNode knode = new KNode(p, s);
						knodesMap.put(p, knode);
						knodesMap.put(s, knode);
					}
				}
			}
		}
		// add to a Set so duplicate values are removed
		Set<KNode> knodes = new HashSet<KNode>();
		knodes.addAll(knodesMap.values());
		return knodes;
	}
}

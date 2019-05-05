package io.kutumbini.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
	public Map<String, Object>  publicTreeD3(int limit) {
		List<Person> persons = publicRepository.persons(limit);
		return toD3ForceMap(persons);
	}

	@Transactional(readOnly = true)
	public String  userEditableTree(User user, int limit) {
		List<Person> persons = personRepository.userEditableFamily(user.getEmail());
		return toJson(persons);
	}
	
	@Transactional(readOnly = true)
	public String  userExtendedTree(User user, int limit) {
		List<Person> persons = personRepository.userExtendedFamily(user.getEmail(), limit);
		return toJson(persons);
	}
	
	@Transactional(readOnly = true)
	public String  publicTree(int limit) {
		Collection<Person> persons = publicRepository.persons(limit);
		return toJson(persons);
	}
	
	private Collection<Person> getRootNodes(Collection<Person> persons) {
		Set<Person> roots = new HashSet<>();
		persons.forEach(p -> roots.addAll(p.getRootAncestors()));
		return roots;
	}

	private Map<String, Object> toD3ForceMap(List<Person> persons) {
		List<Map<String, Object>> nodes = new ArrayList<>();
		persons.forEach(p -> nodes.add(map("name", p.getFullname(), "label", "person")));

		persons.forEach(p -> {});
		List<Map<String, Object>> rels = new ArrayList<>();
		persons.forEach(p -> p.getRelations().forEach(r -> rels.add(map("source", persons.indexOf(p), "target", persons.indexOf(r)))));
				
		return map("nodes", nodes, "links", rels);
	}

	
	private Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
		Map<String, Object> result = new HashMap<String, Object>(2);
		result.put(key1, value1);
		result.put(key2, value2);
		return result;
	}

	private void toJson(Collection<Person> persons, Collection<Person> rootNodes, StringBuilder sb) {
		sb.append("[");
		boolean first = true;
		for (Person p : rootNodes) {
			if (first) {
				first = false;
			}
			else {
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
		persons.forEach(r -> {if (r.getParents().contains(p)) children.add(r);});
		return children;
		
	}
	
	/**
	 * 	JSON sample format: 
	"["
    + "{"
 	    + "\"name\": \"Top Level\","
 	    + "\"children\": ["
 	      + "{"
 	        + "\"name\": \"Level 2: A\","
 	        + "\"children\": ["
 	          + "{"
 	            + "\"name\": \"Son of A\","
 	          + "},"
 	          + "{"
 	            + "\"name\": \"Daughter of A\","
 	          + "}"
 	        + "]"
 	      + "},"
 	      + "{"
 	        + "\"name\": \"Level 2: B\","
 	      + "}"
 	    + "]"
 	  + "}"
 	+ "]";
	 */
	public String toJson(Collection<Person> persons) {
		StringBuilder sb = new StringBuilder();
		toJson(persons, getRootNodes(persons), sb);
		
		 // TODO ygiri make this invisible in UI
		// add dummy top node ( UI limitation - requires a single top node )
		String treeData = "[{ \"id\":\"-1\", \"name\": \"Dummy Top Node\", \"children\": "
							+ sb.toString() + "}]";
		return treeData;
	}

	// TODO ygiri should be in a transaction
	public void addPerson(User user, String firstname, String lastname, Long nodeId, String relation, Long toNodeId) {
		
		Person person = null;
		if (nodeId != null) {
			Optional<Person> optional = personRepository.findById(nodeId);
			if (optional.isPresent()) {
				person = optional.get();
			}
			else {
				throw new ValidationException("There is person with id " + nodeId);
			}
		}
		else {
			person = new Person(firstname, lastname, user);
		}
		
		// add to user
		person.setUser(user);
		
		// add relation
		Optional<Person> related = personRepository.findById(toNodeId);
		if (related.isPresent()) {
			if (relation.equals(RELATION.PARENT)) {
				person.addParent(related.get());
			}
			else if (relation.equals(RELATION.SPOUSE)) {
				person.addSpouse(related.get());
			}
			else {
				LOGGER.error("RELATION not recognized: " + relation);
			}
		}
		personRepository.save(person);
	}

	public Map<String, Object> userExtendedTreeD3(User user, int limit) {
		List<Person> persons = personRepository.userExtendedFamily(user.getEmail(), limit);
		return toD3ForceMap(persons);
	}
	
	public Collection<KNode> convertToKnodes(Collection<Person> persons) {
		Map<Person, KNode> knodesMap = new HashMap<Person, KNode>();
		for (Person p : persons) {
			if (p.getSpouses().isEmpty()) {
				//single
				knodesMap.put(p, new KNode(p, null));
			}
			else {
				for (Person s : p.getSpouses()) {
					if (knodesMap.containsKey(s)) {
						// nothing to do, p would be in knode that s belongs to
					}
					else {
						KNode knode = new KNode(p, s);
						knodesMap.put(p, knode);
						knodesMap.put(s, knode);
					}
				}
			}
		}
		
		return knodesMap.values();
	}
}

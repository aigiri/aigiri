package io.kutumbini.web.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Family;
import io.kutumbini.domain.entity.Gender;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.validation.ValidationException;

/**
 * Processes the data submitted on edits to the family tree
 *
 */
public class EditDataProcessor {

	public static void updateSessionData(List<Map<String, Object>> submitted, HttpSession session, User user,
			Set<Family> addFamilies, Set<Family> removeFamilies) {
		submitted.forEach(map -> updateSessionData(map, session, user, addFamilies, removeFamilies));
	}

	private static void updateSessionData(Map<String, Object> map, HttpSession session, User user,
			Set<Family> addFamilies, Set<Family> removeFamilies) {
		// the person in the map which represents one row in EDIT TABLE in
		// editFamily.html
		Long pid = Long.valueOf((String) map.get(Constants.ID));
		Person person = findPerson(pid, session);

		// update person attributes
		updatePerson(map, person);

		// update spouses
		updateSpouses(map, person, session, user, addFamilies, removeFamilies);

		// update parents
		updateParents(map, person, session, user, addFamilies, removeFamilies);
	}

	private static void updatePerson(Map<String, Object> map, Person person) {
		person.setFirstname((String) map.get(Constants.FIRST_NAME));
		person.setLastname((String) map.get(Constants.LAST_NAME));
		String genderString = (String) map.get(Constants.GENDER);
		Gender gender = null;
		if (genderString != null && genderString.toUpperCase().startsWith("M")) {
			gender = Gender.Male;
		} else if (genderString != null && genderString.toUpperCase().startsWith("F")) {
			gender = Gender.Female;
		} else if (genderString != null && genderString.toUpperCase().startsWith("O")) {
			gender = Gender.Other;
		}

		person.setGender(gender);
	}

	private static void updateSpouses(Map<String, Object> map, Person person, HttpSession session, User user,
			Set<Family> addFamilies, Set<Family> removeFamilies) {
		Set<Long> spouseIds = findSpouseIds(person, session);
		String spousesString = (String) map.get(Constants.SPOUSES);
		List<String> submittedSpouseStringIds = Arrays.asList(spousesString.split("\\D+"));
		for (String submitted : submittedSpouseStringIds) {
			if (StringUtils.isBlank(submitted)) continue;
			Long submittedSpouseId = Long.valueOf(submitted.trim());
			if (!spouseIds.contains(submittedSpouseId)) {
				// create family with this spouse pair
				Person spouse = findPerson(submittedSpouseId, session);
				Family family = new Family(user.getId());
				family.addParent(person);
				family.addParent(spouse);
				addFamilies.add(family);
			}
		}

		for (Long id : spouseIds) {
			if (!submittedSpouseStringIds.contains(id.toString())) {
				Family family = findFamily(person.getId(), id, session);
				Person spouse = findPerson(id, session);
				family.getParents().remove(spouse);
			}
		}
	}

	private static void updateParents(Map<String, Object> map, Person person, HttpSession session, User user,
			Set<Family> addFamilies, Set<Family> removeFamilies) {
		// if there is a family that has all three (child-parent-parent) then there are no changes to update
		// else if there is a family that has two out of three (child-parent-parent) then find it and add the third person to it
		// else if at least one parent is present then create a new family 
		
		Long parentId1 = null;
		Long parentId2 = null;
		String submittedString = (String) map.get(Constants.PARENTS);
		List<String> submitted = Arrays.asList(submittedString.split("\\D+"));

		// limited to two parents
		if (submitted.size() > 0 && StringUtils.isNotBlank(submitted.get(0))) parentId1 = Long.valueOf(submitted.get(0));
		if (submitted.size() > 1 && StringUtils.isNotBlank(submitted.get(1))) parentId2 = Long.valueOf(submitted.get(1));
			
		List<Family> families = (List<Family>) session.getAttribute(Constants.FAMILIES);
		
		// filter for person as child
		List<Family> childFamilies = families.stream()
				.filter(f -> f.getChildren().contains(person)).collect(Collectors.toList());
		// a person must be in at most one family as a child
		if (childFamilies.size() > 1) {
			throw new IllegalStateException();
		}

		if (parentId1 == null && parentId2 == null) {
			if (!childFamilies.isEmpty()) {
				// remove the family
				removeFamilies.add(childFamilies.get(0));
			}
			return;
		}
		
		// create a family with the passed person as child if it doesn't exist
		Family family = null;
		if (childFamilies.isEmpty()) {
			family = new Family(user.getId());
			family.addChild(person);
			addFamilies.add(family);
		}
		else {
			family = childFamilies.get(0);
		}
		
		// single parent case
		if ((parentId1 != null && parentId2 == null) || (parentId1 == null && parentId2 != null)) {
			Long singleParentId = parentId1 != null ? parentId1 : parentId2;
			// clear any existing parents
			family.getParents().clear();
			// add the submitted parent
			family.addParent(findPerson(singleParentId, session));
		}
		
		// two parents case
		if (parentId1 != null && parentId2 != null) {
			// clear any existing parents
			family.getParents().clear();
			// add the submitted parents
			family.addParent(findPerson(parentId1, session));
			family.addParent(findPerson(parentId2, session));
		}

	}

	private static Person findPerson(long pid, HttpSession session) {
		List<Person> persons = (List<Person>) session.getAttribute(Constants.PERSONS);
		Optional<Person> operson = persons.stream().filter(p -> p.getId() == pid).findAny();
		if (!operson.isPresent()) {
			throw new ValidationException("Person with id " + pid + " is not present in the session");
		}

		return operson.get();
	}

	// returns the Family that has the passed parents
	private static Family findFamily(Long parentid, Long parentid2, HttpSession session) {
		List<Family> families = (List<Family>) session.getAttribute(Constants.FAMILIES);
		Optional<Family> ofamily = families.stream()
				.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid))
				.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid2)).findAny();

		return ofamily.orElse(null);
	}

	private static List<Family> findFamilies(Long parentid, HttpSession session) {
		List<Family> families = (List<Family>) session.getAttribute(Constants.FAMILIES);
		List<Family> filtered = families.stream()
				.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid)).collect(Collectors.toList());

		return filtered;
	}

	private static Set<Long> findSpouseIds(Person person, HttpSession session) {
		List<Family> families = (List<Family>) session.getAttribute(Constants.FAMILIES);
		Set<Long> spouseIds = new HashSet();

		families.stream().filter(f -> f.getParents().contains(person)).forEach(f -> {
			f.getParents().forEach(spouse -> {
				if (!spouse.equals(person))
					spouseIds.add(spouse.getId());
			});
		});

		return spouseIds;
	}

}
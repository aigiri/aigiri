package io.kutumbini.web.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Family;
import io.kutumbini.domain.entity.Gender;
import io.kutumbini.domain.entity.Person;

/**
 * Processes the data submitted on edits to the family tree
 *
 */
public class EditDataProcessor {

	private FamilyCouncelor familyCouncelor;

	public EditDataProcessor(FamilyCouncelor familyCouncelor) {
		this.familyCouncelor = familyCouncelor;
	}

	public void updateSessionData(List<Map<String, Object>> submitted, User user) {
		submitted.forEach(map -> updateSessionData(map, user));
	}

	private void updateSessionData(Map<String, Object> map, User user) {
		// the person in the map which represents one row in EDIT TABLE in
		// editFamily.html
		Long pid = Long.valueOf((String) map.get(Constants.ID));
		Person person = familyCouncelor.findPerson(pid);

		// update person attributes
		updatePerson(map, person);

		// update spouses
		updateSpouses(map, person, user);

		// update parents
		updateParents(map, person, user);
	}

	private void updatePerson(Map<String, Object> map, Person person) {
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

	private void updateSpouses(Map<String, Object> map, Person person, User user) {
		Set<Long> spouseIds = familyCouncelor.findSpouseIds(person);
		String spousesString = (String) map.get(Constants.SPOUSES);
		List<String> submittedSpouseStringIds = Arrays.asList(spousesString.split("\\D+"));
		for (String submitted : submittedSpouseStringIds) {
			if (StringUtils.isBlank(submitted))
				continue;
			Long submittedSpouseId = Long.valueOf(submitted.trim());
			if (!spouseIds.contains(submittedSpouseId)) {
				// create family with this spouse pair
				Person spouse = familyCouncelor.findPerson(submittedSpouseId);
				Family family = new Family(user.getId());
				family.addParent(person);
				family.addParent(spouse);
				familyCouncelor.addFamily(family);
			}
		}

		for (Long id : spouseIds) {
			if (!submittedSpouseStringIds.contains(id.toString())) {
				Family family = familyCouncelor.findFamily(person.getId(), id);
				Person spouse = familyCouncelor.findPerson(id);
				family.getParents().remove(spouse);
			}
		}
	}

	private void updateParents(Map<String, Object> map, Person person, User user) {
		// if there is a family that has all three (child-parent-parent) then there are
		// no changes to update
		// else if there is a family that has two out of three (child-parent-parent)
		// then find it and add the third person to it
		// else if at least one parent is present then create a new family

		Long parentId1 = null;
		Long parentId2 = null;
		String submittedString = (String) map.get(Constants.PARENTS);
		List<String> submitted = Arrays.asList(submittedString.split("\\D+"));

		// limited to two parents
		if (submitted.size() > 0 && StringUtils.isNotBlank(submitted.get(0)))
			parentId1 = Long.valueOf(submitted.get(0));
		if (submitted.size() > 1 && StringUtils.isNotBlank(submitted.get(1)))
			parentId2 = Long.valueOf(submitted.get(1));

		// filter for person as child
		List<Family> childFamilies = familyCouncelor.findChildFamilies(person);
		// a person must be in at most one family as a child
		if (childFamilies.size() > 1) {
			throw new IllegalStateException();
		}

		if (parentId1 == null && parentId2 == null) {
			if (!childFamilies.isEmpty()) {
				// remove the family
				familyCouncelor.removeFamily(childFamilies.get(0));
			}
			return;
		}

		// single parent case
		if ((parentId1 != null && parentId2 == null) || (parentId1 == null && parentId2 != null)) {
			Long singleParentId = parentId1 != null ? parentId1 : parentId2;
			// create a family with the passed person as child if it doesn't exist
			Family family = null;
			if (childFamilies.isEmpty()) {
				// find the single parent's family
				family = familyCouncelor.findSingleParentFamily(singleParentId);
				if (family == null) {
					family = new Family(user.getId());
					family.addParent(familyCouncelor.findPerson(singleParentId));
					familyCouncelor.addFamily(family);
				}
				family.addChild(person);
			} else {
				family = childFamilies.get(0);
				// clear any existing parents
				family.getParents().clear();
				family.addParent(familyCouncelor.findPerson(singleParentId));
			}
		}

		// two parents case
		if (parentId1 != null && parentId2 != null) {
			Family family = null;
			if (childFamilies.isEmpty()) {
				family = familyCouncelor.findFamily(parentId1, parentId2);
				if (family == null) {
					family = new Family(user.getId());
					family.addParent(familyCouncelor.findPerson(parentId1));
					family.addParent(familyCouncelor.findPerson(parentId2));
					familyCouncelor.addFamily(family);
				}
				family.addChild(person);
			} else {
				family = childFamilies.get(0);
			}
			// clear any existing parents and add the submitted parents
			family.getParents().clear();
			family.addParent(familyCouncelor.findPerson(parentId1));
			family.addParent(familyCouncelor.findPerson(parentId2));
		}
	}

	public static List<Map<String, Object>> getEditFamilyData(List<Person> persons, List<Family> families) {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		persons.forEach(person -> {
			Map<String, Object> map = new HashMap<String, Object>();
			Set<Long> spouses = new HashSet<Long>();

			map.put("person", person);

			StringBuilder spouseIds = new StringBuilder();
			families.stream().filter(f -> f.getParents().contains(person)).forEach(f -> {
				f.getParents().forEach(parent -> {
					if (!parent.equals(person))
						spouseIds.append("," + parent.getId());
				});
			});
			map.put("spouses", spouseIds.toString().replaceFirst(",", ""));

			StringBuilder parentIds = new StringBuilder();
			families.stream().filter(f -> f.getChildren().contains(person)).forEach(f -> {
				f.getParents().forEach(parent -> parentIds.append("," + parent.getId()));
			});
			map.put("parents", parentIds.toString().replaceFirst(",", ""));

			data.add(map);
		});

		return data;
	}

}
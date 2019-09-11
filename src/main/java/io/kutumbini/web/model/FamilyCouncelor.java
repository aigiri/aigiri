package io.kutumbini.web.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Family;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.services.FamilyTreeService;
import io.kutumbini.validation.ValidationException;

/**
 * Provides validation and finder methods with various criteria to locate
 * families
 *
 */
public class FamilyCouncelor {

	private User user;
	private FamilyTreeService familyTreeService;
	private List<Person> persons;
	private List<Family> families;
	private Set<Family> addedFamilies;
	private Set<Family> removedFamilies;

	public FamilyCouncelor(HttpSession session, User user, FamilyTreeService familyTreeService) {
		this.user = user;
		this.familyTreeService = familyTreeService;
		addedFamilies = new HashSet<Family>();
		removedFamilies = new HashSet<Family>();
		persons = (List<Person>) session.getAttribute(Constants.PERSONS);
		families = (List<Family>) session.getAttribute(Constants.FAMILIES);
	}

	public void saveToRepository() {
		// remove families that may have become invalid after after complex updates
		families.forEach(f -> {
			if (!f.isValid())
				removedFamilies.add(f);
		});

		Set<Person> allPersons = new HashSet<Person>();
		families.forEach(f -> allPersons.addAll(f.getChildren()));
		families.forEach(f -> allPersons.addAll(f.getParents()));
		addedFamilies.forEach(f -> allPersons.addAll(f.getChildren()));
		addedFamilies.forEach(f -> allPersons.addAll(f.getParents()));

		// check for duplicate families
		for (Person p1 : allPersons) {
			for (Person p2 : allPersons) {
				List<Family> fmlies = new ArrayList<Family>();
				// single parent families
				if (p1.equals(p2)) {
					Person p = p1; // = p2
					fmlies = findFamilies(p.getId()).stream().filter(f -> f.getParents().size() == 1)
							.collect(Collectors.toList());
				}
				// find families with p1 and p2 as parents
				else {
					fmlies = findFamilies(p1.getId()).stream().filter(f -> f.getParents().contains(p2))
							.collect(Collectors.toList());
				}
				mergeFamilies(fmlies);
			}
		}

		// now save
		removedFamilies.forEach(f -> familyTreeService.deleteFamily(f.getId(), user));
		addedFamilies.forEach(f -> familyTreeService.saveFamily(f));

		familyTreeService.savePersons(persons);
		familyTreeService.saveFamilies(families);
	}

	// merge all children of this single parent into the first family and remove the
	// rest
	private void mergeFamilies(List<Family> fmlies) {
		if (fmlies.size() > 1) {
			Family keeper = fmlies.get(0);
			for (int i = 1; i < fmlies.size(); i++) {
				fmlies.get(i).getChildren().forEach(child -> keeper.addChild(child));
				removeFamily(fmlies.get(i));
			}
		}
	}

	public Person findPerson(long pid) {
		Optional<Person> operson = persons.stream().filter(p -> p.getId() == pid).findAny();
		if (!operson.isPresent()) {
			throw new ValidationException("Person with id " + pid + " is not present in the session");
		}

		return operson.get();
	}

	// returns the Family that has the passed parent as the single parent
	public Family findSingleParentFamily(long parentid) {
		Family family = families.stream().filter(f -> f.getParents().size() == 1)
				.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid)).findAny()
				.orElse(null);
		if (family == null) {
			// try in the newly added list
			family = addedFamilies.stream().filter(f -> f.getParents().size() == 1)
					.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid)).findAny()
					.orElse(null);
		}
		return family;
	}

	// returns the Family that has the passed parents
	public Family findFamily(long parentid, long parentid2) {
		Family family = families.stream()
				.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid))
				.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid2)).findAny()
				.orElse(null);
		if (family == null) {
			family = addedFamilies.stream()
					.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid))
					.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid2)).findAny()
					.orElse(null);
		}
		return family;
	}

	public List<Family> findFamilies(long parentid) {
		List<Family> filtered = families.stream()
				.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid))
				.collect(Collectors.toList());
//		check addedFamilies
		List<Family> filtered2 = addedFamilies.stream()
				.filter(f -> f.getParents().stream().anyMatch(parent -> parent.getId() == parentid))
				.collect(Collectors.toList());
		filtered.addAll(filtered2);

		return filtered;
	}

	public List<Family> findChildFamilies(Person child) {
		// filter for person as child
		List<Family> filtered = families.stream().filter(f -> f.getChildren().contains(child))
				.collect(Collectors.toList());
//		check addedFamilies
		List<Family> filtered2 = addedFamilies.stream().filter(f -> f.getChildren().contains(child))
				.collect(Collectors.toList());
		filtered.addAll(filtered2);

		return filtered;
	}

	public Set<Long> findSpouseIds(Person person) {
		Set<Long> spouseIds = new HashSet();

		families.stream().filter(f -> f.getParents().contains(person)).forEach(f -> {
			f.getParents().forEach(spouse -> {
				if (!spouse.equals(person))
					spouseIds.add(spouse.getId());
			});
		});
//		check addedFamilies
		addedFamilies.stream().filter(f -> f.getParents().contains(person)).forEach(f -> {
			f.getParents().forEach(spouse -> {
				if (!spouse.equals(person))
					spouseIds.add(spouse.getId());
			});
		});

		return spouseIds;
	}

	public void addFamily(Family f) {
		validate(f);
		addedFamilies.add(f);
	}

	public void removeFamily(Family f) {
		removedFamilies.add(f);
		families.remove(f);
	}

	public void validate(Family f) {
		// avoid creating duplicate families!
		// if the passed has two parents, make sure there is no two-parent family that
		// was already added
		// to this list or is in the session list
		// if the passed has a single parent, make sure there is no single-parent family
		// that was already added
		// to this list or is in the session list
		if (f.getParents().size() == 1) {
			Person p = f.getParents().iterator().next();
			Family family = findSingleParentFamily(p.getId());
			if (family != null) {
				throw new ValidationException("Family " + f + " is a dupliacte of " + family);
			}
		} else if (f.getParents().size() == 2) {
			Person p1 = f.getParents().iterator().next();
			Person p2 = f.getParents().iterator().next();
			Family family = findFamily(p1.getId(), p2.getId());
			if (family != null) {
				throw new ValidationException("Family " + f + " is a dupliacte of " + family);
			}
		} else {
			throw new ValidationException("Family " + f + " has " + f.getParents().size() + " number of parents");
		}
	}

}
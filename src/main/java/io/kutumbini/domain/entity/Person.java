package io.kutumbini.domain.entity;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Required;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.relationship.RELATION;
import io.kutumbini.validation.ValidationException;


/**
 * Represents a node in the family tree
 *
 */
@NodeEntity
public class Person {

    @Id
    @GeneratedValue
	private Long id;
    @Required
	private String firstname;
    @Required
	private String lastname;

	// outgoing relation by default
	@Required
	@Relationship(RELATION.OWNED_BY)
	private User user;
	
	// outgoing relation by default
	@Relationship(RELATION.PARENT)
	private Set<Person> parents = new HashSet<>();

//	@JsonIgnoreProperties is for tools performing JSON serialization, to prevent infinite loop, spouse <-> spouse 
	@JsonIgnoreProperties("spouse")
	@Relationship(RELATION.SPOUSE)
	private Set<Person> spouses = new HashSet<>();

	public Person() {
	}

	public Person(String firstname, String lastname, User user) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.user = user;
	}
	
	public List<Person> getAncestors() {
		List<Person> ancestors = new ArrayList<>();
		ancestors.add(this);
		parents.forEach(p -> {ancestors.addAll(p.getAncestors());});
		return ancestors;
	}
	
	public List<Person> getRootAncestors() {
		List<Person> roots = new ArrayList<>();
		if (parents.isEmpty()) {
			roots.add(this);
		}
		else {
			parents.forEach(p -> {roots.addAll(p.getRootAncestors());});
		}
		return roots;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	public void addParent(Person p) {
		// make sure 'this' is not already a parent or an ancestor of p 
		if (p.getAncestors().contains(this)) throw new ValidationException("Cyclical parent-child relationship not allowed");
		this.parents.add(p);
	}
	
	public Set<Person> getParents() {
		return parents;
	}

	public void addSpouse(Person spouse) {
		if (!spouses.contains(spouse)) {
			spouses.add(spouse);
			spouse.addSpouse(this);
		}
	}
	
	public Set<Person> getSpouses() {
		return spouses;
	}

	public List<Person> getRelations() {
		List<Person> relations = new ArrayList<>();
		relations.addAll(parents);
		relations.addAll(spouses);
		return relations;
	}

	public Long getId() {
		return id;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}
	
	public String getFullname() {
		return firstname + " " + lastname;
	}

	@Override
	public boolean equals(Object obj) {
		if (this.id != null) {
			return (obj instanceof Person) && ((Person)obj).id == this.id;
		}
		else if (this.firstname != null) {
			return (obj instanceof Person) && this.getFirstname().equals(((Person)obj).getFirstname());
		}
		else {
			return (obj instanceof Person);
		}
	}
	
	@Override
	public String toString() {
		return "id:" + id + " " + getFullname();
	}

}
package io.kutumbini.domain.entity;


import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Required;

import io.kutumbini.domain.relationship.RELATION;


/**
 * Represents a node in the family tree
 *
 */
@NodeEntity
public class PersonA {

    @Id
    @GeneratedValue
	private Long id;
    @Required
	private String firstname;
    @Required
	private String lastname;

	// this person is a child of the family
	@Relationship(type=RELATION.CHILD, direction=Relationship.INCOMING)
	private Family bornIn;
	
	// this person is a parent in each of of the families
	// could be more than one family if multiple marriages
	@Relationship(type=RELATION.PARENT, direction=Relationship.INCOMING)
	private Set<Family> families = new HashSet<>();

	public PersonA() {
	}

	public PersonA(String firstname, String lastname) {
		this.firstname = firstname;
		this.lastname = lastname;
	}
	
	public void setBornIn(Family bornIn) {
		this.bornIn = bornIn;
	}
	
	public Family getBornIn() {
		return bornIn;
	}
	
	public void addFamily(Family family) {
		if (!families.contains(family)) {
			families.add(family);
		}
	}
	
	public Set<Family> getFamilies() {
		return families;
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
			return (obj instanceof PersonA) && ((PersonA)obj).id == this.id;
		}
		else if (this.firstname != null) {
			return (obj instanceof PersonA) && this.getFirstname().equals(((PersonA)obj).getFirstname());
		}
		else {
			return (obj instanceof PersonA);
		}
	}
	
	@Override
	public String toString() {
		return "id:" + id + " " + getFullname();
	}

}
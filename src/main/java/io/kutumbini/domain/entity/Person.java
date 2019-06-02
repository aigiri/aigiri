package io.kutumbini.domain.entity;


import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Required;


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
    private Gender gender;

	public Person() {
	}

	public Person(String firstname, String lastname, Gender gender) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.gender = gender;
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

	public Gender getGender() {
		return gender;
	}
	
	public void setGender(Gender gender) {
		this.gender = gender;
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
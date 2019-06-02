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
 * Represents a family: Parents and children
 *
 */
@NodeEntity
public class Family {

    @Id
    @GeneratedValue
	private Long id;
	
	// TODO ygiri @Required is not working
	@Required
	private long userId;
	
	@Relationship(RELATION.PARENT)
	private Set<Person> parents = new HashSet<>();

	@Relationship(RELATION.CHILD)
	private Set<Person> children = new HashSet<>();

	private String name;
	
	public Family() {
	}

	public long getUserId() {
		return userId;
	}
	
	public void setUserId(long userId) {
		this.userId = userId;
	}

	public Set<Person> getParents() {
		return parents;
	}

	public void addParent(Person p) {
		if (!parents.contains(p)) {
			parents.add(p);
		}
	}

	public Set<Person> getChildren() {
		return children;
	}

	public void addChild(Person child) {
		if (!children.contains(child)) {
			children.add(child);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this.id != null) {
			return (obj instanceof Family) && ((Family)obj).id == this.id;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "id:" + id + " " + parents;
	}
	
	public String getName() {
		if (name == null) {
			StringBuilder namesb = new StringBuilder();
			Set<String> names = new HashSet<String>();
			parents.forEach(p -> names.add(p.getLastname()));
			names.forEach(name -> {
					if (namesb.length() == 0) {
						namesb.append(name);
					}
					else {namesb.append("-" + name);}});
			name = namesb.toString();
		}
		return name;
	}
	
	public Long getId() {
		return id;
	}

}
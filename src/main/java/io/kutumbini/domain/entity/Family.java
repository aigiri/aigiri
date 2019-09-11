package io.kutumbini.domain.entity;


import java.util.HashSet;
import java.util.Set;

import javax.validation.ValidationException;

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

	public Family(long userId) {
		this.userId = userId;
	}

	public long getUserId() {
		return userId;
	}
	
	public Set<Person> getParents() {
		return parents;
	}

	public void addParent(Person p) {
		if (parents.size() == 2) {
			throw new ValidationException("Family already has two parents");
		}
		else if (!parents.contains(p)) {
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
	public int hashCode() {
		Set<Long> ids = new HashSet<Long>();
		this.getChildren().forEach(e -> ids.add(e.getId()));
		this.getParents().forEach(e -> ids.add(e.getId()));
		return ids.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Family) {
			Family other = (Family) obj;
			Set<Long> ids = new HashSet<Long>();
			this.getChildren().forEach(e -> ids.add(e.getId()));
			this.getParents().forEach(e -> ids.add(e.getId()));
			
			Set<Long> otherIds = new HashSet<Long>();
			other.getChildren().forEach(e -> otherIds.add(e.getId()));
			other.getParents().forEach(e -> otherIds.add(e.getId()));
			
			return ids.equals(otherIds);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + id + "] Parents " + parents + " - Children " + children;
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

	public boolean isMember(Person p) {
		return getParents().contains(p) || getChildren().contains(p);
	}

	public boolean isValid() {
		return getParents().size() + getChildren().size() > 1;
	}

}
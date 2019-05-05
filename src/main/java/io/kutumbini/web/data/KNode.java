package io.kutumbini.web.data;

import io.kutumbini.domain.entity.Person;

/**
 * 
 * Holds a couple ( spouses ) or a single person if y is missing
 *
 */
public class KNode {

	public Person x;
	
	// if present, represents spouse
	public Person y;
	
	public KNode(Person x, Person y) {
		this.x = x;
		this.y = y;
	}

	public String getIdString() {
		String idString = x.getId().toString() + (y != null? "-" + y.getId().toString() : "");
		return idString;
	}
	
	public String getName() {
		String name = null;
		if (y==null) {
			name = x.getFullname();
		}
		else if (x.getLastname().equalsIgnoreCase(y.getLastname())) {
			name = x.getFirstname() + " - " + y.getFullname();
		}
		else {
			name = x.getFullname() + " - " + y.getFullname();
		}
		return name;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public int hashCode() {
		return getIdString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean evalue = obj instanceof KNode;
		return evalue && ((KNode)obj).getIdString().equals(getIdString());
	}
}

package io.kutumbini.web.data;

import io.kutumbini.domain.entity.Person;
import lombok.Data;

/**
 * 
 * Holds a couple ( spouses ) or a single person if y is missing
 *
 */
@Data
public class KNode {

	private Person x;
	
	// if present, represents spouse
	private Person y;
	
	public KNode(Person x, Person y) {
		this.x = x;
		this.y = y;
	}

}

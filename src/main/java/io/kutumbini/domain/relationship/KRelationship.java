package io.kutumbini.domain.relationship;

import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity
public enum KRelationship {
	
	PARENT, CHILD, OWNED_BY, DELEGATE_FULL, DELEGATE_PERIPHERAL;

}

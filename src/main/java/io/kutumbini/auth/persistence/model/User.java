package io.kutumbini.auth.persistence.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.aerogear.security.otp.api.Base32;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import io.kutumbini.domain.entity.Person;
import io.kutumbini.domain.relationship.RELATION;

@NodeEntity
public class User {

    @Id
    @GeneratedValue
    private Long id;
    
    private String firstname;

    private String lastname;

    private String email;

    private String password;

    private boolean enabled;

    private boolean isUsing2FA;

    private String secret;

	private Set<Role> roles;
	
	@Relationship(type = RELATION.OWNED_BY, direction = Relationship.INCOMING)
	private List<Person> owned = new ArrayList<>();
	
	@Relationship(type = RELATION.DELEGATE_FULL, direction = Relationship.INCOMING)
	private List<User> delegatedFull = new ArrayList<>();

	@Relationship(type = RELATION.DELEGATE_PERIPHERAL, direction = Relationship.INCOMING)
	private List<User> delegatedPeripheral = new ArrayList<>();

    public User() {
        super();
        this.secret = Base32.random();
        
        // TODO ygiri change this to false after implementing email confirmation
        // the confirmation event can then set this to true
        this.enabled = true; 
        
        // TODO ygiri need another class to determine what role to assign
        roles = new HashSet();
        roles.add(Role.ROLE_USER);
    }

	public void addOwned(Person p) {
		this.owned.add(p);
	}
	
	public List<Person> getOwned() {
		return owned;
	}

	public void addDelagatedFull(User u) {
		this.delegatedFull.add(u);
	}
	
	public List<User> getDelegatedFull() {
		return delegatedFull;
	}

	public void addDelagatedPeripheral(User u) {
		this.delegatedPeripheral.add(u);
	}
	
	public List<User> getDelegatedPeripheral() {
		return delegatedPeripheral;
	}

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String username) {
        this.email = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

	public void addRole(Role role) {
		roles.add(role);
	}

	public Collection<Role> getRoles() {
    	// TODO ygiri remove this!
    	if (roles == null) {
            roles = new HashSet<>();
            roles.add(Role.ROLE_USER);
    	}
        return roles;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isUsing2FA() {
        return isUsing2FA;
    }

    public void setUsing2FA(boolean isUsing2FA) {
        this.isUsing2FA = isUsing2FA;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((email == null) ? 0 : email.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User user = (User) obj;
        if (!email.equals(user.email)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("User [id=").append(id).append(", firstName=").append(firstname).append(", lastName=").append(lastname).append(", email=").append(email).append(", password=").append(password).append(", enabled=").append(enabled).append(", isUsing2FA=")
                .append(isUsing2FA).append(", secret=").append(secret).append("]");
        return builder.toString();
    }

}
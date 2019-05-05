package io.kutumbini.auth.persistence.model;

import java.util.Calendar;
import java.util.Date;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class GrantVerificationToken {

    private static final int EXPIRATION = 60 * 24;

    @Id
    @GeneratedValue
    private Long id;

    private String token;

    private User grantor;

    private Date expiryDate;
    
    // added for kutumbini
    // 0 for peripheral, 1 for full
    private short delegateGrantAccessLevel = -1;
    
    private String grantEmail;
    
    private User grantee;
    
    private Date grantedDate;
    
    public GrantVerificationToken() {
        super();
    }

    public GrantVerificationToken(final String token) {
        super();

        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public GrantVerificationToken(final String token, final User user) {
        super();

        this.token = token;
        this.grantor = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public User getGrantor() {
        return grantor;
    }

    public void setGrantor(final User user) {
        this.grantor = user;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(final Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public short getDelegateGrantAccessLevel() {
    	return delegateGrantAccessLevel;
    }
    
    public void setDelegateGrantAccessLevel(short delegateGrantAccessLevel) {
    	this.delegateGrantAccessLevel = delegateGrantAccessLevel;
    }
    
	public void setEmailTo(String grantEmail) {
		this.grantEmail = grantEmail;
	}

	public void setGrantee(User grantee) {
		this.grantee = grantee;
	}

	private Date calculateExpiryDate(final int expiryTimeInMinutes) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(new Date().getTime());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public void updateToken(final String token) {
        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    //

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expiryDate == null) ? 0 : expiryDate.hashCode());
        result = prime * result + ((token == null) ? 0 : token.hashCode());
        result = prime * result + ((grantor == null) ? 0 : grantor.hashCode());
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
        final GrantVerificationToken other = (GrantVerificationToken) obj;
        if (expiryDate == null) {
            if (other.expiryDate != null) {
                return false;
            }
        } else if (!expiryDate.equals(other.expiryDate)) {
            return false;
        }
        if (token == null) {
            if (other.token != null) {
                return false;
            }
        } else if (!token.equals(other.token)) {
            return false;
        }
        if (grantor == null) {
            if (other.grantor != null) {
                return false;
            }
        } else if (!grantor.equals(other.grantor)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Token [String=").append(token).append("]").append("[Expires").append(expiryDate).append("]");
        return builder.toString();
    }

}

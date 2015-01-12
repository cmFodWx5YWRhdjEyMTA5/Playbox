package uk.co.darkerwaters.client.variables;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Variable implements Serializable {

	private static final long serialVersionUID = -8881355204612144469L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	@Persistent
	private User user;
	@Persistent
	private String variableName;
	@Persistent
	private Date createDate;

	public Variable() {
		this.createDate = new Date();
	}

	public Variable(User user, String variableName) {
		this();
		this.user = user;
		this.variableName = variableName;
	}

	public Long getId() {
		return this.id;
	}

	public User getUser() {
		return this.user;
	}

	public String getVariableName() {
		return this.variableName;
	}

	public Date getCreateDate() {
		return this.createDate;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
}

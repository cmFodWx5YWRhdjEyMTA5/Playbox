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
public class Variables implements Serializable {

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
	@Persistent
	private String readAccessUsers;
	
	public Variables() {
		this.createDate = new Date();
		this.variableName = "";
		this.readAccessUsers = "";
	}

	public Variables(User user) {
		this();
		this.user = user;
	}
	
	public Variables(Variables toCopy) {
		this();
		this.user = toCopy.user == null ? null : toCopy.user;
		this.variableName = toCopy.variableName;
		this.readAccessUsers = toCopy.readAccessUsers;
	}

	public Long getId() {
		return this.id;
	}

	public User getUser() {
		return this.user;
	}
	
	public int getNumberVariables() {
		if (null == this.variableName || this.variableName.isEmpty()) {
			return 0;
		}
		else {
			return this.variableName.split(",").length;
		}
	}

	public String getVariableName(int index) {
		return this.variableName.split(",")[index];
	}

	public Date getCreateDate() {
		return this.createDate;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void addVariableName(String variableName) {
		this.variableName += variableName + ",";
	}

	public void removeVariableName(String variableName) {
		this.variableName = this.variableName.replace(variableName + ",", "");
	}

	public String getVariablesNames() {
		return this.variableName;
	}
	
	public int getNumberSharedUsers() {
		if (null == this.readAccessUsers || this.readAccessUsers.isEmpty()) {
			return 0;
		}
		else {
			return this.readAccessUsers.split(",").length;
		}
	}

	public String getSharedUser(int index) {
		return this.readAccessUsers.split(",")[index];
	}

	public void addSharedUser(String userId) {
		if (null == this.readAccessUsers) {
			this.readAccessUsers = "";
		}
		this.readAccessUsers += userId + ",";
	}

	public void removeSharedUser(String userId) {
		this.readAccessUsers = this.readAccessUsers.replace(userId + ",", "");
	}
}

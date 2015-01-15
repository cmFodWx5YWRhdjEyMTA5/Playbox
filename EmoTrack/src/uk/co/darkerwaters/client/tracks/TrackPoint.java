package uk.co.darkerwaters.client.tracks;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.users.User;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class TrackPoint implements Serializable {
	
	private static final long serialVersionUID = -5136550031509060989L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	@Persistent
	private User user;
	@Persistent
	private String valuesString;
	@Persistent
	private Date trackDate;
	
	public TrackPoint(User user, TrackPointData data) {
		this.user = user;
		// create our data from that passed in
		this.valuesString = data.getValuesString();
		this.trackDate = data.getTrackDate();
	}

	public Long getId() {
		return this.id;
	}
	
	public TrackPointData getData() {
		return new TrackPointData(this.trackDate, this.valuesString);
	}

	public Date getTrackDate() {
		return this.trackDate;
	}

	public void setValuesString(String values) {
		this.valuesString = values;
	}
}

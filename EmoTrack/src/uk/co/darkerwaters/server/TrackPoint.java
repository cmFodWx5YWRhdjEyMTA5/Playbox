package uk.co.darkerwaters.server;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import uk.co.darkerwaters.shared.TrackPointData;

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
	@Persistent
	private String eventString;
	
	public TrackPoint(User user, TrackPointData data) {
		this.user = user;
		// create our data from that passed in
		this.valuesString = data.getValuesString();
		this.trackDate = data.getTrackDate();
		this.eventString = data.getEvent();
	}

	public Long getId() {
		return this.id;
	}
	
	public TrackPointData getData() {
		return new TrackPointData(this.trackDate, this.valuesString, this.eventString);
	}

	public Date getTrackDate() {
		return this.trackDate;
	}

	public void setValuesString(String values) {
		this.valuesString = values;
	}
	
	public void setEventString(String event) {
		this.eventString = event;
	}
}

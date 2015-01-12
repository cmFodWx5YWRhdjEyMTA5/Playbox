package uk.co.darkerwaters.client.tracks;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class TrackPoint implements Serializable {

	private static final long serialVersionUID = -415265892134713347L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	@Persistent
	private String valuesString;
	@Persistent
	private Date trackDate;
	
	private static final String K_VALUES_SEP = ",";
	private static final String K_VALUE_SEP = ":";
	
	public TrackPoint() {
		this(new Date());
	}

	public TrackPoint(Date trackDate) {
		this.trackDate = trackDate;
		this.valuesString = "";
	}

	public Long getId() {
		return this.id;
	}
	
	public String[] getValuesNames() {
		String[] values = this.valuesString.split(K_VALUES_SEP);
		String[] valuesNames = new String[values.length];
		for (int i = 0; i < values.length; ++i) {
			valuesNames[i] = values[i].split(K_VALUE_SEP)[0];
		}
		return valuesNames;
	}
	
	public Integer[] getValuesValues() {
		String[] values = this.valuesString.split(K_VALUES_SEP);
		Integer[] valuesValues = new Integer[values.length];
		for (int i = 0; i < values.length; ++i) {
			valuesValues[i] = Integer.parseInt(values[i].split(K_VALUE_SEP)[1]);
		}
		return valuesValues;
	}
	
	public void addValue(String name, Integer value) {
		this.valuesString += name + K_VALUE_SEP + value.toString() + K_VALUES_SEP;
	}

	public String getValuesString() {
		return this.valuesString;
	}

	public Date getTrackDate() {
		return this.trackDate;
	}

	public void setValuesString(String values) {
		this.valuesString = values;
	}
}

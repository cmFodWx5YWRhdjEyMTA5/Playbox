package uk.co.darkerwaters.client.tracks;

import java.io.Serializable;
import java.util.Date;

public class TrackPointData implements Serializable {

	private static final long serialVersionUID = -7885672008931167066L;
	private String valuesString;
	private long trackDate;
	
	static final String K_VALUES_SEP = ",";
	static final String K_VALUE_SEP = ":";
	
	public TrackPointData() {
		this(new Date());
	}

	public TrackPointData(Date trackDate) {
		this(trackDate, "");
	}

	TrackPointData(Date trackDate, String valuesString) {
		this.trackDate = trackDate == null ? new Date().getTime() : trackDate.getTime();
		this.valuesString = valuesString;
	}

	String getValuesString() {
		return this.valuesString;
	}

	public Date getTrackDate() {
		return new Date(this.trackDate);
	}
	
	@Override
	public String toString() {
		return getTrackDate().toString() + ": " + this.valuesString;
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
		this.valuesString += name + TrackPointData.K_VALUE_SEP + value.toString() + TrackPointData.K_VALUES_SEP;
	}
}

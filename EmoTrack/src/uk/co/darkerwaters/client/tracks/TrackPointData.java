package uk.co.darkerwaters.client.tracks;

import java.io.Serializable;
import java.util.Date;

public class TrackPointData implements Serializable {

	private static final long serialVersionUID = -7885672008931167066L;
	private String valuesString;
	private long trackDate;
	
	private String event;
	
	static final String K_VALUES_SEP = ",";
	static final String K_VALUE_SEP = ":";
	
	//NB - include a space as this is not allowed as a value to track
	public static String[] SLEEPKEY = new String[] {"Awake in bed", "Light sleep", "Deep sleep"};
	
	//NB - include a space as this is not allowed as a value to track
	public static String ACTIVITYKEY = "Activity ";

	public static boolean IsSleepKey(String value) {
		boolean isKey = false;
		for (String key : SLEEPKEY) {
			if (key.equals(value)) {
				isKey = true;
				break;
			}
		}
		return isKey;
	}
	
	public static boolean IsActivityKey(String value) {
		return value.startsWith(ACTIVITYKEY);
	}
	
	public TrackPointData() {
		this(new Date());
	}

	public TrackPointData(Date trackDate) {
		this(trackDate, "", "");
	}
	
	public TrackPointData(Date trackDate, String event) {
		this(trackDate, "", event);
	}

	TrackPointData(Date trackDate, String valuesString, String eventString) {
		this.trackDate = trackDate == null ? new Date().getTime() : trackDate.getTime();
		this.valuesString = valuesString;
		this.event = eventString;
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
		if (null == this.valuesString || this.valuesString.isEmpty()) {
			// there is no data
			return new String[0];
		}
		String[] values = this.valuesString.split(K_VALUES_SEP);
		String[] valuesNames = new String[values.length];
		for (int i = 0; i < values.length; ++i) {
			valuesNames[i] = values[i].split(K_VALUE_SEP)[0];
		}
		return valuesNames;
	}
	
	public Integer[] getValuesValues() {
		if (null == this.valuesString || this.valuesString.isEmpty()) {
			// there is no data
			return new Integer[0];
		}
		String[] values = this.valuesString.split(K_VALUES_SEP);
		Integer[] valuesValues = new Integer[values.length];
		for (int i = 0; i < values.length; ++i) {
			valuesValues[i] = Integer.parseInt(values[i].split(K_VALUE_SEP)[1]);
		}
		return valuesValues;
	}
	
	public String getEvent() {
		return this.event;
	}
	
	public void addValue(String name, Integer value) {
		this.valuesString += name + TrackPointData.K_VALUE_SEP + value.toString() + TrackPointData.K_VALUES_SEP;
	}
}

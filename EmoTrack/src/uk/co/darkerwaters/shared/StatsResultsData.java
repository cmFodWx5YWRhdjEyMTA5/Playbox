package uk.co.darkerwaters.shared;

import java.io.Serializable;

import uk.co.darkerwaters.client.EmoTrack;

public class StatsResultsData implements Serializable {
	
	private static final long serialVersionUID = 2727844250087629721L;
	
	private static final String K_VALUE_SEP = ":";

	private String noneValueTitles = "";
	private String noneValueValues = "";
	
	private String monthlyAverageKeys = "";
	private String monthlyAverageValues = "";
	
	private String weeklyAverageKeys = "";
	private String weeklyAverageValues = "";
	
	public StatsResultsData() {
		// constructor
	}

	public void addMonthlyAverage(String key, float average) {
		this.monthlyAverageKeys += key + K_VALUE_SEP;
		this.monthlyAverageValues += Float.toString(average) + K_VALUE_SEP;
	}

	public void addWeeklyAverage(String key, float average) {
		this.weeklyAverageKeys += key + K_VALUE_SEP;
		this.weeklyAverageValues += Float.toString(average) + K_VALUE_SEP;
	}

	public void addNumberOfNone(String key, int count) {
		this.noneValueTitles += key + K_VALUE_SEP;
		this.noneValueValues += Integer.toString(count) + K_VALUE_SEP;
	}
	
	public String[] getMonthlyAverageKeys() {
		return this.monthlyAverageKeys.split(K_VALUE_SEP);
	}
	
	public String[] getWeeklyAverageKeys() {
		return this.weeklyAverageKeys.split(K_VALUE_SEP);
	}
	
	public float getMonthlyAverage(int index) {
		float value = 0f;
		try {
			value = Float.parseFloat(this.monthlyAverageValues.split(K_VALUE_SEP)[index]);
		}
		catch (Exception e) {
			EmoTrack.LOG.severe("Failed to get the monthly average value at index " + index + " from \"" + this.monthlyAverageValues + "\": " + e.getMessage());
		}
		return value;
	}
	
	public float getWeeklyAverage(int index) {
		float value = 0f;
		try {
			value = Float.parseFloat(this.weeklyAverageValues.split(K_VALUE_SEP)[index]);
		}
		catch (Exception e) {
			EmoTrack.LOG.severe("Failed to get the weekly average value at index " + index + " from \"" + this.weeklyAverageValues + "\": " + e.getMessage());
		}
		return value;
	}
	
	public String[] getNoneValueTitles() {
		return this.noneValueTitles.split(K_VALUE_SEP);
	}
	
	public int getNoneValue(int index) {
		int value = 0;
		try {
			value = Integer.parseInt(this.noneValueValues.split(K_VALUE_SEP)[index]);
		}
		catch (Exception e) {
			EmoTrack.LOG.severe("Failed to get the none value at index " + index + " from \"" + this.noneValueTitles + "\": " + e.getMessage());
		}
		return value;
	}
}

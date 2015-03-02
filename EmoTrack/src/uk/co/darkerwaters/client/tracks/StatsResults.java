package uk.co.darkerwaters.client.tracks;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.shared.StatsResultsData;

public class StatsResults {

	public static DateTimeFormat monthDate = DateTimeFormat.getFormat("yyyy-MM");
	public static DateTimeFormat yearDate = DateTimeFormat.getFormat("yyyy");
	
	private final StatsResultsData data;
	
	public StatsResults(StatsResultsData data) {
		// constructor
		this.data = data;
	}
	
	public Date getDateFromMonthlyKey(String key) {
		Date toReturn = null;
		String[] split = key.split(" ");
		try {
			toReturn = monthDate.parse(split[0]);
		}
		catch (Exception e) {
			EmoTrack.LOG.severe("Failed to parse monthly date key \"" + key + "\": " + e.getMessage());
		}
		return toReturn;
	}
	
	public Date getDateFromWeeklyKey(String key) {
		Date toReturn = null;
		String[] split = key.split(" ");
		try {
			String[] dateParts = split[0].split("-");
			toReturn = monthDate.parse(dateParts[0] + "-Jan");
			CalendarUtil.setToFirstDayOfMonth(toReturn);
			int weekNumber = Integer.parseInt(dateParts[1]);
			CalendarUtil.addDaysToDate(toReturn, weekNumber * 7);
		}
		catch (Exception e) {
			EmoTrack.LOG.severe("Failed to parse weekly date key \"" + key + "\": " + e.getMessage());
		}
		return toReturn;
	}
	
	public int getWeekNumberWeeklyKey(String key) {
		int toReturn = 0;
		String[] split = key.split(" ");
		try {
			String[] dateParts = split[0].split("-");
			toReturn = Integer.parseInt(dateParts[1]);
		}
		catch (Exception e) {
			EmoTrack.LOG.severe("Failed to parse week number key \"" + key + "\": " + e.getMessage());
		}
		return toReturn;
	}
	
	public String getTitleNameFromWeeklyKey(String key) {
		return getTitleNameFromMonthlyKey(key);
	}
	
	public String getTitleNameFromMonthlyKey(String key) {
		String toReturn = null;
		int splitIndex = key.indexOf(" ");
		if (-1 != splitIndex) {
			toReturn = key.substring(splitIndex + 1, key.length());
		}
		return toReturn;
	}
	
	public String[] getMonthlyAverageKeys() {
		return this.data.getMonthlyAverageKeys();
	}
	
	public String[] getWeeklyAverageKeys() {
		return this.data.getWeeklyAverageKeys();
	}
	
	public float getMonthlyAverage(int index) {
		return this.data.getMonthlyAverage(index);
	}
	
	public float getWeeklyAverage(int index) {
		return this.data.getWeeklyAverage(index);
	}
	
	public String[] getNoneValueTitles() {
		return this.data.getNoneValueTitles();
	}
	
	public int getNoneValue(int index) {
		return this.data.getNoneValue(index);
	}
}

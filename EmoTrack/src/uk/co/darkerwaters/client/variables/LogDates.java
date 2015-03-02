package uk.co.darkerwaters.client.variables;

import java.util.Date;

import uk.co.darkerwaters.client.EmoTrackConstants;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public enum LogDates {
	today(EmoTrackConstants.Instance.timeToday()),
	now(EmoTrackConstants.Instance.timeNow()),
	one_hour(EmoTrackConstants.Instance.timeOneHour()),
	two_hour(EmoTrackConstants.Instance.timeTwoHour()),
	this_morning(EmoTrackConstants.Instance.timeThisMorning()),
	this_afternoon(EmoTrackConstants.Instance.timeThisAfternoon()),
	this_evening(EmoTrackConstants.Instance.timeThisEvening()),
	yesterday(EmoTrackConstants.Instance.timeYesterday()),
	other(EmoTrackConstants.Instance.timeOther());
	
	public final String title;
	public static DateTimeFormat datefmt = DateTimeFormat.getFormat("yyyy-MM-dd");
	public static DateTimeFormat dateHrfmt = DateTimeFormat.getFormat("yyyy-MM-dd HH");
	
	LogDates(String title) {
		this.title = title;
	}
	public Date getDate() {
		Date toReturn = new Date();
		String timeFormat = datefmt.format(toReturn);
		switch(this) {
		case today :
			// just return today
			toReturn = dateHrfmt.parse(timeFormat + " 12");
			break;
		case now :
			// return now, just the default
			break;
		case one_hour :
			// return an hour ago
			toReturn.setTime(toReturn.getTime() - 60 * 60000);
			break;
		case two_hour :
			// return two hours ago
			toReturn.setTime(toReturn.getTime() - 120 * 60000);
			break;
		case this_morning :
			// set to this morning, first add the hours we want to this
			// and parse into the time we want
			toReturn = dateHrfmt.parse(timeFormat + " 10");
			break;
		case this_afternoon:
			// set to this afternoon, first add the hours we want to this
			// and parse into the time we want
			toReturn = dateHrfmt.parse(timeFormat + " 15");
			break;
		case this_evening :
			// set to this evening, first add the hours we want to this
			// and parse into the time we want
			toReturn = dateHrfmt.parse(timeFormat + " 20");
			break;
		case yesterday :
			// set to yesterday, just take a day off this date, use midday though to be nice
			toReturn = dateHrfmt.parse(timeFormat + " 12");
			CalendarUtil.addDaysToDate(toReturn, -1);
			break;
		case other :
			// get the date from the date picker instead
			break;
		}
		return toReturn;
	}
	
	public static Date limitDateToDay(Date value) {
		String dayDateStr = LogDates.datefmt.format(value);
		return LogDates.dateHrfmt.parse(dayDateStr + " 12");
	}
}
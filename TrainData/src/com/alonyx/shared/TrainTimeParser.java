package com.alonyx.shared;

import java.util.Date;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

public class TrainTimeParser {
 

	@SuppressWarnings("deprecation")
	public static Date getEventTimeDate(String eventTimeString) {
		// the event time for this data is formatted thus...
		//"5/5/2015 4:48:36 AM";
		eventTimeString = eventTimeString.replaceAll("\"", "");
		// do this with regex so works in the client and server
		String pattern = "^(\\d{1,2})/(\\d{1,2})/(\\d{4}) (\\d{1,2}):(\\d{2}):(\\d{1,2}) (?:AM|PM)$";
		
		RegExp regExp = RegExp.compile(pattern);
		MatchResult m = regExp.exec(eventTimeString);
 
        Date toReturn = null;
        if (null != m && m.getGroupCount() >= 7) { 
            // create the date
            try {
	            toReturn = new Date(Integer.parseInt(m.getGroup(3)) - 1900, 
	            		Integer.parseInt(m.getGroup(1)) - 1,	// month is zero index 
	            		Integer.parseInt(m.getGroup(2)), 
	            		Integer.parseInt(m.getGroup(4)) + (eventTimeString.contains("AM") ? 0 : 12), 
	            		Integer.parseInt(m.getGroup(5)), 
	            		Integer.parseInt(m.getGroup(6)));
	            //prints example 
	            //System.out.println(eventTimeString + " is " + toReturn.toString());
	            //TODO this creates a time in BST - if on a UK machine (might miss trains when transitioning times +- an hour)
            }
            catch (NumberFormatException e) {
            	e.printStackTrace();
            }
        } 
        return toReturn;
	}

	@SuppressWarnings("deprecation")
	public static Date getNextArrivalDate(String nextArrivalString) {
		
		// the next arrival time for this data is formatted thus...
		//"4:48:36 AM";
		nextArrivalString = nextArrivalString.replaceAll("\"", "");
		// do this with regex so works in the client and server
		String pattern = "^(\\d{1,2}):(\\d{2}):(\\d{1,2}) (?:AM|PM)$";
 
		RegExp regExp = RegExp.compile(pattern);
		MatchResult m = regExp.exec(nextArrivalString);
 
        Date toReturn = null;
        if (null != m && m.getGroupCount() >= 3) { 
            // create the date
            try {
            	Date now = new Date();
	            toReturn = new Date(now.getYear(), 
	            		now.getMonth(), 
	            		now.getDay(), 
	            		Integer.parseInt(m.getGroup(1)) + (nextArrivalString.contains("AM") ? 0 : 12), 
	            		Integer.parseInt(m.getGroup(2)), 
	            		Integer.parseInt(m.getGroup(3)));
	            //prints example 
	            //System.out.println(nextArrivalString + " is " + toReturn.toString());
	            //TODO this creates a time in BST - if on a UK machine (might miss trains when transitioning times +- an hour)
            }
            catch (NumberFormatException e) {
            	e.printStackTrace();
            }
        } 
        return toReturn;
	}

}

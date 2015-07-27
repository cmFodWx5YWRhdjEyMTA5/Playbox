package com.alonyx.traindataserver;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

public class TrainTimeParser {
 

	@SuppressWarnings("deprecation")
	public static Date getEventTimeDate(String eventTimeString) {
		// the event time for this data is formatted thus...
		//"5/5/2015 4:48:36 AM";
		eventTimeString = eventTimeString.replaceAll("\"", "");
		// do this with regex so works in the client and server
		Pattern p = Pattern.compile("^(\\d{1,2})/(\\d{1,2})/(\\d{4}) (\\d{1,2}):(\\d{2}):(\\d{1,2}) (?:AM|PM)$");
		Matcher m = p.matcher(eventTimeString);
		
		if (false == m.matches()) {
			LoggerFactory.getLogger(TrainTimeParser.class).error("Failed to match \"" + eventTimeString + "\" to an event date");
			return null;
		} 
        Date toReturn = null;
        if (null != m && m.groupCount() >= 6) { 
            // create the date
            try {
	            toReturn = new Date(Integer.parseInt(m.group(3)) - 1900, 
	            		Integer.parseInt(m.group(1)) - 1,	// month is zero index 
	            		Integer.parseInt(m.group(2)), 
	            		Integer.parseInt(m.group(4)) + (eventTimeString.contains("AM") ? 0 : 12), 
	            		Integer.parseInt(m.group(5)), 
	            		Integer.parseInt(m.group(6)));
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
		Pattern p = Pattern.compile("^(\\d{1,2}):(\\d{2}):(\\d{1,2}) (?:AM|PM)$");
		Matcher m = p.matcher(nextArrivalString);
		
		if (false == m.matches()) {
			LoggerFactory.getLogger(TrainTimeParser.class).error("Failed to match \"" + nextArrivalString + "\" to an arrival date");
			return null;
		} 
        Date toReturn = null;
        if (null != m && m.groupCount() >= 3) { 
            // create the date
            try {
            	Date now = new Date();
	            toReturn = new Date(now.getYear(), 
	            		now.getMonth(), 
	            		now.getDay(), 
	            		Integer.parseInt(m.group(1)) + (nextArrivalString.contains("AM") ? 0 : 12), 
	            		Integer.parseInt(m.group(2)), 
	            		Integer.parseInt(m.group(3)));
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

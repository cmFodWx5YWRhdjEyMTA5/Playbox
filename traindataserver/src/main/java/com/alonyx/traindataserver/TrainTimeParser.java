package com.alonyx.traindataserver;

import java.util.Date;

public class TrainTimeParser {
 

	@SuppressWarnings("deprecation")
	public static Date getEventTimeDate(String eventTimeString) {
		// the event time for this data is formatted thus...
		//"5/5/2015 4:48:36 AM";
		eventTimeString = eventTimeString.replaceAll("\"", "");
		// do this with regex so works in the client and server
		String pattern = "^(\\d{1,2})/(\\d{1,2})/(\\d{4}) (\\d{1,2}):(\\d{2}):(\\d{1,2}) (?:AM|PM)$";
		
		String[] split = eventTimeString.split(pattern);
 
        Date toReturn = null;
        if (null != split && split.length >= 7) { 
            // create the date
            try {
	            toReturn = new Date(Integer.parseInt(split[2]) - 1900, 
	            		Integer.parseInt(split[0]) - 1,	// month is zero index 
	            		Integer.parseInt(split[1]), 
	            		Integer.parseInt(split[3]) + (eventTimeString.contains("AM") ? 0 : 12), 
	            		Integer.parseInt(split[4]), 
	            		Integer.parseInt(split[5]));
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
 
		String[] split = nextArrivalString.split(pattern);
		
        Date toReturn = null;
        if (null != split && split.length >= 3) { 
            // create the date
            try {
            	Date now = new Date();
	            toReturn = new Date(now.getYear(), 
	            		now.getMonth(), 
	            		now.getDay(), 
	            		Integer.parseInt(split[0]) + (nextArrivalString.contains("AM") ? 0 : 12), 
	            		Integer.parseInt(split[1]), 
	            		Integer.parseInt(split[2]));
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

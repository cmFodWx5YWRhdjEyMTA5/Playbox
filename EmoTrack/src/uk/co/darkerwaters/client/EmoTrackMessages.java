package uk.co.darkerwaters.client;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface EmoTrackMessages extends Messages {
	
	public static final EmoTrackMessages Instance = GWT.create(EmoTrackMessages.class);
	
	@DefaultMessage("''{0}'' is not a valid variable name, no punctuation please.")
	String invalidVariableName(String symbol);

	@DefaultMessage("Last update: {0,date,medium} {0,time,medium}")
	String lastUpdate(Date timestamp);

	@DefaultMessage("''{0}'' is not a valid variable name to track, you are already tracking this...")
	String usedVariableName(String variableName);

	@DefaultMessage("Selected data at {0,date,medium}")
	String selectedDate(Date date);
	
	@DefaultMessage("No {0} at all")
	String describeVal0(String valueTitle);
	
	@DefaultMessage("Only a little {0}")
	String describeVal01(String valueTitle);
	
	@DefaultMessage("A little {0}")
	String describeVal02(String valueTitle);
	
	@DefaultMessage("Some {0}")
	String describeVal03(String valueTitle);
	
	@DefaultMessage("Quite a lot of {0}")
	String describeVal04(String valueTitle);
	
	@DefaultMessage("A lot of {0}")
	String describeVal05(String valueTitle);
	
	@DefaultMessage("Loads of {0}")
	String describeVal06(String valueTitle);
	
	@DefaultMessage("Too much {0}")
	String describeVal07(String valueTitle);
	
	@DefaultMessage("A huge amount of {0}")
	String describeVal08(String valueTitle);
	
	@DefaultMessage("An overwhelming amount of {0}")
	String describeVal09(String valueTitle);
	
	@DefaultMessage("The most {0} possible")
	String describeVal10(String valueTitle);
	
	@DefaultMessage("Current value is a number:  {0}")
	String describeValErr(int value);
}

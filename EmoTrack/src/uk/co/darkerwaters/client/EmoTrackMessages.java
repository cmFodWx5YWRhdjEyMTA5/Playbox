package uk.co.darkerwaters.client;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

public interface EmoTrackMessages extends Messages {
	
	public static final EmoTrackMessages Instance = GWT.create(EmoTrackMessages.class);
	
	@DefaultMessage("Sorry, but ''{0}'' is not a valid emotion title, please enter a simple, single word (no punctuation).")
	String invalidVariableName(String symbol);
	
	@DefaultMessage("Sorry, but ''{0}'' is not a valid event title, no punctuation please.")
	String invalidEventTitle(String symbol);

	@DefaultMessage("Last update: {0,date,medium} {0,time,medium}")
	String lastUpdate(Date timestamp);

	@DefaultMessage("Sorry, but ''{0}'' is not a valid emotion to track, you are already tracking this...")
	String usedVariableName(String variableName);

	@DefaultMessage("Selected data at {0,date,medium}")
	String selectedDate(Date date);
	
	@DefaultMessage("{0,date,medium}")
	String date(Date date);
	
	@DefaultMessage("{0,time,short}")
	String time(Date date);
	
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
	
	@DefaultMessage("Overwhelming {0}")
	String describeVal09(String valueTitle);
	
	@DefaultMessage("The most {0} possible")
	String describeVal10(String valueTitle);
	
	@DefaultMessage("Current value is a number: {0}")
	String describeValErr(int value);

	@DefaultMessage("Please log in to your google account to use this service")
	String notLoggedIn();

	@DefaultMessage("Delete \"{0}\" as a tracked variable")
	String tipDeleteButton(String variableTitle);

	@DefaultMessage("Set the value of \"{0}\" to track")
	String tipEmotionSlider(String title);

	@DefaultMessage("Zero is not a valid amount to track, please enter a number")
	String invalidAmount();
}

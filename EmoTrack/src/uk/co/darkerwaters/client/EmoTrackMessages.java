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
}

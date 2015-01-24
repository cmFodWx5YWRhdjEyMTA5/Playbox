package uk.co.darkerwaters.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

public interface EmoTrackConstants extends Constants {
	/** some generic constants for 100% width, cell spacing and such*/
	public static final String K_100PERCENT = "100%";
	public static final int K_TABLECELLSPACING = 6;
	/** some more constants for all the css IDs we are using in the code */ 
	public static final String K_CSS_ID_APPTITLE = "appTitle";
	public static final String K_CSS_ID_APPPLACEHOLDER = "appPlaceholder";
	public static final String K_CSS_ID_APPPLACEHOLDERENTRY = "appDataEntry";
	public static final String K_CSS_ID_APPPLACEHOLDERDISPLAY = "appDataDisplay";
	public static final String K_CSS_ID_TIMESELECTIMAGE = "timeSelectImage";
	public static final String K_CSS_ID_EVENTTEXTBOX = "eventEntry";
	public static final String K_CSS_ID_VARIABLETABLEPANEL = "variableTablePanel";
	public static final String K_CSS_ID_ADDPANEL = "variableAddPanel";
	public static final String K_CSS_ID_LOGBUTTON = "variableLogButton";
	public static final String K_CSS_ID_ADDBUTTON = "variableAddButton";
	public static final String K_CSS_ID_VARIABLETEXTBOX = "variableEntry";
	public static final String K_CSS_ID_VARIABLESCHART = "variablesChart";
	public static final String K_CSS_ID_DATACHART = "dataChart";
	public static final String K_CSS_ID_DATACHARTPANEL = "dataChartPanel";
	public static final String K_CSS_ID_DATACHARTDATAPANEL = "dataChartDataPanel";
	public static final String K_CSS_ID_DATACHARTSELECTIONPANEL = "dataChartSelectionPanel";
	public static final String K_CSS_ID_DATACHARTDATELABEL = "dataChartDateLabel";
	public static final String K_CSS_ID_TRACKDATAENTRY = "trackDataPanel";
	public static final String K_CSS_ID_DELETEDATABUTTON = "deleteSelectedData";
	/** some more constants for all the css classes we are using in the code */ 
	public static final String K_CSS_CLASS_TABLEHEADER = "table-header";
	public static final String K_CSS_CLASS_SELECTDATEBUTTON = "select-date-button";
	public static final String K_CSS_CLASS_VARIABLETABLE = "variable-table";
	public static final String K_CSS_CLASS_VARIABLEROW = "variable-row";
	public static final String K_CSS_CLASS_VARIABLESCHART = "values-chart";
	
	/** some more constants for images and ting */
	public static final String K_IMG_TIMESELECT = "images/time-select.png";
	
	/** and the instance to use across the application */
	public static final EmoTrackConstants Instance = GWT.create(EmoTrackConstants.class);
		
	@DefaultStringValue ("Emotion Tracker")
	String emotionTracker();
	
	@DefaultStringValue ("Track Emotions / Events")
	String trackEmotionsEvents();
	
	@DefaultStringValue ("Track Values")
	String trackValues();

	@DefaultStringValue("now") 
	String timeNow();
	
	@DefaultStringValue("1hr ago") 
	String timeOneHour();
	
	@DefaultStringValue("2hrs ago") 
	String timeTwoHour();
	
	@DefaultStringValue("this morning")
	String timeThisMorning();
	
	@DefaultStringValue("this afternoon")
	String timeThisAfternoon();
	
	@DefaultStringValue("this evening")
	String timeThisEvening();
	
	@DefaultStringValue("yesterday")
	String timeYesterday();
	
	@DefaultStringValue("other") 
	String timeOther();

	@DefaultStringValue("Enter specific event...") 
	String eventEntry();

	@DefaultStringValue("Enter new value to track...") 
	String variableEntry();

	@DefaultStringValue("Log Values") 
	String logValues();

	@DefaultStringValue("Log Event") 
	String logEvent();

	@DefaultStringValue("Add") 
	String addVariable();

	@DefaultStringValue("Current values to track...")
	String currentVariablesToTrack();

	@DefaultStringValue("Tracked Values")
	String dataChartTitle();

	@DefaultStringValue("Delete data at this date")
	String deleteSelectionButton();

	@DefaultStringValue("Events")
	String events();
	
	@DefaultStringValue("Date")
	String date();
	
	@DefaultStringValue("Latest values")
	String latestValues();

	@DefaultStringValue("Logged in as:")
	String loginDescription();

	@DefaultStringValue("Please sign in to your Google Account to access the application.")
	String notLoggedIn();
	
	@DefaultStringValue("Sign In")
	String loginSignIn();
	
	@DefaultStringValue("Sign Out")
	String loginSignOut();
}

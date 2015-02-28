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
	public static final String K_CSS_ID_APPPLACEHOLDERVALUEENTRY = "appValueEntry";
	public static final String K_CSS_ID_APPPLACEHOLDERDISPLAY = "appDataDisplay";
	public static final String K_CSS_ID_TIMESELECTIMAGE = "timeSelectImage";
	public static final String K_CSS_ID_VALUEENTRY = "valueEntry";
	public static final String K_CSS_ID_EVENTTEXTBOX = "eventEntry";
	public static final String K_CSS_ID_VARIABLETABLEPANEL = "variableTablePanel";
	public static final String K_CSS_ID_ADDPANEL = "variableAddPanel";
	public static final String K_CSS_ID_LOGBUTTON = "variableLogButton";
	public static final String K_CSS_ID_ADDBUTTON = "variableAddButton";
	public static final String K_CSS_ID_VARIABLETEXTBOX = "variableEntry";
	public static final String K_CSS_ID_VARIABLESCHART = "variablesChart";
	public static final String K_CSS_ID_DATACHART = "dataChart";
	public static final String K_CSS_ID_SLEEPCHART = "sleepChart";
	public static final String K_CSS_ID_DATACHARTPANEL = "dataChartPanel";
	public static final String K_CSS_ID_DATACHARTDATAPANEL = "dataChartDataPanel";
	public static final String K_CSS_ID_DATACHARTSELECTIONPANEL = "dataChartSelectionPanel";
	public static final String K_CSS_ID_DATACHARTDATELABEL = "dataChartDateLabel";
	public static final String K_CSS_ID_TRACKDATAENTRY = "trackDataPanel";
	public static final String K_CSS_ID_DELETEDATABUTTON = "deleteSelectedData";
	public static final String K_CSS_ID_DATESELECTED = "selectedDate";
	public static final String K_CSS_ID_TIMESELECTED = "selectedTime";
	public static final String K_CSS_ID_DATACHARTDATATEXT = "dataChartDataText";
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
	
	@DefaultStringValue ("Time and date")
	String trackValues();

	@DefaultStringValue("today") 
	String timeToday();
	
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

	@DefaultStringValue("Enter specific event details to record for the selected date...") 
	String eventEntry();

	@DefaultStringValue("Enter new emotion to track...") 
	String variableEntry();

	@DefaultStringValue("Log Current Emotions") 
	String logValues();

	@DefaultStringValue("Log Current Sleep Times") 
	String logSleep();

	@DefaultStringValue("Log Current Activity Level") 
	String logActivity();
	
	@DefaultStringValue("at") 
	String at();

	@DefaultStringValue("Log Event") 
	String logEvent();

	@DefaultStringValue("Add") 
	String addVariable();

	@DefaultStringValue("Current emotions to track...")
	String currentVariablesToTrack();

	@DefaultStringValue("Emotion Log")
	String dataChartTitle();
	
	@DefaultStringValue("Sleep Log")
	String sleepChartTitle();

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

	@DefaultStringValue("Please sign in to your Google Account...")
	String notLoggedIn();
	
	@DefaultStringValue("Sign In")
	String loginSignIn();
	
	@DefaultStringValue("Sign Out")
	String loginSignOut();
	
	@DefaultStringValue("To start; <Add> new emotions to track...")
	String addNewValuesToTrack();

	@DefaultStringValue("EmoTrack")
	String alertTitle();

	@DefaultStringValue("Click a point on a graph to select / delete...")
	String selectValue();

	@DefaultStringValue("Emotions")
	String emotions();
	
	@DefaultStringValue("Activity")
	String activity();
	
	@DefaultStringValue("Sleep")
	String sleep();

	@DefaultStringValue("Successfully recorded ")
	String recorded();

	@DefaultStringValue("Time asleep")
	String timeAsleep();
	
	@DefaultStringValue("of which")
	String ofWhich();
	
	@DefaultStringValue("was deep sleep")
	String wasDeepSleep();

	@DefaultStringValue("Asleep")
	String sleeping();

	@DefaultStringValue("Deep Sleep")
	String deepSleep();

	@DefaultStringValue("hours")
	String hours();
	
	@DefaultStringValue("minutes")
	String minutes();

	@DefaultStringValue("Awake")
	String awake();

	@DefaultStringValue(" of ")
	String of();

	@DefaultStringValue("As often as you can, log values for each negative emotion tracking.")
	String emotionTrackExplan();
	
	@DefaultStringValue("Each morning, log the amount of sleep you got the previous night.")
	String sleepTrackExplan();
	
	@DefaultStringValue("Each evening, log the amount of activity you achieved that day.")
	String activityTrackExplan();
	
	@DefaultStringValue("Log significant events as they happen.")
	String eventTrackExplan();

	@DefaultStringValue("Add a new negative emotion to track")
	String tipAddEmotion();

	@DefaultStringValue("Log the entered emotion values for the currently selected date")
	String tipLogEmotions();

	@DefaultStringValue("Log the entered event description for the currently selected date")
	String tipLogEvent();

	@DefaultStringValue("Log the entered sleep values for the currently selected date")
	String tipLogSleep();
	
	@DefaultStringValue("Log the entered activity level for the currently selected date")
	String tipLogActivity();

	@DefaultStringValue("Send the entered data to the site administrator")
	String tipSendEmail();

	@DefaultStringValue("Show data for the previous month")
	String tipPreviousMonth();

	@DefaultStringValue("Show data for the next month")
	String tipNextMonth();

	@DefaultStringValue("Delete the tracked data for the selected date, click an item on the graph to select.")
	String tipDeleteGraphSelection();

	@DefaultStringValue("Points")
	String activityPoints();
	
	@DefaultStringValue("Steps")
	String activitySteps();

	@DefaultStringValue("Select the type of activity to track")
	String tipActivityText();
	
	@DefaultStringValue("Enter the level of activity to track for the selected type")
	String tipActivityList();

	@DefaultStringValue("The data is now selected to <copy> and <paste> into any spreadsheet application, eg Microsoft Excel.")
	String nowCopyAndPaste();

	@DefaultStringValue("Export / Delete data")
	String exportData();

	@DefaultStringValue("Delete the entire row of data from the server.")
	String tipDeleteRowButton();
}

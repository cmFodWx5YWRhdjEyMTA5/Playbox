package uk.co.darkerwaters.client.entry;

import java.util.Date;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.tracks.TrackPointData;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;

public class ActivityTab extends ValueEntryTab {
	
	private FlowPanel mainPanel = new FlowPanel();
	private TextBox activityText = new TextBox();
	private ListBox activityList;
	private Button logEventButton;
	
	private boolean isInitialised = false;
	
	public ActivityTab(ValueEntryListener listener, TrackPointServiceAsync trackPointService, final DateSelectTab dateSelectPanel) {
		super(listener, trackPointService);
	    
	    mainPanel.add(FlatUI.createHeader(6, EmoTrackConstants.Instance.activityTrackExplan()));
	    this.activityList = FlatUI.createSelect("activityList", null);
		
		// create the entry controls
		mainPanel.add(FlatUI.createLabel(EmoTrackConstants.Instance.activity(), "activityLabel"));
		mainPanel.add(this.activityText);
		mainPanel.add(this.activityList);
		
		FlatUI.makeEntryText(this.activityText, "activityText", null);
		
		FlatUI.makeTooltip(this.activityList, EmoTrackConstants.Instance.tipActivityList());
		FlatUI.makeTooltip(this.activityList, EmoTrackConstants.Instance.tipActivityText());
		
		// populate the list
		for (ActivityTypes type : ActivityTypes.values()) {
			this.activityList.addItem(type.title);
		}
		
		this.activityText.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				SleepTab.restrictEventToNumber(event, activityText, "activity");
				updateEntryValue();
			}
		});
		
		mainPanel.getElement().setId("logActivityPanel");
	    
	    // create the log values button
	    Button button = createLogEventButton(dateSelectPanel);
	    FlowPanel logValPanel = createLogValuesButtonPanel(button, dateSelectPanel, false);
		mainPanel.add(logValPanel);
		
		mainPanel.add(createResultsPanel());
		
		updateEntryValue();
	}

	@Override
	public Panel getContent() {
		return this.mainPanel;
	}
	
	@Override
	public void setActiveItem(boolean isActive) {
		super.setActiveItem(isActive);
		if (false == this.isInitialised && isActive) {
			// activated for the first time
			FlatUI.configureSelect(this.activityList);
			this.isInitialised = true;
		}
	}

	private Button createLogEventButton(final DateSelectTab dateSelectPanel) {
		// create the log event button
		this.logEventButton = new Button(EmoTrackConstants.Instance.logActivity());
		FlatUI.makeButton(logEventButton, "logActivityButton", EmoTrackConstants.Instance.tipLogActivity());
		logEventButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// and log the event
				logEvent(dateSelectPanel.getSelectedDate(), updateEntryValue());
			}
		});
		return logEventButton;
	}
	
	protected int updateEntryValue() {
		int value = 0;
		try {
			value = Integer.parseInt(this.activityText.getText());
		}
		catch (Exception e) {
			EmoTrack.LOG.severe("Activity is not a number: " + e.getMessage());
		}
		return value;
	}

	protected void logEvent(Date selectedDate, int value) {
		if (value <= 0) {
			FlatUI.createErrorMessage(EmoTrackMessages.Instance.invalidAmount(), this.activityText);
			return;
		}
		String activity = this.activityList.getItemText(this.activityList.getSelectedIndex());
		TrackPointData point = new TrackPointData(DateSelectTab.limitDateToDay(selectedDate));
		point.addValue(TrackPointData.ACTIVITYKEY + activity, value);
		StringBuilder description = new StringBuilder(EmoTrackConstants.Instance.recorded());
		description.append(value);
		description.append(" ");
		description.append(activity);
		description.append(" ");
		description.append(EmoTrackConstants.Instance.at());
		// append the time
		description.append(getDateString());
		final String successString = description.toString();
		if (false == checkLoginStatus()) {
			return;
		}
		// send this data to the service now
		trackPointService.addTrackPoint(point, new AsyncCallback<TrackPointData>() {
			@Override
			public void onFailure(Throwable error) {
				listener.handleError(error);
			}
			@Override
			public void onSuccess(TrackPointData result) {
				// inform the graph / view of this new point
				ActivityTab.this.listener.updateTrackEntry(result);
				// create the string for this data we just added
				alertResult(successString);
			}
		});	
	}
}

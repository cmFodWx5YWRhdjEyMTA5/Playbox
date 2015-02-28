package uk.co.darkerwaters.client.entry;

import java.util.Date;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.tracks.TrackPointData;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;

public class EventsTab extends ValueEntryTab {
	
	private FlowPanel mainPanel = new FlowPanel();
	
	public EventsTab(ValueEntryListener listener, TrackPointServiceAsync trackPointService, final DateSelectTab dateSelectPanel) {
		super(listener, trackPointService);
	   
		// create the log event controls
		mainPanel.getElement().setId("logEventPanel");
	    
	    mainPanel.add(FlatUI.createHeader(6, EmoTrackConstants.Instance.eventTrackExplan()));
	    
	    TextBox eventTextBox = new TextBox();
	    FlatUI.makeEntryText(eventTextBox, EmoTrackConstants.K_CSS_ID_EVENTTEXTBOX, EmoTrackConstants.Instance.eventEntry());
	    mainPanel.add(eventTextBox);
	    
	    // create the log values button
	    Button button = createLogEventButton(eventTextBox, dateSelectPanel);
	    FlowPanel logValPanel = createLogValuesButtonPanel(button, dateSelectPanel, false);
		mainPanel.add(logValPanel);
		
		mainPanel.add(createResultsPanel());
	}

	@Override
	public Panel getContent() {
		return this.mainPanel;
	}

	private Button createLogEventButton(final TextBox eventTextBox, final DateSelectTab dateSelectPanel) {
		// create the log event button
		Button logEventButton = new Button(EmoTrackConstants.Instance.logEvent());
		FlatUI.makeButton(logEventButton, "logEventButton", EmoTrackConstants.Instance.tipLogEvent());
		logEventButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// log this event
				String eventString = eventTextBox.getText();
				// The string must be between 1 and 50 chars that are numbers, letters, or dots.
				if (!eventString.matches("^[0-9a-zA-Z\\. ]{1,50}$")) {
					EmoTrack.alertWidget(EmoTrackConstants.Instance.alertTitle(), EmoTrackMessages.Instance.invalidEventTitle(eventString));
					eventTextBox.selectAll();
					return;
				}
				// reset the text box
				eventTextBox.setText("");
				// and log the event
				logEvent(dateSelectPanel.getSelectedDate(), eventString);
			}
		});
		return logEventButton;
	}
	
	protected void logEvent(Date selectedDate, String event) {
		if (false == checkLoginStatus()) {
			return;
		}
		StringBuilder description = new StringBuilder(EmoTrackConstants.Instance.recorded());
		description.append(event);
		// append the time
		description.append(getDateString());
		final String successString = description.toString();
		// create a point to contain this data
		TrackPointData point = new TrackPointData(DateSelectTab.limitDateToDay(selectedDate), event);
		// send this data to the service now
		trackPointService.addTrackPoint(point, new AsyncCallback<TrackPointData>() {
			@Override
			public void onFailure(Throwable error) {
				listener.handleError(error);
			}
			@Override
			public void onSuccess(TrackPointData result) {
				// inform the graph / view of this new point
				EventsTab.this.listener.updateTrackEntry(result);
				// create the string for this data we just added
				alertResult(successString);
			}
		});	
	}
}

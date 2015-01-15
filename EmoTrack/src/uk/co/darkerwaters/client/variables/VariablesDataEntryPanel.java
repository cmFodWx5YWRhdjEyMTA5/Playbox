package uk.co.darkerwaters.client.variables;

import java.util.Date;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.WatermarkedTextBox;
import uk.co.darkerwaters.client.tracks.TrackPointData;
import uk.co.darkerwaters.client.tracks.TrackPointService;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DatePicker;

public class VariablesDataEntryPanel extends DecoratorPanel {
	
	private final TrackPointServiceAsync trackPointService = GWT.create(TrackPointService.class);
	private final EmoTrackConstants constants = EmoTrackConstants.Instance;
	
	private String[] variableTitles = null;
	private int[] variableValues = null;
	
	public interface VariablesDataEntryListener {
		void updateVariableValues(String[] titles, int[] values);
		void updateTrackEntry(TrackPointData newPoint);
	}
	
	private final VariablesDataEntryListener listener;
		
	public VariablesDataEntryPanel(VariablesDataEntryListener listener) {
		// remember the listener to inform of data changes
		this.listener = listener;
	    this.getElement().setId(EmoTrackConstants.K_CSS_ID_TRACKDATAENTRY);
		// create the panel to contain the entry values
	    FlexTable layout = new FlexTable();
	    layout.setCellSpacing(EmoTrackConstants.K_TABLECELLSPACING);
	    layout.setWidth(EmoTrackConstants.K_100PERCENT);
	    FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

	    // Add a title to the form
	    layout.setHTML(0, 0, constants.trackEmotionsEvents());
	    // span the title over all three columns we are going to create
	    cellFormatter.setColSpan(0, 0, 3);
	    cellFormatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);

	    // add the time selection controls to row one
	    Image timeSelectImage = new Image(EmoTrackConstants.K_IMG_TIMESELECT);
	    timeSelectImage.getElement().setId(EmoTrackConstants.K_CSS_ID_TIMESELECTIMAGE);
	    DatePicker logDatePicker = createLogDatePicker();
	    ListBox logDateList = createLogDateList(logDatePicker);
	    layout.setWidget(1, 0, timeSelectImage);
	    layout.setWidget(1, 1, logDateList);
	    layout.setWidget(1, 2, logDatePicker);
	    
	    // add the track event controls to the next row
	    TextBox eventTextBox = new WatermarkedTextBox("", constants.eventEntry());
	    eventTextBox.getElement().setId(EmoTrackConstants.K_CSS_ID_EVENTTEXTBOX);
	    layout.setWidget(2, 0, eventTextBox);
	    cellFormatter.setColSpan(2, 0, 2);
	    layout.setWidget(2, 1, createLogEventButton());

	    // create the variables panel
	    final VariablesListPanel variablesPanel = createVariablesPanel(logDateList, logDatePicker);
	    // add the variables panel to form in a disclosure panel
	    DisclosurePanel trackValueOptions = new DisclosurePanel(constants.trackValues());
	    trackValueOptions.setWidth(EmoTrackConstants.K_100PERCENT);
	    trackValueOptions.setAnimationEnabled(true);
	    trackValueOptions.ensureDebugId("cwVariablesPanel");
	    trackValueOptions.setContent(variablesPanel);
	    trackValueOptions.addOpenHandler(new OpenHandler<DisclosurePanel>() {
			@Override
			public void onOpen(OpenEvent<DisclosurePanel> event) {
				// when open, inform the variables panel to show all its rows
				variablesPanel.showRows();
			}});
	    layout.setWidget(3, 0, trackValueOptions);
	    // span the advanced options over all three columns
	    cellFormatter.setColSpan(3, 0, 3);

	    // Wrap the contents in this DecoratorPanel
	    this.setWidget(layout);
	}
	
	private VariablesListPanel createVariablesPanel(final ListBox logDateList, final DatePicker logDatePicker) {
		return new VariablesListPanel(new VariablesListPanel.VariablesPanelListener() {
			@Override
			public void updateVariableValues(String[] titles, int[] values) {
				// store these values for when we want to log them
				setVariableData(titles, values);				
			}
			@Override
			public void handleError(Throwable error) {
				VariablesDataEntryPanel.this.handleError(error);
			}
			@Override
			public void logVariables() {
				VariablesDataEntryPanel.this.logCurrentVariables(logDateList, logDatePicker);
			}
		});
	}

	protected void logCurrentVariables(ListBox logDateList, DatePicker logDatePicker) {
		int selectedIndex = logDateList.getSelectedIndex();
		Date selectedDate = new Date();
		if (selectedIndex == LogDates.other.ordinal()) {
			// user has selected "other" so get the date from the date picker
			selectedDate = logDatePicker.getValue();
		}
		else if (selectedIndex != -1) {
			// get the date this data should be logged for
			selectedDate = LogDates.values()[selectedIndex].getDate();
		}
		//send this data to the server
		final TrackPointData point = new TrackPointData(selectedDate);
		synchronized (this) {
			// add all the values to the variable
			if (null != this.variableTitles && null != this.variableValues) {
				for (int i = 0; i < this.variableTitles.length && i < this.variableValues.length; ++i) {
					point.addValue(this.variableTitles[i], this.variableValues[i]);
				}
			}
		}
		// send this data to the service now
		trackPointService.addTrackPoint(point, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable error) {
				handleError(error);
			}
			@Override
			public void onSuccess(Void result) {
				// inform the graph / view of this new point
				VariablesDataEntryPanel.this.listener.updateTrackEntry(point);
			}
		});		
	}

	protected void setVariableData(String[] titles, int[] values) {
		synchronized (this) {
			this.variableTitles = titles;
			this.variableValues = values;
		}
		// inform the listener of this
		this.listener.updateVariableValues(titles, values);
	}

	private Button createLogEventButton() {
		// create the log event button
		Button logEventButton = new Button(constants.logEvent());
		return logEventButton;
	}

	private ListBox createLogDateList(final DatePicker logDatePicker) {
		// add all the options to the drop-down
		final ListBox logDateList = new ListBox();
		for (LogDates date : LogDates.values()) {
			logDateList.addItem(date.title);
		}
		logDateList.setSelectedIndex(0);
		logDateList.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				// added to only show the date picker when "other" is selected
				if (logDateList.getSelectedIndex() == LogDates.other.ordinal()) {
					// user has selected "other"
					logDatePicker.setVisible(true);
				}
				else {
					// user has selected another specific time
					logDatePicker.setVisible(false);
				}
			}
		});
		return logDateList;
	}
	
	private DatePicker createLogDatePicker() {
		// Set the default value on the date picker
	    DatePicker logDatePicker = new DatePicker();
	    logDatePicker.setValue(new Date(), true);
	    logDatePicker.setVisible(false);		
		return logDatePicker;
	}

	private void handleError(Throwable error) {
		Window.alert(error.getMessage());
	}
}

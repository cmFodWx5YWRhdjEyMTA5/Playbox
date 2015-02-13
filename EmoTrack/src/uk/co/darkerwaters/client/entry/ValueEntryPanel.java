package uk.co.darkerwaters.client.entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.FlatUI;
import uk.co.darkerwaters.client.tracks.TrackPointData;
import uk.co.darkerwaters.client.tracks.TrackPointService;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import uk.co.darkerwaters.client.variables.LogDates;
import uk.co.darkerwaters.client.variables.VariablesService;
import uk.co.darkerwaters.client.variables.VariablesServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DatePicker;

public class ValueEntryPanel extends FlowPanel {
	
	private final TrackPointServiceAsync trackPointService = GWT.create(TrackPointService.class);
	
	private final VariablesServiceAsync variablesService = GWT.create(VariablesService.class);
	
	private final List<ValueSelectPanel> valueSelectPanels;

	private TextBox newVariableTextBox;
	
	private final FlowPanel valueSelectPanel;
	
	public interface ValueEntryListener extends EmoTrackListener {
		void updateVariableValues(String[] titles, int[] values);
		void updateTrackEntry(TrackPointData newPoint);
		boolean checkLoginStatus();
	}
	
	private final ValueEntryListener listener;

	private Widget valueSelectWaitLabel;
	

	public ValueEntryPanel(ValueEntryListener listener) {
		this.listener = listener;
		this.getElement().setId(EmoTrackConstants.K_CSS_ID_VALUEENTRY);
	    
	    Label dateSelectLabel = new Label(EmoTrackMessages.Instance.date(LogDates.now.getDate()));
	    dateSelectLabel.getElement().setId(EmoTrackConstants.K_CSS_ID_DATESELECTED);
	    Label timeSelectLabel = new Label(EmoTrackMessages.Instance.time(LogDates.now.getDate()));
	    timeSelectLabel.getElement().setId(EmoTrackConstants.K_CSS_ID_TIMESELECTED);
		
		// add the entry controls
		VerticalPanel leftPanel = new VerticalPanel();
		leftPanel.getElement().setId("entryLeftPanel");
	    Image timeSelectImage = new Image(EmoTrackConstants.K_IMG_TIMESELECT);
	    timeSelectImage.getElement().setId(EmoTrackConstants.K_CSS_ID_TIMESELECTIMAGE);
	    final DatePicker logDatePicker = createLogDatePicker(dateSelectLabel, timeSelectLabel);
	    logDatePicker.setWidth("200px");
	    
	    final ListBox logDateList = createLogDateList(logDatePicker, dateSelectLabel, timeSelectLabel);
	    FlowPanel timePanel = new FlowPanel();
	    timePanel.add(timeSelectImage);
	    FlowPanel timeSelectPanel = new FlowPanel();
	    timeSelectPanel.getElement().setId("timeDateSelectPanel");
	    HeadingElement headingElement = Document.get().createHElement(4);
	    headingElement.setInnerText(EmoTrackConstants.Instance.trackValues());
	    timeSelectPanel.getElement().appendChild(headingElement);
	    timeSelectPanel.add(logDateList);
	    timeSelectPanel.add(logDatePicker);
	    FlowPanel labelPanel = new FlowPanel();
	    labelPanel.add(dateSelectLabel);
	    labelPanel.add(timeSelectLabel);
	    timeSelectPanel.add(labelPanel);
	    timePanel.add(timeSelectPanel);
	    leftPanel.add(timePanel);

		// create the log event controls
		FlowPanel logPanel = new FlowPanel();
		logPanel.getElement().setId("logEventPanel");
	    TextBox eventTextBox = new TextBox();
	    FlatUI.makeEntryText(eventTextBox, EmoTrackConstants.K_CSS_ID_EVENTTEXTBOX, EmoTrackConstants.Instance.eventEntry());
	    logPanel.add(createLogEventButton(eventTextBox, logDateList, logDatePicker));
	    logPanel.add(eventTextBox);
		
	    leftPanel.add(logPanel);
	    RootPanel.get("timeValueEntry").add(leftPanel);
	    
	    VerticalPanel variablePanel = new VerticalPanel();
	    variablePanel.getElement().setId("variableSelectPanel");
	    FlowPanel newDataPanel = new FlowPanel();
	    this.newVariableTextBox = new TextBox();
	    FlatUI.makeEntryText(newVariableTextBox, EmoTrackConstants.K_CSS_ID_VARIABLETEXTBOX, EmoTrackConstants.Instance.variableEntry());
		// create the add button to add things to the flex table
		Button addVariableButton = new Button(EmoTrackConstants.Instance.addVariable());
		FlatUI.makeButton(addVariableButton, EmoTrackConstants.K_CSS_ID_ADDBUTTON);
		newDataPanel.add(newVariableTextBox);
		newDataPanel.add(addVariableButton);
		variablePanel.add(newDataPanel);
		
		this.valueSelectWaitLabel = new Label(EmoTrackConstants.Instance.addNewValuesToTrack());
		this.valueSelectWaitLabel.addStyleName("info");
		
		// now the panel for the guages
		valueSelectPanel = new FlowPanel();
		valueSelectPanel.addStyleName("entryValue");
		valueSelectPanel.add(this.valueSelectWaitLabel);
		variablePanel.add(valueSelectPanel);
		// create the log values button
		FlowPanel logValPanel = new FlowPanel();
		Button logValuesButton = new Button(EmoTrackConstants.Instance.logValues());
		FlatUI.makeButton(logValuesButton, null);
		logValuesButton.addStyleName("entryValue");
		// setup the button
		logValuesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// log the current data
				logCurrentVariables(getSelectedDate(logDateList, logDatePicker));		
			}
		});
		logValPanel.add(logValuesButton);
		logValPanel.add(new MirrorLabel(dateSelectLabel, "entryValue"));
		logValPanel.add(new MirrorLabel(timeSelectLabel, "entryValue"));
		variablePanel.add(logValPanel);
	    
		RootPanel.get(EmoTrackConstants.K_CSS_ID_APPPLACEHOLDERVALUEENTRY).add(variablePanel);

		// Listen for mouse events on the Add button.
		addVariableButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addVariable();
			}
		});

		// Listen for keyboard events in the input box.
		newVariableTextBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					addVariable();
				}
			}
		});
	    
	    this.valueSelectPanels = new ArrayList<ValueSelectPanel>();
	    loadVariables();
	}

	private Button createLogEventButton(final TextBox eventTextBox, final ListBox logDateList, final DatePicker logDatePicker) {
		// create the log event button
		Button logEventButton = new Button(EmoTrackConstants.Instance.logEvent());
		FlatUI.makeButton(logEventButton, "logEventButton");
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
				logEvent(getSelectedDate(logDateList, logDatePicker), eventString);
			}
		});
		return logEventButton;
	}
	
	protected void logEvent(Date selectedDate, String event) {
		if (false == checkLoginStatus()) {
			return;
		}
		// create a point to contain this data
		final TrackPointData point = new TrackPointData(selectedDate, event);
		// send this data to the service now
		trackPointService.addTrackPoint(point, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable error) {
				listener.handleError(error);
			}
			@Override
			public void onSuccess(Void result) {
				// inform the graph / view of this new point
				ValueEntryPanel.this.listener.updateTrackEntry(point);
			}
		});	
	}

	private Date getSelectedDate(ListBox logDateList, DatePicker logDatePicker) {
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
		return selectedDate;
	}

	private ListBox createLogDateList(final DatePicker logDatePicker, final Label dateSelectLabel, final Label timeSelectLabel) {
		// add all the options to the drop-down
		final ListBox logDateList = new ListBox();
		FlatUI.makeCombo(logDateList, null);
		for (LogDates date : LogDates.values()) {
			logDateList.addItem(date.title);
		}
		logDateList.setSelectedIndex(0);
		logDateList.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				// added to only show the date picker when "other" is selected
				LogDates selected = LogDates.values()[logDateList.getSelectedIndex()];
				if (selected == LogDates.other) {
					// user has selected "other"
					logDatePicker.setVisible(true);
					dateSelectLabel.setVisible(false);
					timeSelectLabel.setVisible(false);
				}
				else {
					// user has selected another specific time
					logDatePicker.setVisible(false);
					timeSelectLabel.setVisible(true);
					dateSelectLabel.setVisible(true);
					setDateLabels(dateSelectLabel, timeSelectLabel, selected.getDate());
				}
			}
		});
		return logDateList;
	}
	
	private void setDateLabels(final Label dateSelectLabel, final Label timeSelectLabel, Date selected) {
		dateSelectLabel.setText(EmoTrackMessages.Instance.date(selected));
		timeSelectLabel.setText(EmoTrackMessages.Instance.time(selected));
		MirrorLabel.update();
	}
	
	private DatePicker createLogDatePicker(final Label dateSelectLabel, final Label timeSelectLabel) {
		// Set the default value on the date picker
	    DatePicker logDatePicker = new DatePicker();
	    logDatePicker.setValue(new Date(), true);
	    logDatePicker.setVisible(false);
	    logDatePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				setDateLabels(dateSelectLabel, timeSelectLabel, event.getValue());
			}
		});
		return logDatePicker;
	}

	private void addVariable() {
		// get the name all lower case...
		String variableName = newVariableTextBox.getText().toLowerCase().trim();
		if (variableName.length() > 1) {
			// but let us camel case it for niceness
			variableName = variableName.substring(0, 1).toUpperCase() + variableName.substring(1, variableName.length());
		}
		newVariableTextBox.setFocus(true);

		// The variable name must be between 1 and 20 chars that are numbers, letters,
		// or dots.
		if (!variableName.matches("^[0-9a-zA-Z\\.]{1,20}$")) {
			EmoTrack.alertWidget(EmoTrackConstants.Instance.alertTitle(), EmoTrackMessages.Instance.invalidVariableName(variableName));
			newVariableTextBox.selectAll();
			return;
	    }

		newVariableTextBox.setText("");

		// Don't add the variable if it's already in the table.
		if (isVariableExist(variableName)) {
			// don't allow this addition
			EmoTrack.alertWidget(EmoTrackConstants.Instance.alertTitle(), EmoTrackMessages.Instance.usedVariableName(variableName));
			newVariableTextBox.selectAll();
		}
		else {
			// add the new variable
			addVariable(variableName);
		}
	}

	private boolean isVariableExist(String variableName) {
		boolean isFound = false;
		for (ValueSelectPanel panel : this.valueSelectPanels) {
			// check for the name, ignoring case
			if (null != panel && 0 == panel.getVariableTitle().compareToIgnoreCase(variableName)) {
				// this is there
				isFound = true;
				break;
			}
		}
		return isFound;
	}

	private void addVariable(final String variableName) {
		if (false == checkLoginStatus()) {
			return;
		}
		variablesService.addVariable(variableName, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				listener.handleError(error);
			}

			public void onSuccess(Void ignore) {
				displayVariable(variableName);
			}
		});
	}

	private void loadVariables() {
		variablesService.getVariables(new AsyncCallback<String[]>() {
			public void onFailure(Throwable error) {
				listener.handleError(error);
				listener.loadingComplete();
			}

			public void onSuccess(String[] variables) {
				displayVariables(variables);
				listener.loadingComplete();
			}
		});
	}

	protected void displayVariables(String[] variables) {
		for (String variable : variables) {
			displayVariable(variable);
		}
	}

	private void displayVariable(String variable) {
		// remove the wait label if this is the first
		if (this.valueSelectPanels.isEmpty()) {
			valueSelectPanel.remove(this.valueSelectWaitLabel);
		}
		// create the gauges for the variables to track
	    ValueSelectPanel selectPanel = new ValueSelectPanel(this, variable);
	    valueSelectPanel.add(selectPanel);
	    this.valueSelectPanels.add(selectPanel);
	}

	private void undisplayVariable(String variable) {
		ValueSelectPanel toRemove = null;
		for (ValueSelectPanel panel : this.valueSelectPanels) {
			if (panel.getVariableTitle().equals(variable)) {
				toRemove = panel;
				break;
			}
		}
		if (null != toRemove) {
			valueSelectPanel.remove(toRemove);
			this.valueSelectPanels.remove(toRemove);
			if (this.valueSelectPanels.isEmpty()) {
				// add the wait label
				valueSelectPanel.add(this.valueSelectWaitLabel);
			}
		}
	}

	void removeVariable(final String variableName) {
		if (false == checkLoginStatus()) {
			return;
		}
		variablesService.removeVariable(variableName, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				listener.handleError(error);
			}

			public void onSuccess(Void ignore) {
				undisplayVariable(variableName);
			}
		});
	}

	protected void logCurrentVariables(Date selectedDate) {
		//send this data to the server
		final TrackPointData point = new TrackPointData(selectedDate);
		for (ValueSelectPanel panel : this.valueSelectPanels) {
			point.addValue(panel.getVariableTitle(), panel.getVariableValue());
		}
		if (false == checkLoginStatus()) {
			return;
		}
		// send this data to the service now
		trackPointService.addTrackPoint(point, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable error) {
				listener.handleError(error);
			}
			@Override
			public void onSuccess(Void result) {
				// inform the graph / view of this new point
				ValueEntryPanel.this.listener.updateTrackEntry(point);
				EmoTrack.alertWidget(EmoTrackConstants.Instance.alertTitle(), EmoTrackConstants.Instance.valuesLogged());
			}
		});		
	}

	private boolean checkLoginStatus() {
		return listener.checkLoginStatus();
	}
	
}

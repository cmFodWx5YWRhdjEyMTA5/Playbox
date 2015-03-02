package uk.co.darkerwaters.client.entry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import uk.co.darkerwaters.client.variables.VariablesService;
import uk.co.darkerwaters.client.variables.VariablesServiceAsync;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class EmotionsTab extends ValueEntryTab {
	
	private final VariablesServiceAsync variablesService = GWT.create(VariablesService.class);
	
	private VerticalPanel mainPanel = new VerticalPanel();

	private TextBox newVariableTextBox;

	private Widget valueSelectWaitLabel;
	
	private final FlowPanel valueSelectPanel;
	
	private final List<EmotionChartSelectPanel> valueSelectPanels;
	
	public EmotionsTab(ValueEntryListener listener, TrackPointServiceAsync trackPointService, final DateSelectTab dateSelectPanel) {
		super(listener, trackPointService);
	    mainPanel.getElement().setId("variableSelectPanel");
	    
	    mainPanel.add(FlatUI.createHeader(6, EmoTrackConstants.Instance.emotionTrackExplan()));
	    
	    FlowPanel newDataPanel = new FlowPanel();
	    this.newVariableTextBox = new TextBox();
	    FlatUI.makeEntryText(newVariableTextBox, EmoTrackConstants.K_CSS_ID_VARIABLETEXTBOX, EmoTrackConstants.Instance.variableEntry());
		// create the add button to add things to the flex table
		Button addVariableButton = new Button(EmoTrackConstants.Instance.addVariable());
		FlatUI.makeButton(addVariableButton, EmoTrackConstants.K_CSS_ID_ADDBUTTON, EmoTrackConstants.Instance.tipAddEmotion());
		newDataPanel.add(newVariableTextBox);
		newDataPanel.add(addVariableButton);
		mainPanel.add(newDataPanel);
		
		this.valueSelectWaitLabel = new Label(EmoTrackConstants.Instance.addNewValuesToTrack());
		this.valueSelectWaitLabel.addStyleName("info");
		
		// now the panel for the guages
		valueSelectPanel = new FlowPanel();
		valueSelectPanel.addStyleName("entryValue");
		valueSelectPanel.add(this.valueSelectWaitLabel);
		mainPanel.add(valueSelectPanel);
		// create the log values button
		Button logValuesButton = new Button(EmoTrackConstants.Instance.logValues());
		FlatUI.makeButton(logValuesButton, null, EmoTrackConstants.Instance.tipLogEmotions());
		logValuesButton.addStyleName("entryValue");
		// setup the button
		logValuesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// log the current data
				logCurrentVariables(dateSelectPanel.getSelectedDate());		
			}
		});
		FlowPanel logValPanel = createLogValuesButtonPanel(logValuesButton, dateSelectPanel, true);
		mainPanel.add(logValPanel);
		
		// setup the results label
		mainPanel.add(createResultsPanel());
		
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
	    
	    this.valueSelectPanels = new ArrayList<EmotionChartSelectPanel>();
	    loadVariables();
	}

	@Override
	public Panel getContent() {
		return this.mainPanel;
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
			FlatUI.createErrorMessage(EmoTrackMessages.Instance.invalidVariableName(variableName), this.newVariableTextBox);
			newVariableTextBox.selectAll();
			return;
	    }

		newVariableTextBox.setText("");

		// Don't add the variable if it's already in the table.
		if (isVariableExist(variableName)) {
			// don't allow this addition
			FlatUI.createErrorMessage(EmoTrackMessages.Instance.usedVariableName(variableName), this.newVariableTextBox);
			newVariableTextBox.selectAll();
		}
		else {
			// add the new variable
			addVariable(variableName);
		}
	}

	private boolean isVariableExist(String variableName) {
		boolean isFound = false;
		for (EmotionChartSelectPanel panel : this.valueSelectPanels) {
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
	    EmotionChartSelectPanel selectPanel = new EmotionChartSelectPanel(this, variable);
	    valueSelectPanel.add(selectPanel);
	    this.valueSelectPanels.add(selectPanel);
	}

	private void undisplayVariable(String variable) {
		EmotionChartSelectPanel toRemove = null;
		for (EmotionChartSelectPanel panel : this.valueSelectPanels) {
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
		TrackPointData point = new TrackPointData(selectedDate);
		StringBuilder description = new StringBuilder(EmoTrackConstants.Instance.recorded());
		for (EmotionChartSelectPanel panel : this.valueSelectPanels) {
			point.addValue(panel.getVariableTitle(), panel.getVariableValue());
			description.append(panel.getDescription());
			description.append("; ");
		}
		if (description.length() > 2) {
			// remove the last comma
			description.deleteCharAt(description.length() - 2);
		}
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
				EmotionsTab.this.listener.updateTrackEntry(result);
				// create the string for this data we just added
				alertResult(successString);
			}
		});		
	}
}

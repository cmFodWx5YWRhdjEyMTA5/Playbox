package uk.co.darkerwaters.client.variables;

import java.util.ArrayList;
import java.util.logging.Level;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.variables.slider.VariableValueSlider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.kiouri.sliderbar.client.event.BarValueChangedEvent;
import com.kiouri.sliderbar.client.event.BarValueChangedHandler;

public class VariablesPanel extends VerticalPanel {
	
	private final VariablesServiceAsync variablesService = GWT.create(VariablesService.class);

	private HorizontalPanel addPanel = new HorizontalPanel();
	private FlexTable variablesFlexTable = new FlexTable();
	private TextBox newVariableTextBox = new TextBox();
	private Button addVariableButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private ArrayList<String> variables = new ArrayList<String>();
	
	public VariablesPanel(Panel parent) {
		this.addStyleName("variable-table-panel");
		// create the variables table nicely
		variablesFlexTable.setText(0, 0, "Current values to track...");
		// Add styles to elements in the stock list table.
		variablesFlexTable.getRowFormatter().addStyleName(0, "variable-header");
		variablesFlexTable.addStyleName("variable-table");
		// load the variables into the panel
		loadVariables();

		// Assemble Add Stock panel.
		addPanel.add(newVariableTextBox);
		addPanel.add(addVariableButton);
		addPanel.addStyleName("addPanel");

		this.add(variablesFlexTable);
		this.add(addPanel);
		this.add(lastUpdatedLabel);
		
		// Move cursor focus to the input box.
		newVariableTextBox.setFocus(true);

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
	}

	/**
	 * Add stock to FlexTable. Executed when the user clicks the addStockButton
	 * or presses enter in the newSymbolTextBox.
	 */
	private void addVariable() {
		// get the name all lower case...
		String variableName = newVariableTextBox.getText().toLowerCase().trim();
		if (variableName.length() > 1) {
			// but let us camel case it for niceness
			variableName = variableName.substring(0, 1).toUpperCase() + variableName.substring(1, variableName.length());
		}
		newVariableTextBox.setFocus(true);

		// The variable name must be between 1 and 10 chars that are numbers, letters,
		// or dots.
		if (!variableName.matches("^[0-9a-zA-Z\\.]{1,10}$")) {
			Window.alert("'" + variableName + "' is not a valid variable name to track.");
			newVariableTextBox.selectAll();
			return;
		}

		newVariableTextBox.setText("");

		// Don't add the variable if it's already in the table.
		if (isVariableExist(variableName)) {
			// don't allow this addition
			Window.alert("'" + variableName + "' is not a valid variable name to track, you are already tracking this...");
			newVariableTextBox.selectAll();
		}
		else {
			// add the new variable
			addVariable(variableName);
		}
	}

	private void addVariable(final String variableName) {
		variablesService.addVariable(variableName, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}

			public void onSuccess(Void ignore) {
				displayVariable(variableName);
			}
		});
	}

	private void loadVariables() {
		variablesService.getVariables(new AsyncCallback<String[]>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}

			public void onSuccess(String[] variables) {
				displayVariables(variables);
			}
		});
	}

	private void displayVariables(String[] variables) {
		for (String variable : variables) {
			displayVariable(variable);
		}
	}

	private void displayVariable(final String variableName) {
		// Add the variable to the panel
		if (variables.contains(variableName)) {
			// this is already there, ignore this, grr
			EmoTrack.LOG.log(Level.WARNING, "There are duplicate variables of \"" + variableName + "\" in the db");
		}
		else {
			variables.add(variableName);
			VariableRow row = new VariableRow(variableName);
			int rowIndex = this.variablesFlexTable.getRowCount();
			// set the data to show this in a row on the table
			variablesFlexTable.setText(rowIndex, 0, variableName);
			variablesFlexTable.setWidget(rowIndex, 0, row);
			variablesFlexTable.getCellFormatter().addStyleName(rowIndex, 0, "variable-row");
		}
	}
	
	private boolean isVariableExist(String variableName) {
		boolean isFound = false;
		for (String variable : this.variables) {
			// check for the name, ignoring case
			if (null != variable && 0 == variable.compareToIgnoreCase(variableName)) {
				// this is there
				isFound = true;
				break;
			}
		}
		return isFound;
	}
	
	class VariableRow extends VerticalPanel {
		HorizontalPanel textPanel = new HorizontalPanel();
		Button deleteButton = new Button();
		Label variableNameLabel = new Label();
		Label variableValueLabel = new Label();
		VariableValueSlider slider = new VariableValueSlider(10, "100%", true);
		
		public VariableRow(final String variableName) {
			// set our style
			addStyleName("variable-row");
			textPanel.addStyleName("variable-row");
			// set the data on the labels
			deleteButton.setText("x");
			variableNameLabel.setText(variableName);
			variableValueLabel.addStyleName("variable-title");
			setNewValue(slider.getValue());
			variableValueLabel.addStyleName("variable-description");
			// setup the slider
			slider.addStyleName("variable-slider");
			slider.addBarValueChangedHandler(new BarValueChangedHandler() {
				@Override
				public void onBarValueChanged(BarValueChangedEvent event) {
					// the bar value has changed, change the label
					setNewValue(event.getValue());
				}
			});
			// setup the delete button
			deleteButton.addStyleDependentName("remove");
			deleteButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					removeVariable(variableName);
				}
			});
			// add them to the panel
			textPanel.add(deleteButton);
			textPanel.add(variableNameLabel);
			textPanel.add(variableValueLabel);
			// add the label and sliders to the main panel
			this.add(textPanel);
			this.add(slider);
		}

		protected void setNewValue(int value) {
			variableValueLabel.setText("Current value is a number: " + Integer.toString(slider.getValue()));
		}
	}

	private void removeVariable(final String variableName) {
		variablesService.removeVariable(variableName, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}

			public void onSuccess(Void ignore) {
				undisplayVariable(variableName);
			}
		});
	}

	private void undisplayVariable(String variableName) {
		int removedIndex = variables.indexOf(variableName);
		variables.remove(removedIndex);
		variablesFlexTable.removeRow(removedIndex + 1);
	}

	private void handleError(Throwable error) {
		Window.alert(error.getMessage());
	}
}

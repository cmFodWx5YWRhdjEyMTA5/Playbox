package uk.co.darkerwaters.client.variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.WatermarkedTextBox;

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
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class VariablesListPanel extends VerticalPanel {
	private final FlexTable variablesFlexTable;
	private final WatermarkedTextBox newVariableTextBox;
	
	private final VariablesServiceAsync variablesService = GWT.create(VariablesService.class);
	
	private final ArrayList<String> variables = new ArrayList<String>();
	final HashMap<String, Integer> variableValues = new HashMap<String, Integer>();
	
	public interface VariablesPanelListener {
		void updateVariableValues(String[] titles, int[] values);
		void handleError(Throwable error);
		void logVariables();
	}
	
	private final VariablesPanelListener listener;
	
	public VariablesListPanel(VariablesPanelListener listener) {
		// remember the listener to inform
		this.listener = listener;
		// setup our ID
		getElement().setId(EmoTrackConstants.K_CSS_ID_VARIABLETABLEPANEL);
		EmoTrackConstants constants = EmoTrackConstants.Instance;
		this.variablesFlexTable = new FlexTable();
		this.newVariableTextBox = new WatermarkedTextBox("", constants.variableEntry());
		this.newVariableTextBox.getElement().setId(EmoTrackConstants.K_CSS_ID_VARIABLETEXTBOX);
		// create the add button to add things to the flex table
		Button addVariableButton = new Button(constants.addVariable());
		addVariableButton.getElement().setId(EmoTrackConstants.K_CSS_ID_ADDBUTTON);
		// create the variables table nicely
		variablesFlexTable.setText(0, 0, constants.currentVariablesToTrack());
		// Add styles to elements in the stock list table.
		variablesFlexTable.getRowFormatter().addStyleName(0, EmoTrackConstants.K_CSS_CLASS_TABLEHEADER);
		variablesFlexTable.addStyleName(EmoTrackConstants.K_CSS_CLASS_VARIABLETABLE);
		// create the logging button
		Button logVariableButton = createLogValuesButton(constants);
		logVariableButton.getElement().setId(EmoTrackConstants.K_CSS_ID_LOGBUTTON);
		// load the variables into the panel
		loadVariables();

		// Assemble Add panel for the bottom of the table
		HorizontalPanel addPanel = new HorizontalPanel();
		addPanel.getElement().setId(EmoTrackConstants.K_CSS_ID_ADDPANEL);
		addPanel.add(newVariableTextBox);
		addPanel.add(addVariableButton);
		addPanel.add(logVariableButton);
		// center align the buttons
		addPanel.setCellVerticalAlignment(addVariableButton, HasVerticalAlignment.ALIGN_MIDDLE);
		addPanel.setCellVerticalAlignment(logVariableButton, HasVerticalAlignment.ALIGN_MIDDLE);
		
		// setup the children of this panel
		this.add(variablesFlexTable);
		this.add(addPanel);
		
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
	
	private Button createLogValuesButton(EmoTrackConstants constants) {
		// create the log event button
		Button logValuesButton = new Button(constants.logValues());
		// setup the button
		logValuesButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// log the current data
				VariablesListPanel.this.listener.logVariables();		
			}
		});
		return logValuesButton;
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

		// The variable name must be between 1 and 20 chars that are numbers, letters,
		// or dots.
		if (!variableName.matches("^[0-9a-zA-Z\\.]{1,20}$")) {
	      Window.alert(EmoTrackMessages.Instance.invalidVariableName(variableName));
	      newVariableTextBox.selectAll();
	      return;
	    }

		newVariableTextBox.setText("");

		// Don't add the variable if it's already in the table.
		if (isVariableExist(variableName)) {
			// don't allow this addition
			Window.alert(EmoTrackMessages.Instance.usedVariableName(variableName));
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
				// and update the values
				updateValues();
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
		// and update the values
		updateValues();
	}

	private void displayVariable(final String variableName) {
		// Add the variable to the panel
		if (variables.contains(variableName)) {
			// this is already there, ignore this, grr
			EmoTrack.LOG.log(Level.WARNING, "There are duplicate variables of \"" + variableName + "\" in the db");
		}
		else {
			variables.add(variableName);
			VariableRowPanel row = new VariableRowPanel(new VariableRowPanel.VariableRowListener() {
				@Override
				public void updateVariableRow(String variableName, int variableValue) {
					// update the values in our map
					VariablesListPanel.this.variableValues.put(variableName, variableValue);
					// and update our values elsewhere
					VariablesListPanel.this.updateValues();
				}
				
				@Override
				public void removeVariableRow(String variableName) {
					// remove the row from the flex table
					VariablesListPanel.this.removeVariable(variableName);
				}
			}, variableName);
			int rowIndex = variablesFlexTable.getRowCount();
			// set the data to show this in a row on the table
			variablesFlexTable.setText(rowIndex, 0, variableName);
			variablesFlexTable.setWidget(rowIndex, 0, row);
			variablesFlexTable.getCellFormatter().addStyleName(rowIndex, 0, 
					EmoTrackConstants.K_CSS_CLASS_VARIABLEROW);
		}
	}
	
	public void showRows() {
		int noRows = variablesFlexTable.getRowCount();
		for (int i = 1; i < noRows; ++i) {
			Widget widget = variablesFlexTable.getWidget(i, 0);
			if (null != widget && widget instanceof VariableRowPanel) {
				VariableRowPanel row = (VariableRowPanel)widget;
				row.showRow();
			}
		}
	}
	
	private void updateValues() {
		// get all the data from our controls to pass onto any listeners
		String[] titles = new String[variables.size()];
		int[] values = new int[titles.length];
		for (int i = 0; i < titles.length; ++i) {
			titles[i] = variables.get(i);
			Integer value = variableValues.get(titles[i]);
			if (null != value) {
				// use the latest data from the map
				values[i] = value;
			}
			else {
				// not in the map, no data
				values[i] = 0;
			}
		}
		this.listener.updateVariableValues(titles, values);
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
	
	void removeVariable(final String variableName) {
		variablesService.removeVariable(variableName, new AsyncCallback<Void>() {
			public void onFailure(Throwable error) {
				handleError(error);
			}

			public void onSuccess(Void ignore) {
				undisplayVariable(variableName);
			}
		});
	}

	private void handleError(Throwable error) {
		// pass this to the listener
		this.listener.handleError(error);
	}

	private void undisplayVariable(String variableName) {
		int removedIndex = variables.indexOf(variableName);
		variables.remove(removedIndex);
		variablesFlexTable.removeRow(removedIndex + 1);
		// and update the values
		updateValues();
	}
}

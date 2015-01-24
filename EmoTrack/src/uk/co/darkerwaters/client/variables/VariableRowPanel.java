package uk.co.darkerwaters.client.variables;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.variables.slider.VariableValueSlider;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.kiouri.sliderbar.client.event.BarValueChangedEvent;
import com.kiouri.sliderbar.client.event.BarValueChangedHandler;

public class VariableRowPanel extends VerticalPanel {

	public interface VariableRowListener {
		void removeVariableRow(String variableName);
		void updateVariableRow(String variableName, int variableValue);
	}

	HorizontalPanel textPanel = new HorizontalPanel();
	Button deleteButton = new Button();
	Label variableNameLabel = new Label();
	Label variableValueLabel = new Label();
	VariableValueSlider slider = null;
	
	private final VariableRowListener listener;
	
	public VariableRowPanel(VariableRowListener listener, final String variableName) {
		// remember the listener to inform of any changes
		this.listener = listener;
		// set our style
		addStyleName("variable-row");
		textPanel.addStyleName("variable-row");
		// set the data on the labels
		deleteButton.setText("x");
		variableNameLabel.setText(variableName);
		variableValueLabel.addStyleName("variable-title");
		variableValueLabel.addStyleName("variable-description");
		// setup the delete button
		deleteButton.addStyleDependentName("remove");
		deleteButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// inform the listener of this
				VariableRowPanel.this.listener.removeVariableRow(variableName);
			}
		});
		// add them to the panel
		textPanel.add(deleteButton);
		textPanel.add(variableNameLabel);
		textPanel.add(variableValueLabel);
		// and show the rows with initial data of zero
		showRow(0);
	}

	public void showRow() {
		// call the other function with the value currently in the row
		this.showRow(-1);
	}
	
	public void showRow(int value) {
		if (null != this.slider) {
			if (-1 == value) {
				// the specified value is not valid, use the value in the current slider
				value = this.slider.getValue();
			}
			this.remove(this.slider);
		}
		else {
			// this is the first show, add the text controls which resize just fine
			this.add(textPanel);
		}
		// create the slider late so it is the correct size
		this.slider = new VariableValueSlider(10, EmoTrackConstants.K_100PERCENT);
		// setup the slider
		this.slider.addStyleName("variable-slider");
		this.slider.setValue(value);
		this.slider.addBarValueChangedHandler(new BarValueChangedHandler() {
			@Override
			public void onBarValueChanged(BarValueChangedEvent event) {
				// the bar value has changed, change the label
				updateValue(variableNameLabel.getText(), event.getValue());
			}
		});
		this.add(this.slider);
	}

	protected void updateValue(String variableName, int value) {
		// update this on the label
		String valueLabel;
		switch (value) {
		case 0:
			valueLabel = EmoTrackMessages.Instance.describeVal0(variableName);
			break;
		case 1:
			valueLabel = EmoTrackMessages.Instance.describeVal01(variableName);
			break;
		case 2:
			valueLabel = EmoTrackMessages.Instance.describeVal02(variableName);
			break;
		case 3:
			valueLabel = EmoTrackMessages.Instance.describeVal03(variableName);
			break;
		case 4:
			valueLabel = EmoTrackMessages.Instance.describeVal04(variableName);
			break;
		case 5:
			valueLabel = EmoTrackMessages.Instance.describeVal05(variableName);
			break;
		case 6:
			valueLabel = EmoTrackMessages.Instance.describeVal06(variableName);
			break;
		case 7:
			valueLabel = EmoTrackMessages.Instance.describeVal07(variableName);
			break;
		case 8:
			valueLabel = EmoTrackMessages.Instance.describeVal08(variableName);
			break;
		case 9:
			valueLabel = EmoTrackMessages.Instance.describeVal09(variableName);
			break;
		case 10:
			valueLabel = EmoTrackMessages.Instance.describeVal10(variableName);
			break;
		default :
			 valueLabel = EmoTrackMessages.Instance.describeValErr(value);
			 break;
		}
		variableValueLabel.setText(valueLabel);
		// inform the listener of this
		this.listener.updateVariableRow(variableName, value);
	}
}
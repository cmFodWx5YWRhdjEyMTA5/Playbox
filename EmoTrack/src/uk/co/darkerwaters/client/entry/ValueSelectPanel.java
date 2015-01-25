package uk.co.darkerwaters.client.entry;

import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.variables.GaugeChartPanel;
import uk.co.darkerwaters.client.variables.slider.VariableValueSlider;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.visualization.client.visualizations.Gauge;
import com.kiouri.sliderbar.client.event.BarValueChangedEvent;
import com.kiouri.sliderbar.client.event.BarValueChangedHandler;

public class ValueSelectPanel extends FlowPanel {
	private final GaugeChartPanel chartPanel;
	private final String title;
	private VariableValueSlider variableValueSlider = null;
	private final Label variableValueLabel;
	private int currentValue = 0;;
	
	public ValueSelectPanel(final ValueEntryPanel parent, String title) {
		this.addStyleName("entryValue");
		this.addStyleName("valueSelect");
		this.title = title;
		this.variableValueLabel = new Label(title);
		add(variableValueLabel);
		this.setWidth("200px");
		this.chartPanel = new GaugeChartPanel(new GaugeChartPanel.CreationListener() {
			@Override
			public void chartCreated(GaugeChartPanel panel, Gauge chart) {
				// chart created, add to this panel
				addChartControls(parent, chart);
			}
		});
	}

	protected void addChartControls(final ValueEntryPanel parent, final Gauge chart) {
		// add the chart
		chart.setWidth("200px");
		chart.setHeight("200px");
		chartPanel.updateData(new String[] {title}, new int[] {ValueSelectPanel.this.currentValue});
		this.add(chart);
		// and add the slider
		this.variableValueSlider = new VariableValueSlider(10, "150px");
		this.variableValueSlider.addBarValueChangedHandler(new BarValueChangedHandler() {
			@Override
			public void onBarValueChanged(BarValueChangedEvent event) {
				// update the chart
				ValueSelectPanel.this.currentValue = event.getValue();
				chartPanel.updateData(new String[] {title}, new int[] {ValueSelectPanel.this.currentValue});
				updateValue(title, ValueSelectPanel.this.currentValue);
				//chart.setWidth("200px");
			}
		});
		this.variableValueSlider.addStyleName("variable-slider");
		Button deleteButton = new Button("X");
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				parent.removeVariable(ValueSelectPanel.this.title);
			}
		});
		deleteButton.addStyleName("entryValue");
		FlowPanel controlPanel = new FlowPanel();
		controlPanel.add(deleteButton);
		controlPanel.add(this.variableValueSlider);
		this.add(controlPanel);
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
		this.variableValueLabel.setText(valueLabel);
	}

	protected void handleError(Throwable error) {
		// TODO Auto-generated method stub
		
	}

	public String getVariableTitle() {
		return this.title;
	}

	public Integer getVariableValue() {
		return this.currentValue;
	}

}

package uk.co.darkerwaters.client.entry;

import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.FlatUI;
import uk.co.darkerwaters.shared.GaugeChartPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.visualization.client.visualizations.Gauge;

public class EmotionChartSelectPanel extends FlowPanel {
	private final GaugeChartPanel chartPanel;
	private final String title;
	private SimplePanel variableValueSlider = null;
	private final Label variableValueLabel;
	private int currentValue = 0;;
	
	public EmotionChartSelectPanel(final EmotionsTab parent, String title) {
		this.addStyleName("entryValue");
		this.addStyleName("valueSelect");
		this.title = title;
		this.variableValueLabel = new Label(title);
		this.variableValueLabel.addStyleName("valueLabel");
		this.addStyleName("chartLabel");
		this.setWidth("200px");
		this.chartPanel = new GaugeChartPanel(new GaugeChartPanel.CreationListener() {
			@Override
			public void chartCreated(GaugeChartPanel panel, Gauge chart) {
				// chart created, add to this panel
				addChartControls(parent, chart);
			}
		});
	}

	protected void addChartControls(final EmotionsTab parent, final Gauge chart) {
		// add the chart
		chart.setWidth("200px");
		chart.setHeight("200px");
		chartPanel.updateData(new String[] {title}, new int[] {EmotionChartSelectPanel.this.currentValue});
		this.add(chart);
		// and add the slider
		this.variableValueSlider = FlatUI.makeSlider(null, new FlatUI.SliderListener() {
			@Override
			public void valueChanged(int value) {
				// update the chart
				EmotionChartSelectPanel.this.currentValue = value;
				chartPanel.updateData(new String[] {title}, new int[] {EmotionChartSelectPanel.this.currentValue});
				updateValue(title, EmotionChartSelectPanel.this.currentValue);
				//chart.setWidth("200px");
			}
		});
		Button deleteButton = new Button("X");
		FlatUI.makeButton(deleteButton, null);
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				parent.removeVariable(EmotionChartSelectPanel.this.title);
			}
		});
		deleteButton.addStyleName("entryValue");
		FlowPanel controlPanel = new FlowPanel();
		controlPanel.add(deleteButton);
		controlPanel.add(this.variableValueLabel);
		this.add(controlPanel);
		this.add(variableValueSlider);
		FlatUI.configureSlider(this.variableValueSlider);
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

	public String getDescription() {
		return this.variableValueLabel.getText();
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

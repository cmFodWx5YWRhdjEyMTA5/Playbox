package uk.co.darkerwaters.client.entry;

import java.util.Date;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.tracks.TrackPointData;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import uk.co.darkerwaters.shared.DonutChartPanel;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.visualization.client.visualizations.PieChart;

public class SleepTab extends ValueEntryTab {
	
	public static String[] sleepColours = new String[] {"#66CCFF", "#003399", "#fdf7c5", "transparent"};
	
	private FlowPanel mainPanel = new FlowPanel();
	private TextBox[] asleepText = new TextBox[] {new TextBox(), new TextBox()};
	private TextBox[] deepSleepText = new TextBox[] {new TextBox(), new TextBox()};
	private DonutChartPanel donutChartPanel;
	
	private final String[] titles = new String[] {
			EmoTrackConstants.Instance.sleeping(),
			EmoTrackConstants.Instance.deepSleep(),
			EmoTrackConstants.Instance.awake()
	};
	private FlowPanel chartPanel;
	private SimplePanel asleepSlider;
	private SimplePanel deepSleepSlider = null;
	
	private boolean isSlidersInitialised = false;
	
	private boolean isCreatingDeepSlider = false;
	
	public SleepTab(ValueEntryListener listener, TrackPointServiceAsync trackPointService, final DateSelectTab dateSelectPanel) {
		super(listener, trackPointService);
	    
	    mainPanel.add(FlatUI.createHeader(6, EmoTrackConstants.Instance.sleepTrackExplan()));
		
		FlowPanel entryPanel = new FlowPanel();
		
		this.asleepSlider = FlatUI.makeSlider(null, new FlatUI.SliderListener() {
			@Override
			public void valueChanged(int value) {
				SleepTab.this.asleepText[0].setText(Integer.toString(value));
				int[] values = updateEntryValues();
				makeDeepSleepSlider();
				FlatUI.configureSlider(deepSleepSlider, values[0] / 60);
			}
		});
		makeDeepSleepSlider();
	   
		// create the sleep entry controls
		entryPanel.add(createEntryPanel(EmoTrackConstants.Instance.timeAsleep(), this.asleepText, asleepSlider));
		FlowPanel deepPanel = createEntryPanel(EmoTrackConstants.Instance.ofWhich(), this.deepSleepText, deepSleepSlider);
		InlineLabel label = FlatUI.createLabel(EmoTrackConstants.Instance.wasDeepSleep(), null);
		label.addStyleName("entryValue sleep-entry-label");
		deepPanel.add(label);
		entryPanel.add(deepPanel);
		
		// and a nice graph
		this.donutChartPanel = new DonutChartPanel(new DonutChartPanel.CreationListener() {
			@Override
			public void chartCreated(DonutChartPanel panel, PieChart chart) {
				// set the data
				addChartControls(SleepTab.this, chart);
			}
		}, SleepTab.sleepColours );
		mainPanel.add(entryPanel);
		mainPanel.getElement().setId("logSleepPanel");
	    //TextBox eventTextBox = new TextBox();
	    //FlatUI.makeEntryText(eventTextBox, EmoTrackConstants.K_CSS_ID_EVENTTEXTBOX, EmoTrackConstants.Instance.eventEntry());
	    //mainPanel.add(eventTextBox);
		
		this.chartPanel = new FlowPanel();
		chartPanel.addStyleName("sleep-chart-panel");
		mainPanel.add(chartPanel);
	    
	    // create the log values button
	    Button button = createLogEventButton(dateSelectPanel);
	    FlowPanel logValPanel = createLogValuesButtonPanel(button, dateSelectPanel, false);
		mainPanel.add(logValPanel);
		
		mainPanel.add(createResultsPanel());
	}

	private void makeDeepSleepSlider() {
		Panel sliderParent = null;
		isCreatingDeepSlider = true;
		if (null != this.deepSleepSlider) {
			sliderParent = (Panel)this.deepSleepSlider.getParent();
			this.deepSleepSlider.removeFromParent();
		}
		this.deepSleepSlider = FlatUI.makeSlider(null, new FlatUI.SliderListener() {
			@Override
			public void valueChanged(int value) {
				if (false == isCreatingDeepSlider) {
					SleepTab.this.deepSleepText[0].setText(Integer.toString(value));
				}
				isCreatingDeepSlider = false;
			}
		});
		if (null != sliderParent) {
			sliderParent.add(deepSleepSlider);
		}
	}
	
	@Override
	public void setActiveItem(boolean isActive) {
		super.setActiveItem(isActive);
		if (isActive) {
			this.donutChartPanel.createChart();
			if (false == this.isSlidersInitialised) {
				FlatUI.configureSlider(asleepSlider, 24);
				FlatUI.configureSlider(deepSleepSlider, 24);
				this.isSlidersInitialised = true;
			}
		}
		else {
			this.donutChartPanel.destroyChart();
		}
	}

	private FlowPanel createEntryPanel(String labelContent, final TextBox[] inputBoxes, SimplePanel slider) {
		FlowPanel panel = new FlowPanel();
		panel.addStyleName("sleep-entry-panel");
		InlineLabel label = FlatUI.createLabel(labelContent, null);
		label.addStyleName("entryValue sleep-entry-label");
		panel.add(label);
		
		FlowPanel controlPanel = new FlowPanel();
		controlPanel.addStyleName("sleep-entry-panel");
		
		// hour input
		FlatUI.makeEntryText(inputBoxes[0], null, null);
		inputBoxes[0].addStyleName("sleep-entry-text");
		FlowPanel hourGroup = new FlowPanel();
		hourGroup.addStyleName("sleep-entry entryValue");
		hourGroup.add(inputBoxes[0]);
		hourGroup.add(FlatUI.createLabel(EmoTrackConstants.Instance.hours(), null));
		controlPanel.add(hourGroup);
		
		label = FlatUI.createLabel(":", null);
		label.addStyleName("entryValue sleep-entry-label");
		controlPanel.add(label);
		
		// minute input
		FlatUI.makeEntryText(inputBoxes[1], null, null);
		inputBoxes[1].addStyleName("sleep-entry-text");
		FlowPanel minuteGroup = new FlowPanel();
		minuteGroup.addStyleName("sleep-entry entryValue");
		minuteGroup.add(inputBoxes[1]);
		minuteGroup.add(FlatUI.createLabel(EmoTrackConstants.Instance.minutes(), null));
		controlPanel.add(minuteGroup);
		
		slider.addStyleName("sleep-slider");
		controlPanel.add(slider);
		panel.add(controlPanel);
		
		// handle the text entry
		inputBoxes[0].addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				restrictEventToNumber(event, inputBoxes[0], "hours");
				int[] values = updateEntryValues();
				makeDeepSleepSlider();
				FlatUI.configureSlider(deepSleepSlider, values[0] / 60);
			}
		});
		inputBoxes[1].addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				restrictEventToNumber(event, inputBoxes[1], "mins");
				updateEntryValues();
			}
		});
		
		return panel;
	}

	protected int[] updateEntryValues() {
		// get the values from the text boxes
		int[] results = new int[3];
		
		results[0] += limitValue(asleepText[0], 60, 23 * 60);
		results[0] += limitValue(asleepText[1], 1, 59);
		
		results[1] += limitValue(deepSleepText[0], 60, results[0]);
		results[1] += limitValue(deepSleepText[1], 1, Math.min(59, results[0] - results[1]));
		
		// and the active time
		results[2] = 24 * 60 - results[0];
		
		if (null != this.donutChartPanel) {
			// chart data is different though, need to remove the deep sleep time from the asleep time
			int[] pieResults = new int[3];
			pieResults[0] = results[0] - results[1];
			pieResults[1] = results[1];
			pieResults[2] = results[2];
			this.donutChartPanel.updateData(titles, pieResults);
		}
		// return the results
		return results;
		
	}

	private int limitValue(TextBox input, int factor, int maxValue) {
		// get the new value in minutes
		int value = 0;
		try {
			value = Integer.parseInt(input.getText()) * factor;
		}
		catch (NumberFormatException e) {
			EmoTrack.LOG.severe(e.getMessage());
		}
		if (value > maxValue) {
			value = maxValue;
			input.setText(Integer.toString(value / factor));
		}
		return value;
	}

	@Override
	public Panel getContent() {
		return this.mainPanel;
	}

	public static void restrictEventToNumber(ChangeEvent event, TextBox input, String type) {
		String text = input.getText();
		String digits = text.replaceAll("[^0-9]", "");
		if (type.equals("hours")) {
			// limit the hours
			if (false == digits.isEmpty() && Integer.parseInt(digits) > 24) {
				digits = "24";
			}
		}
		else if (type.equals("mins")) {
			// limit the minutes
			if (false == digits.isEmpty() && Integer.parseInt(digits) > 60) {
				digits = "60";
			}
		}
		input.setText(digits);
	}

	protected void addChartControls(final SleepTab parent, final PieChart chart) {
		// add the chart
		chart.setWidth("200px");
		chart.setHeight("200px");
		chart.addStyleName("sleep-chart");
		this.chartPanel.add(chart);
	}

	private Button createLogEventButton(final DateSelectTab dateSelectPanel) {
		// create the log event button
		Button logEventButton = new Button(EmoTrackConstants.Instance.logSleep());
		FlatUI.makeButton(logEventButton, "logSleepButton", EmoTrackConstants.Instance.tipLogSleep());
		logEventButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// and log the event
				logEvent(dateSelectPanel.getSelectedDate(), updateEntryValues());
			}
		});
		return logEventButton;
	}
	
	protected void logEvent(Date selectedDate, int[] values) {
		if (values[0] <= 0) {
			EmoTrack.alertWidget(EmoTrackConstants.Instance.alertTitle(), EmoTrackMessages.Instance.invalidAmount());
			return;
		}
		TrackPointData point = new TrackPointData(DateSelectTab.limitDateToDay(selectedDate));
		StringBuilder description = new StringBuilder(EmoTrackConstants.Instance.recorded());
		for (int i = 0; i < values.length - 1; ++i) {
			point.addValue(TrackPointData.SLEEPKEY[i], values[i]);
			description.append(values[i]);
			description.append(" ");
			description.append(EmoTrackConstants.Instance.minutes());
			description.append(EmoTrackConstants.Instance.of());
			description.append(titles[i]);
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
				SleepTab.this.listener.updateTrackEntry(result);
				// create the string for this data we just added
				alertResult(successString);
			}
		});	
	}
}

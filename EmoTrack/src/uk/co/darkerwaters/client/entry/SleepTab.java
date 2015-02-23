package uk.co.darkerwaters.client.entry;

import java.util.Date;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.EmoTrackConstants;
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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.visualization.client.visualizations.PieChart;

public class SleepTab extends ValueEntryTab {
	
	private FlowPanel mainPanel = new FlowPanel();
	private TextBox[] inBedText = new TextBox[] {new TextBox(), new TextBox()};
	private TextBox[] asleepText = new TextBox[] {new TextBox(), new TextBox()};
	private TextBox[] deepSleepText = new TextBox[] {new TextBox(), new TextBox()};
	private DonutChartPanel donutChartPanel;
	
	private final String[] titles = new String[] {
			EmoTrackConstants.Instance.timeInBed(),
			EmoTrackConstants.Instance.sleeping(),
			EmoTrackConstants.Instance.deepSleep(),
			EmoTrackConstants.Instance.awake()
	};
	private FlowPanel rightPanel;
	
	public SleepTab(ValueEntryListener listener, TrackPointServiceAsync trackPointService, final DateSelectTab dateSelectPanel) {
		super(listener, trackPointService);
		
		FlowPanel leftPanel = new FlowPanel();
		leftPanel.addStyleName("sleep-left");
	   
		// create the sleep entry controls
		leftPanel.add(createEntryPanel(titles[0], this.inBedText));
		leftPanel.add(createEntryPanel(titles[1], this.asleepText));
		leftPanel.add(createEntryPanel(titles[2], this.deepSleepText));
		
		// and a nice graph
		this.donutChartPanel = new DonutChartPanel(new DonutChartPanel.CreationListener() {
			@Override
			public void chartCreated(DonutChartPanel panel, PieChart chart) {
				// set the data
				addChartControls(SleepTab.this, chart);
			}
		});
		mainPanel.add(leftPanel);
		mainPanel.getElement().setId("logSleepPanel");
	    //TextBox eventTextBox = new TextBox();
	    //FlatUI.makeEntryText(eventTextBox, EmoTrackConstants.K_CSS_ID_EVENTTEXTBOX, EmoTrackConstants.Instance.eventEntry());
	    //mainPanel.add(eventTextBox);
		
		this.rightPanel = new FlowPanel();
		rightPanel.addStyleName("sleep-right");
		mainPanel.add(rightPanel);
	    
	    // create the log values button
	    Button button = createLogEventButton(dateSelectPanel);
	    FlowPanel logValPanel = createLogValuesButtonPanel(button, dateSelectPanel);
		mainPanel.add(logValPanel);
		
		mainPanel.add(createResultsPanel());
	}
	
	@Override
	public void setActiveItem(boolean isActive) {
		super.setActiveItem(isActive);
		if (isActive) {
			this.donutChartPanel.createChart();
		}
		else {
			this.donutChartPanel.destroyChart();
		}
	}

	private FlowPanel createEntryPanel(String labelContent, final TextBox[] inputBoxes) {
		FlowPanel panel = new FlowPanel();
		panel.addStyleName("sleep-entry-panel");
		InlineLabel label = FlatUI.createLabel(labelContent, null);
		label.addStyleName("entryValue sleep-entry-label");
		panel.add(label);
		
		// hour input
		FlatUI.makeEntryText(inputBoxes[0], null, null);
		inputBoxes[0].addStyleName("sleep-entry-text");
		FlowPanel hourGroup = new FlowPanel();
		hourGroup.addStyleName("sleep-entry entryValue");
		hourGroup.add(inputBoxes[0]);
		hourGroup.add(FlatUI.createLabel(EmoTrackConstants.Instance.hours(), null));
		panel.add(hourGroup);
		
		// minute input
		FlatUI.makeEntryText(inputBoxes[1], null, null);
		inputBoxes[1].addStyleName("sleep-entry-text");
		FlowPanel minuteGroup = new FlowPanel();
		minuteGroup.addStyleName("sleep-entry entryValue");
		minuteGroup.add(inputBoxes[1]);
		minuteGroup.add(FlatUI.createLabel(EmoTrackConstants.Instance.minutes(), null));
		panel.add(minuteGroup);
		
		// handle the text entry
		inputBoxes[0].addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				restrictEventToNumber(event, true, inputBoxes[0]);
				updateEntryValues();
			}
		});
		inputBoxes[1].addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				restrictEventToNumber(event, false, inputBoxes[1]);
				updateEntryValues();
			}
		});
		
		return panel;
	}

	protected int[] updateEntryValues() {
		// get the values from the text boxes
		int[] results = new int[4];
		results[0] += limitValue(inBedText[0], 60, results);
		results[0] += limitValue(inBedText[1], 1, results);
		
		results[1] += limitValue(asleepText[0], 60, results);
		results[1] += limitValue(asleepText[1], 1, results);
		
		results[2] += limitValue(deepSleepText[0], 60, results);
		results[2] += limitValue(deepSleepText[1], 1, results);
		
		// and the active time
		results[3] = 24 * 60 - getTotal(results);
		
		if (null != this.donutChartPanel) {
			this.donutChartPanel.updateData(titles, results);
		}
		// return the results
		return results;
		
	}

	private int limitValue(TextBox input, int factor, int[] results) {
		// get the total
		int total = getTotal(results);
		// get the new value in minutes
		int value = 0;
		try {
			value = Integer.parseInt(input.getText()) * factor;
		}
		catch (NumberFormatException e) {
			EmoTrack.LOG.severe(e.getMessage());
		}
		if (total + value > 24 * 60) {
			// value is too high
			value = 24 * 60 - total;
			input.setText(Integer.toString((int)(value / factor)));
		}
		return value;
	}

	private int getTotal(int[] results) {
		int total = 0;
		for (int value : results) {
			total += value;
		}
		return total;
	}

	@Override
	public Panel getContent() {
		return this.mainPanel;
	}

	private void restrictEventToNumber(ChangeEvent event, boolean isHours, TextBox input) {
		String text = input.getText();
		String digits = text.replaceAll("[^0-9]", "");
		if (isHours) {
			// limit the hours
			if (false == digits.isEmpty() && Integer.parseInt(digits) > 24) {
				digits = "24";
			}
		}
		else {
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
		this.rightPanel.add(chart);
	}

	private Button createLogEventButton(final DateSelectTab dateSelectPanel) {
		// create the log event button
		Button logEventButton = new Button(EmoTrackConstants.Instance.logSleep());
		FlatUI.makeButton(logEventButton, "logEventButton");
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
		final TrackPointData point = new TrackPointData(selectedDate);
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
		trackPointService.addTrackPoint(point, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable error) {
				listener.handleError(error);
			}
			@Override
			public void onSuccess(Void result) {
				// inform the graph / view of this new point
				SleepTab.this.listener.updateTrackEntry(point);
				// create the string for this data we just added
				alertResult(successString);
			}
		});	
	}
}

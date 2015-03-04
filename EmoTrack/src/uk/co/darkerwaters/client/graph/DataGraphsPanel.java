package uk.co.darkerwaters.client.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.vaadin.gwtgraphics.client.shape.Circle;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackListener;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.SleepTab;
import uk.co.darkerwaters.client.graph.DataGraph.DataGraphListener;
import uk.co.darkerwaters.client.graph.EventGraphDataHandler.EventLabel;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class DataGraphsPanel extends VerticalPanel {
	
	private final ArrayList<TrackPointData> dataRows = new ArrayList<TrackPointData>();
	private final HashMap<String, Integer> columnsAdded = new HashMap<String, Integer>();
	
	private Date currentSelectedDate = null;
	private Label selectedLabel;
	private final EmoTrackListener listener;
	
	public static DateTimeFormat mthDate = DateTimeFormat.getFormat("yyyy-MM");
	public static DateTimeFormat mthDisplayDate = DateTimeFormat.getFormat("MMM yyyy");
	public static DateTimeFormat dayDate = DateTimeFormat.getFormat("yyyy-MM-dd");
	
	private DataGraph<Date, String> eventGraph;
	
	private DataGraph<Date, Integer> emotionGraph;
	
	private DataGraph<Date, Integer> activityGraph;
	
	private DataGraph<Date, Integer> sleepGraph;
	
	private final DataGraphsDataHandler trackDataHandler;
	
	private TrackPointGraphDataHandler[] dataHandlers;
	private TextBox selectedTextBox;
	private Button deleteButton;
	private EventGraphDataHandler eventDataHandler;
	private Date dataEndDate = new Date();
	private Date dataStartDate = new Date();
	
	public DataGraphsPanel(EmoTrackListener listener, DataGraphsDataHandler trackDataHandler) {
		// create all our controls in this panel
		this.listener = listener;
		this.trackDataHandler = trackDataHandler;
		this.getElement().setId(EmoTrackConstants.K_CSS_ID_DATACHARTPANEL);
		// create the data handlers
		dataHandlers = new TrackPointGraphDataHandler[] {
				new TrackPointGraphDataHandler(TrackPointGraphDataHandler.Type.emotion),
				new TrackPointGraphDataHandler(TrackPointGraphDataHandler.Type.activity),
				new TrackPointGraphDataHandler(TrackPointGraphDataHandler.Type.sleep)
		};
		// create the graphs
		DataGraphListener<Date, Integer> graphListener = createGraphListener();
		this.eventDataHandler = new EventGraphDataHandler();
		this.eventGraph = new DataGraph<Date, String>(EmoTrackConstants.Instance.events(), eventDataHandler, new DataGraphListener<Date, String>(){
			@Override
			public void seriesSelected(String seriesTitle, boolean isSelected) {
				// TODO handle series selection
			}
			@Override
			public void pointSelected(DataGraph<Date, String> source, Circle point, String seriesTitle, Date x, String y) {
				handlePointSelection(source, point, null, x, y);
				//eventDataHandler.eventSelected(eventGraph, x, y);
			}}, null);
		this.emotionGraph = new DataGraph<Date, Integer>(EmoTrackConstants.Instance.emotions(), dataHandlers[0], graphListener, null);
		this.activityGraph = new DataGraph<Date, Integer>(EmoTrackConstants.Instance.activity(), dataHandlers[1], graphListener, null);
		this.sleepGraph = new DataGraph<Date, Integer>(EmoTrackConstants.Instance.sleep(), dataHandlers[2], graphListener, SleepTab.sleepColours);
		this.sleepGraph.setIsAreaChart(true);
		
		FlowPanel topPanel = new FlowPanel();
		topPanel.add(createSelectionControls());
		topPanel.add(createDataControls());
		topPanel.getElement().setId("chartTopPanel");
		this.add(topPanel);
		// setup the event graph specially
		this.eventGraph.getContent().removeStyleName("data-graph");
		this.eventGraph.getContent().addStyleName("data-event-graph");
		this.eventGraph.setIsDrawLegend(false);
		this.eventGraph.setIsDrawPath(false);
		
		// set styles on these graphs and add them
		addGraph(this.eventGraph);
		addGraph(this.emotionGraph);
		addGraph(this.activityGraph);
		addGraph(this.sleepGraph);
		
		loadTrackData();
	}

	private void addGraph(DataGraph<?, ?> graph) {
		Panel graphPanel = graph.getContent();
		graphPanel.addStyleName("data-graph");
		this.add(graphPanel);
	}

	private DataGraph.DataGraphListener<Date, Integer> createGraphListener() {
		return new DataGraph.DataGraphListener<Date, Integer>() {
			@Override
			public void seriesSelected(String seriesTitle, boolean isSelected) {
				// TODO handle series selection
			}
			@Override
			public void pointSelected(DataGraph<Date, Integer> source, Circle point, String seriesTitle, Date x, Integer y) {
				handlePointSelection(source, point, seriesTitle, x, y == null ? "null" : y.toString());
			}
		};
	}

	protected void handlePointSelection(final DataGraph<?, ?> source, Circle point, String seriesTitle, Date selectedDate, String value) {
		// default to no selection
		currentSelectedDate = null;
		selectedTextBox.setText("");
		deleteButton.setEnabled(false);
		currentSelectedDate = selectedDate;
		if (null != currentSelectedDate) {
			selectedTextBox.setText(EmoTrackMessages.Instance.selectedDate(currentSelectedDate));
			deleteButton.setEnabled(true);
			if (null != value) {
				String description = value + " " + (seriesTitle == null ? "" : (seriesTitle + " ")) + EmoTrackConstants.Instance.at() + " " + EmoTrackMessages.Instance.date(selectedDate);
				
				final EventLabel eventLabel = new EventGraphDataHandler.EventLabel(point.getX() + 5, point.getY() + 5, description, true);
				eventLabel.addToGraph(source);
				
				Timer timer = new Timer() {
					@Override
					public void run() {
						eventLabel.removeFromGraph(source);
					}
				};
				timer.schedule(5000);
			}
		}
	}

	private void loadTrackData() {
		final String fromDate;
		final String toDate;
		if (null == currentSelectedDate) {
			// get the track points for the current date - so 30 days from now
			Date from = new Date();
			// this is the start of today though, take 30 days off
			CalendarUtil.addDaysToDate(from, -30);
			fromDate = dayDate.format(from);
			// to null (forever)
			toDate = "";
		}
		else {
			// get the specified month of data
			String selected = mthDate.format(this.currentSelectedDate) + "-01";
			Date to = new Date(dayDate.parse(selected).getTime());
			fromDate = dayDate.format(to);
			CalendarUtil.addMonthsToDate(to, 1);
			toDate = dayDate.format(to);
		}
		this.dataStartDate  = dayDate.parse(fromDate);
		if (toDate.isEmpty()) {
			// do until tonight
			Date temp = new Date();
			CalendarUtil.addDaysToDate(temp, 1);
			this.dataEndDate = temp;
		}
		else {
			this.dataEndDate = dayDate.parse(toDate);
		}
		for (TrackPointGraphDataHandler handler : this.dataHandlers) {
			handler.setDateRange(this.dataStartDate, this.dataEndDate);
		}
		this.eventDataHandler.setDateRange(this.dataStartDate, this.dataEndDate);
		// and get the track points for this period
		this.trackDataHandler.getTrackPoints(fromDate, toDate, new AsyncCallback<TrackPointData[]>() {
			@Override
			public void onFailure(Throwable error) {
				handleError(error);
				listener.handleError(error);
				listener.loadingComplete();
			}
			@Override
			public void onSuccess(TrackPointData[] result) {
				// show this data on this chart
				showTrackData(result);
				DataGraphsPanel.this.selectedLabel.setText(fromDate + " --- " + toDate);
				updateTrackSelectionControls(null, null);
				listener.loadingComplete();
			}
		});
	}

	protected void updateTrackSelectionControls(Button lessButton, Button moreButton) {
		if (null != lessButton) {
			lessButton.setEnabled(true);
		}
		// are we in this month?
		if (null != moreButton) {
			moreButton.setEnabled(null != this.currentSelectedDate);
		}
		// set the label
		if (null == this.currentSelectedDate) {
			this.selectedLabel.setText(EmoTrackConstants.Instance.latestValues());
		}
		else {
			this.selectedLabel.setText(mthDisplayDate.format(this.currentSelectedDate));
		}
	}

	protected void deleteSelectedData(final TrackPointData point) {
		this.trackDataHandler.removeTrackPoint(point, new AsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable error) {
				handleError(error);
			}
			@Override
			public void onSuccess(Void result) {
				unshowTrackData(point.getTrackDate());
			}
		});
	}
	
	protected void showTrackData(TrackPointData[] trackData) {
		// show this data on this chart
		this.columnsAdded.clear();
		this.dataRows.clear();
		// now add all the data
		for (int i = 0; i < trackData.length; ++i) {
			addTrackData(trackData[i]);
		}
		// now we have constructed the ordered list of data
		// re-construct the chart's data to show it
		reconstructChartData();
	}
	
	public void unshowTrackData(Date removalDate) {
		// remove all data for the specified date
		for (int i = this.dataRows.size() - 1; i >= 0; --i) {
        	// check our date against those in the list
        	Date compareDate = this.dataRows.get(i).getTrackDate();
        	if (null != compareDate) {
        		if (removalDate.equals(compareDate)) {
        			// remove this
        			this.dataRows.remove(i);
        		}
        	}
        }
		// now we have constructed the ordered list of data
		// re-construct the chart's data to show it
		reconstructChartData();
	}
	
	public void reconstructChartData() {
		// show all the data from the ordered list, first remove all the current rows
		this.eventGraph.clearData();
		this.emotionGraph.clearData();
		this.activityGraph.clearData();
		this.sleepGraph.clearData();
		// because the sleep chart is an area chart, we need to ensure the series are in, in the right order, add them now
		this.sleepGraph.addData(TrackPointData.SLEEPKEY[0], null, null);
		this.sleepGraph.addData(TrackPointData.SLEEPKEY[1], null, null);
		// now re-populate all the rows of data
		for (TrackPointData trackData : this.dataRows) {
			Date trackDate = trackData.getTrackDate();
			String[] titles = trackData.getValuesNames();
			Integer[] values = trackData.getValuesValues();
			// add all this new data
			for (int i = 0; i < titles.length && i < values.length; ++i) {
				this.emotionGraph.addData(titles[i], trackDate, values[i]);
				this.activityGraph.addData(titles[i], trackDate, values[i]);
				this.sleepGraph.addData(titles[i], trackDate, values[i]);
			}
			// is there an event?
			String eventString = trackData.getEvent();
			if (null != eventString && false == eventString.isEmpty()) {
				// add this to the event series
				this.eventGraph.addData("events", trackDate, eventString);
			}
		}
		// now draw this new chart data
		this.eventGraph.resizeWindow();
		this.emotionGraph.resizeWindow();
		this.activityGraph.resizeWindow();
		this.sleepGraph.resizeWindow();
	}

	public void showTrackData(TrackPointData trackData) {
		// add the track data into our ordered list
		addTrackData(trackData);
		// now we have constructed the ordered list of data
		// re-construct the chart's data to show it
		reconstructChartData();
	}

	private void addTrackData(TrackPointData trackData) {
		// insert the item in the correct position in the array
		Date insertDate = trackData.getTrackDate();
		if (null != insertDate 
				&& insertDate.compareTo(this.dataEndDate) <= 0
				&& insertDate.compareTo(this.dataStartDate) >= 0) {
			// this is ok data to put in the graph data to show, first
			// find this date, if already there, we want to replace it
			boolean isAdded = false;
			for (int i = 0; i < this.dataRows.size(); ++i) {
				Date compareDate = this.dataRows.get(i).getTrackDate();
	        	if (null != compareDate && insertDate.equals(compareDate)) {
	        		// this is the one to replace
	        		this.dataRows.set(i, trackData);
	        		isAdded = true;
	        		break;
	        	}
			}
			if (false == isAdded) {
				// insert at the correct location
				this.dataRows.add(trackData);
		        for (int i = this.dataRows.size()-1; i > 0; i--) {
		        	// check our date against those in the list
		        	Date compareDate = this.dataRows.get(i-1).getTrackDate();
		        	if (null != compareDate) {
		        		if (insertDate.compareTo(compareDate) > 0) {
		        			// this is before us, in the correct place, stop now
		        			break;
		        		}
		        		// else move our inserted one down to the new position
		        		Collections.swap(this.dataRows, i, i-1);
		        	}
		        }
			}
		}
	}
	
	private FlowPanel createSelectionControls() {
		FlowPanel selectionPanel = new FlowPanel();
		selectionPanel.getElement().setId(EmoTrackConstants.K_CSS_ID_DATACHARTSELECTIONPANEL);
		final Button lessButton = new Button("<");
		FlatUI.makeButton(lessButton, null, EmoTrackConstants.Instance.tipPreviousMonth());
		this.selectedLabel = FlatUI.createLabel(EmoTrackConstants.Instance.latestValues(), EmoTrackConstants.K_CSS_ID_DATACHARTDATELABEL);
		final Button moreButton = new Button(">");
		FlatUI.makeButton(moreButton, null, EmoTrackConstants.Instance.tipNextMonth());
		
		selectionPanel.add(lessButton);
		selectionPanel.add(this.selectedLabel);
		selectionPanel.add(moreButton);
		
		lessButton.addStyleName(EmoTrackConstants.K_CSS_CLASS_SELECTDATEBUTTON);
		lessButton.setEnabled(true);
		moreButton.addStyleName(EmoTrackConstants.K_CSS_CLASS_SELECTDATEBUTTON);
		moreButton.setEnabled(false);
		
		lessButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// move the selected date less some
				adjustTimePeriod(-1, lessButton, moreButton);
			}
		});
		moreButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// move the selected date less some
				adjustTimePeriod(1, lessButton, moreButton);
			}
		});
		// return the selection panel to this panel to show it
		return selectionPanel;
	}

	private void adjustTimePeriod(int monthOffset, Button lessButton, Button moreButton) {
		// adjust the selected time
		if (this.currentSelectedDate == null) {
			this.currentSelectedDate = new Date();
		}
		// add / remove the month from this date
		CalendarUtil.addMonthsToDate(this.currentSelectedDate, monthOffset);
		// get now as a month?
		String nowMonth = mthDate.format(new Date());
		if (monthOffset > 0 && nowMonth.equals(mthDate.format(this.currentSelectedDate))) {
			// this is this month and we are moving forward, show current values
			this.currentSelectedDate = null;
		}
		// and update the controls
		updateTrackSelectionControls(lessButton, moreButton);
		// and get this data
		loadTrackData();
	}

	private FlowPanel createDataControls() {
		FlowPanel dataPanel = new FlowPanel();
		dataPanel.getElement().setId(EmoTrackConstants.K_CSS_ID_DATACHARTDATAPANEL);
		this.selectedTextBox = new TextBox();
		FlatUI.makeEntryText(selectedTextBox, EmoTrackConstants.K_CSS_ID_DATACHARTDATATEXT, EmoTrackConstants.Instance.selectValue());
		this.deleteButton = new Button(EmoTrackConstants.Instance.deleteSelectionButton());
		FlatUI.makeButton(deleteButton, EmoTrackConstants.K_CSS_ID_DELETEDATABUTTON, EmoTrackConstants.Instance.tipDeleteGraphSelection());
		deleteButton.setEnabled(false);
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// delete the current selected date data
				deleteSelectedData(new TrackPointData(currentSelectedDate));
				currentSelectedDate = null;
				deleteButton.setEnabled(false);
				selectedTextBox.setText("");
			}
		});
		//TODO removed the delete controls as now on the analysis page
		//dataPanel.add(deleteButton);
		//dataPanel.add(selectedTextBox);
		return dataPanel;
	}
	private void handleError(Throwable error) {
		if (null != this.listener) {
			this.listener.handleError(error);
		}
	}

}

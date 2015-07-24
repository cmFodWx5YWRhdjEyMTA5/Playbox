package com.alonyx.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alonyx.shared.TrainTimeParser;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TrainData implements EntryPoint {

	private FlexTable trainsTable;

	private Label resultsLabel;

	private ListBox stationEdit;
	
	public static final Logger LOG = Logger.getLogger(TrainData.class.getName());
	
	public static DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm:ss");
	
	public static DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy MMM dd HH:mm:ss");

	@Override
	public void onModuleLoad() {

		this.resultsLabel = new Label("");
		this.resultsLabel.getElement().setId("results_label");

		// Add a drop box with the list types
		this.stationEdit = new ListBox(false);
	    this.stationEdit.getElement().setId("station_edit");
	    this.stationEdit.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				loadTrains();
			}
		});

		Button trainsButton = new Button("update trains");
		trainsButton.getElement().setId("load_trains_button");
		trainsButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				loadTrains();
			}
		});

		Button gatherButton = new Button("force gathering");
		trainsButton.getElement().setId("gather_button");
		gatherButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				gatherTrains();
			}
		});

		trainsTable = new FlexTable();
		trainsTable.getElement().setId("trains_table");

		/*
        // Let's put a button in the middle...
        trainsTable.setWidget(1, 0, stationEdit);
        trainsTable.setWidget(1, 1, trainsButton);
        // ...and set it's column span so that it takes up the whole row.
        trainsTable.getFlexCellFormatter().setColSpan(1, 1, 2);

        // Let's put a button in the middle...
        trainsTable.setWidget(2, 0, gatherButton);
        // ...and set it's column span so that it takes up the whole row.
        trainsTable.getFlexCellFormatter().setColSpan(2, 0, 3);
		 */

		//trainsTable.setWidth("600px");
		RootPanel.get().add(this.resultsLabel);
		RootPanel.get().add(this.stationEdit);
		RootPanel.get().add(trainsButton);
		RootPanel.get().add(gatherButton);
		RootPanel.get().add(trainsTable);
		
		loadStations();
		
		Timer timer = new Timer() {
			@Override
			public void run() {
				loadStations();
				loadTrains();
			}
		};
		// gather the data now repeatidly
		timer.scheduleRepeating(120000);

	}

	private void gatherTrains() {
		String url = GWT.getHostPageBaseURL() + "v1/gather";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
		TrainData.this.resultsLabel.setText("Please wait, gathering trains...");
		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// Couldn't connect to server (could be timeout, SOP violation, etc.)
					LOG.log(Level.SEVERE, "Failed to send gather request: " + exception.getMessage());
					TrainData.this.resultsLabel.setText("Failed to send gather request: " + exception.getMessage());
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {
						// Process the response in response.getText()
						JSONValue jsonValue = JSONParser.parseStrict(response.getText());
						TrainData.this.resultsLabel.setText("Successfully gathered " + jsonValue.isArray().size() + " items at " + timeFormat.format(new Date()));
					} else {
						// Handle the error.  Can get the status text from response.getStatusText()
						TrainData.this.resultsLabel.setText(response.getStatusText());
					}
				}
			});
		} catch (Exception e) {
			// Couldn't connect to server
			LOG.log(Level.SEVERE, "Failed to perform gather request: " + e.getMessage());
			TrainData.this.resultsLabel.setText("Failed to perform gather request: " + e.getMessage());
		}

	}

	private void loadStations() {
		String url = GWT.getHostPageBaseURL() + "v1/stations";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
		TrainData.this.resultsLabel.setText("Please wait, gathering stations...");
		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// Couldn't connect to server (could be timeout, SOP violation, etc.)
					LOG.log(Level.SEVERE, "Failed to send stations request: " + exception.getMessage());
					TrainData.this.resultsLabel.setText("Failed to send request: " + exception.getMessage());
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {
						// Process the response in response.getText()
						JSONValue jsonValue = JSONParser.parseStrict(response.getText());
						processStations(jsonValue);
					} else {
						// Handle the error.  Can get the status text from response.getStatusText()
						TrainData.this.resultsLabel.setText(response.getStatusText());
					}
				}
			});
		} catch (Exception e) {
			// Couldn't connect to server
			LOG.log(Level.SEVERE, "Failed to perform request: " + e.getMessage());
			TrainData.this.resultsLabel.setText("Failed to perform request: " + e.getMessage());
		}

	}

	private void loadTrains() {
		if (this.stationEdit.getItemCount() == 0 || this.stationEdit.getSelectedIndex() < 0) {
			// no good
			return;
		}
		String stationText = this.stationEdit.getItemText(this.stationEdit.getSelectedIndex());
		stationText = stationText.toUpperCase().replaceAll("\\s+", "_");
		stationText = stationText.replaceAll("\"", "");
		String url = GWT.getHostPageBaseURL() + "v1/trains?station=" + stationText;
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
		TrainData.this.resultsLabel.setText("Please wait, loading trains...");
		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// Couldn't connect to server (could be timeout, SOP violation, etc.)
					LOG.log(Level.SEVERE, "Failed to send trains request: " + exception.getMessage());
					TrainData.this.resultsLabel.setText("Failed to send request: " + exception.getMessage());
				}

				@Override
				public void onResponseReceived(Request request, Response response) {
					if (200 == response.getStatusCode()) {
						// Process the response in response.getText()
						JSONValue jsonValue = JSONParser.parseStrict(response.getText());
						processTrain(jsonValue);
					} else {
						// Handle the error.  Can get the status text from response.getStatusText()
						TrainData.this.resultsLabel.setText(response.getStatusText());
					}
				}
			});
		} catch (Exception e) {
			// Couldn't connect to server
			LOG.log(Level.SEVERE, "Failed to perform request: " + e.getMessage());
			TrainData.this.resultsLabel.setText("Failed to perform request: " + e.getMessage());
		}

	}
	
	protected void processStations(JSONValue jsonValue) {
		JSONObject object = jsonValue.isObject();
		if (null != object) {
			JSONValue stationsValue = object.get("stations");
			for (String stationName : stationsValue.toString().replaceAll("\"", "").split(",")) {
				if (null != stationName && false == stationName.isEmpty()) {
					boolean stationExists = false;
					for (int i = 0; i < this.stationEdit.getItemCount(); ++i) {
						if (this.stationEdit.getItemText(i).equals(stationName)) {
							stationExists = true;
						}
					}
					if (false == stationExists) {
						this.stationEdit.addItem(stationName);
					}
				}
			}
		}
	}

	protected void processTrain(JSONValue jsonValue) {
		this.trainsTable.removeAllRows();
		int row = 1;
		this.trainsTable.getRowFormatter().addStyleName(1, "tableHeader");
		this.trainsTable.setWidget(row, 0, new InlineLabel("Destination"));
		this.trainsTable.setWidget(row, 1, new InlineLabel("Direction"));
		this.trainsTable.setWidget(row, 2, new InlineLabel("Event Time"));
		this.trainsTable.setWidget(row, 3, new InlineLabel("Line"));
		this.trainsTable.setWidget(row, 4, new InlineLabel("Next Arrival"));
		this.trainsTable.setWidget(row, 5, new InlineLabel("Station"));
		this.trainsTable.setWidget(row, 6, new InlineLabel("Train ID"));
		this.trainsTable.setWidget(row, 7, new InlineLabel("Waiting Seconds"));
		this.trainsTable.setWidget(row, 8, new InlineLabel("Waiting Time"));
		++row;
		JSONArray array = jsonValue.isArray();
		TrainData.this.resultsLabel.setText("Successfully got " + jsonValue.isArray().size() + " events at " + timeFormat.format(new Date()));
		ArrayList<JSONObject> trainList = new ArrayList<JSONObject>();
		for (int i = 0; i < array.size(); ++i) {
			JSONObject train = array.get(i).isObject();
			if (null != train) {
				trainList.add(train);
			}
		}
		// sort the list
		Collections.sort(trainList, new Comparator<JSONObject>() {
	        @Override
	        public int compare(JSONObject o1, JSONObject o2) {
	        	JSONValue eventTime1Str = o1.get("EVENT_TIME");
	        	JSONValue eventTime2Str = o2.get("EVENT_TIME");
	        	if (null != eventTime1Str && null != eventTime2Str) {
		        	// convert these to times
		        	Date eventTimeDate1 = TrainTimeParser.getEventTimeDate(eventTime1Str.toString());
		        	Date eventTimeDate2 = TrainTimeParser.getEventTimeDate(eventTime2Str.toString());
		        	if (null != eventTimeDate1 && null != eventTimeDate2) {
		        		return (int)(eventTimeDate1.getTime() - eventTimeDate2.getTime());
		        	}
	        	}
	        	return 0;
	        }
		});
		
		for (JSONObject train : trainList) {
			JSONValue destination = train.get("DESTINATION");
			JSONValue direction = train.get("DIRECTION");
			JSONValue eventTime = train.get("EVENT_TIME");
			JSONValue eventTimes = train.get("EVENT_TIMES");
			JSONValue line = train.get("LINE");
			JSONValue nextArrival = train.get("NEXT_ARR");
			JSONValue station = train.get("STATION");
			JSONValue trainId = train.get("TRAIN_ID");
			JSONValue waitingSeconds = train.get("WAITING_SECONDS");
			JSONValue waitingTime = train.get("WAITING_TIME");
			
			String eventTimeString = calculateBoundryTimes(eventTimes);
			if (null == eventTimeString || eventTimeString.isEmpty()) {
				eventTimeString = eventTime.toString();
			}
			
			this.trainsTable.setWidget(row, 0, new InlineLabel(destination.toString()));
			this.trainsTable.setWidget(row, 1, new InlineLabel(direction.toString()));
			this.trainsTable.setWidget(row, 2, new InlineLabel(eventTimeString));
			this.trainsTable.setWidget(row, 3, new InlineLabel(line.toString()));
			this.trainsTable.setWidget(row, 4, new InlineLabel(nextArrival.toString()));
			this.trainsTable.setWidget(row, 5, new InlineLabel(station.toString()));
			this.trainsTable.setWidget(row, 6, new InlineLabel(trainId.toString()));
			this.trainsTable.setWidget(row, 7, new InlineLabel(waitingSeconds.toString()));
			this.trainsTable.setWidget(row, 8, new InlineLabel(waitingTime.toString()));
			++row;
		}

	}

	private String calculateBoundryTimes(JSONValue eventTimes) {
		String toReturn = null;
		if (null != eventTimes) {
			String eventTimesString = eventTimes.toString();
			if (null != eventTimesString) {
				Date firstTime = null;
				Date lastTime = null;
				for (String eventTime : eventTimesString.split(",")) {
					Date eventTimeDate = TrainTimeParser.getEventTimeDate(eventTime);
					if (null == eventTimeDate) {
						continue;
					}
					if (null == firstTime) {
						firstTime = eventTimeDate;
					}
					else if (null == lastTime) {
						lastTime = eventTimeDate;
					}
					else if (eventTimeDate.getTime() < firstTime.getTime()) {
						firstTime = eventTimeDate;
					}
					else if (eventTimeDate.getTime() > lastTime.getTime()) {
						lastTime = eventTimeDate;
					}
				}
				// now set the string
				if (null != firstTime) {
					toReturn = dateFormat.format(firstTime);
					if (null != lastTime) {
						toReturn += " to " + dateFormat.format(lastTime);
					}
				}
			}
		}
		return toReturn;
	}
}

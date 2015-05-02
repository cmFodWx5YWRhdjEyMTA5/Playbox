package com.alonyx.client;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fusesource.restygwt.client.Defaults;

import com.alonyx.server.JacksonResource;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TrainData implements EntryPoint {

	private FlexTable trainsTable;

	private Label resultsLabel;

	private TextArea stationEdit;
	
	public static final Logger LOG = Logger.getLogger(TrainData.class.getName());
	
	public static DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");

	@Override
	public void onModuleLoad() {

		this.resultsLabel = new Label("");
		this.resultsLabel.getElement().setId("results_label");

		this.stationEdit = new TextArea();
		this.stationEdit.getElement().setId("station_edit");
		this.stationEdit.setText("AVONDALE STATION");

		Button trainsButton = new Button("load trains");
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

	}

	private void gatherTrains() {
		String url = GWT.getHostPageBaseURL() + "v1/gather";
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));

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
						TrainData.this.resultsLabel.setText("Successfully gathered " + jsonValue.isArray().size() + " items at " + dateFormat.format(new Date()));
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

	private void loadTrains() {
		String stationText = this.stationEdit.getText();
		stationText = stationText.toUpperCase().replaceAll(" ", "_");
		String url = GWT.getHostPageBaseURL() + "v1/trains?station=" + stationText;
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));

		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// Couldn't connect to server (could be timeout, SOP violation, etc.)
					LOG.log(Level.SEVERE, "Failed to send gather request: " + exception.getMessage());
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

	protected void processTrain(JSONValue jsonValue) {
		this.trainsTable.removeAllRows();
		int row = 1;
		this.trainsTable.setWidget(row, 1, new InlineLabel("Destination"));
		this.trainsTable.setWidget(row, 2, new InlineLabel("Direction"));
		this.trainsTable.setWidget(row, 3, new InlineLabel("Event Time"));
		this.trainsTable.setWidget(row, 4, new InlineLabel("Line"));
		this.trainsTable.setWidget(row, 5, new InlineLabel("Next Arrival"));
		this.trainsTable.setWidget(row, 6, new InlineLabel("Station"));
		this.trainsTable.setWidget(row, 7, new InlineLabel("Train ID"));
		this.trainsTable.setWidget(row, 8, new InlineLabel("Waiting Seconds"));
		this.trainsTable.setWidget(row, 9, new InlineLabel("Waiting Time"));
		++row;
		JSONArray array = jsonValue.isArray();
		TrainData.this.resultsLabel.setText("Successfully got " + jsonValue.isArray().size() + " events at " + dateFormat.format(new Date()));
		for (int i = 0; i < array.size(); ++i) {
			JSONObject train = array.get(i).isObject();
			JSONValue destination = train.get("DESTINATION");
			JSONValue direction = train.get("DIRECTION");
			JSONValue eventTime = train.get("EVENT_TIME");
			JSONValue line = train.get("LINE");
			JSONValue nextArrival = train.get("NEXT_ARR");
			JSONValue station = train.get("STATION");
			JSONValue trainId = train.get("TRAIN_ID");
			JSONValue waitingSeconds = train.get("WAITING_SECONDS");
			JSONValue waitingTime = train.get("WAITING_TIME");
			
			this.trainsTable.setWidget(row, 1, new InlineLabel(destination.toString()));
			this.trainsTable.setWidget(row, 2, new InlineLabel(direction.toString()));
			this.trainsTable.setWidget(row, 3, new InlineLabel(eventTime.toString()));
			this.trainsTable.setWidget(row, 4, new InlineLabel(line.toString()));
			this.trainsTable.setWidget(row, 5, new InlineLabel(nextArrival.toString()));
			this.trainsTable.setWidget(row, 6, new InlineLabel(station.toString()));
			this.trainsTable.setWidget(row, 7, new InlineLabel(trainId.toString()));
			this.trainsTable.setWidget(row, 8, new InlineLabel(waitingSeconds.toString()));
			this.trainsTable.setWidget(row, 9, new InlineLabel(waitingTime.toString()));
			++row;
		}

	}
}

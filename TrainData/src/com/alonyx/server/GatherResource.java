package com.alonyx.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Path("/gather")
@Produces(value = MediaType.APPLICATION_JSON)
public class GatherResource extends JacksonResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getTrains() {
		TrainData[] trainList = new TrainData[0];
		List<String> stationIds = new ArrayList<String>();
		String url = "http://developer.itsmarta.com/RealtimeTrain/RestServiceNextTrain/GetRealtimeArrivals?apikey=c3917141-7b8c-4c5c-b628-0d1a06939eb7";
		try {

			Client client = Client.create();

			WebResource webResource = client.resource(url);

			ClientResponse response = webResource.accept("application/json") .get(ClientResponse.class);

			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}
			String output = response.getEntity(String.class);
			trainList = new ObjectMapper().readValue(output, TrainData[].class);
			String loggingData = "Gathered data for Train IDs: ";
			for (TrainData train : trainList) {
				// ensure this is created properly
				train.initialise();
				// log this data
				loggingData += train.getTrainId() + ",";
				// and add the train data to the station it is at
				String stationId = StationData.idifyStation(train.getStation());
				addTrainToStationData(stationId, train);
				if (!stationIds.contains(stationId)) {
					// this is a new Id, remember this
					stationIds.add(stationId);
				}
			}
			LOG.log(Level.INFO, loggingData);
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage());
		}
		// and return the response of train data gathered
		String responseString = toJsonString(trainList);
		return responseString;
	}

	public void addTrainToStationData(String station, TrainData train) {
		StationData stationData = PersistantStore.INSTANCE.getStation(station);
		boolean isAddRequired = false;
		if (null == stationData) {
			// create new station data
			stationData = new StationData(station);
			isAddRequired = true;
		}
		// add the data
		stationData.addTrainData(train);
		// persist this change
		if (isAddRequired) {
			// just add to the store
			PersistantStore.INSTANCE.addStation(stationData);
		}
	}
}

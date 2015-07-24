package com.alonyx.traindataserver.resources;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.alonyx.traindataserver.PersistantStore;
import com.alonyx.traindataserver.data.StationData;
import com.alonyx.traindataserver.data.TrainData;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

/**
 * working URL to gather data is
 * http://developer.itsmarta.com/RealtimeTrain/RestServiceNextTrain/GetRealtimeArrivals?apikey=c3917141-7b8c-4c5c-b628-0d1a06939eb7
 */

@Path("/gather")
@Produces(value = MediaType.APPLICATION_JSON)
public class GatherResource {

	@GET
    @Timed
	public TrainData[] getTrains() {
		TrainData[] trainList = new TrainData[0];
		List<String> stationIds = new ArrayList<String>();
		try {
			// create the client
			ClientConfig clientConfig = new ClientConfig();
			// register the train data we are expecting back (as an array from the API)
			clientConfig.register(TrainData[].class);
			// create the cient from this config
			Client client = ClientBuilder.newClient(clientConfig);
			 
			// now make the target call, on the RestServiceNextTrain
			WebTarget webTarget = client.target("http://developer.itsmarta.com/RealtimeTrain/RestServiceNextTrain");
			// real time arrivals
			WebTarget arrivalWebTarget = webTarget.path("GetRealtimeArrivals");
			// add the API key
			WebTarget queryWebTarget = arrivalWebTarget.queryParam("apikey", "c3917141-7b8c-4c5c-b628-0d1a06939eb7");
			// create the invocation
			Invocation.Builder invocationBuilder = queryWebTarget.request(MediaType.TEXT_PLAIN_TYPE);
			//invocationBuilder.header("some-header", "true");
			
			// and perform the get
			Response response = invocationBuilder.get();
			// check the response
			if (response.getStatus() != 200) {
				// log the error
				//LOG("Failed : HTTP error code : " + response.getStatus());
			}
			else {
				String output = response.readEntity(String.class);
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
				//LOG.log(Level.INFO, loggingData);
			}
		} catch (Exception e) {
			//LOG.log(Level.SEVERE, e.getMessage());
		}
		// and return the response of train data gathered
		return trainList;
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

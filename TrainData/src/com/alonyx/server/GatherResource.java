package com.alonyx.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.jdo.PersistenceManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import com.alonyx.shared.StationData;
import com.alonyx.shared.Train;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


@Path("/gather")
@Produces(value = MediaType.APPLICATION_JSON)
public class GatherResource extends JacksonResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getTrains() {
		Train[] trainList = new Train[0];
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
			trainList = new ObjectMapper().readValue(output, Train[].class);
			String loggingData = "Gathered data for Train IDs: ";
			for (Train train : trainList) {
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
		
		// while we are doing this, clear expired data
		for (String stationId : stationIds) {
			clearExpiredTraindata(stationId);
		}
		
		// and return the response of train data gathered
		String responseString = toJsonString(trainList);
		return responseString;
	}

	public void addTrainToStationData(String station, Train train) {
		PersistenceManager pm = getPersistenceManager();
		try {
			StationData stationData = null;
			try {
				stationData = pm.getObjectById(StationData.class, StationData.idifyStation(station));
			}
			catch (Exception e) {
				// fine, just not there yet is all
			}
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
				// just add to the pm
				pm.makePersistent(stationData);
			}
			// else update in the pm just by closing the PM in the finally call...
		}
		finally {
			pm.close();
		}
	}
	
	private void clearExpiredTraindata(String stationId) {
		PersistenceManager pm = getPersistenceManager();
		try {
			StationData stationData = null;
			try {
				stationData = pm.getObjectById(StationData.class, StationData.idifyStation(stationId));
			}
			catch (Exception e) {
				// fine, just not there yet is all
			}
			if (null != stationData) {
				// get rid of the oldest data from the station train data
				stationData.clearExpiredTrainData();
			}
			// update in the pm just by closing the PM in the finally call...
		}
		finally {
			pm.close();
		}
	}
}

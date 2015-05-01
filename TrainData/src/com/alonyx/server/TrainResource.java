package com.alonyx.server;

import javax.jdo.PersistenceManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.alonyx.shared.StationData;
import com.alonyx.shared.Train;

@Path("/trains")
@Produces(value = MediaType.APPLICATION_JSON)
public class TrainResource extends JacksonResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getTrains(@QueryParam("station") String station) {
		PersistenceManager pm = getPersistenceManager();
		StationData stationData = null;
		try {
			try {
				stationData = pm.getObjectById(StationData.class, StationData.idifyStation(station));
			}
			catch (Exception e) {
				// fine, just not there yet is all
			}
			if (null == stationData) {
				// create new station data
				stationData = new StationData();
			}
			else {
				// copy the data so we don't mess up the pm
				stationData = new StationData(stationData);
			}
		}
		finally {
			pm.close();
		}
		// return all the trains in this station
		Train[] trains = stationData.getTrains();
		return toJsonString(trains);
	}
}

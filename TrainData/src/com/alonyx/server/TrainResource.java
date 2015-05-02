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
		String returnString = "[]";
		try {
			try {
				stationData = pm.getObjectById(StationData.class, StationData.idifyStation(station));
			}
			catch (Exception e) {
				// fine, just not there yet is all
			}
			if (null != stationData) {
				Train[] trains = stationData.getTrains();
				returnString = toJsonString(trains);
			}
		}
		finally {
			pm.close();
		}
		// return all the trains in this station
		return returnString;
	}
}

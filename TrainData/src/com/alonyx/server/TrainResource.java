package com.alonyx.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/trains")
@Produces(value = MediaType.APPLICATION_JSON)
public class TrainResource extends JacksonResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getTrains(@QueryParam("station") String station) {
		StationData stationData = PersistantStore.INSTANCE.getStation(station);
		String returnString = "[]";
		if (null != stationData) {
			TrainData[] trains = stationData.getTrains();
			returnString = toJsonString(trains);
		}
		// return all the trains in this station
		return returnString;
	}
}

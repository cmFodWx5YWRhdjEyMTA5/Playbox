package com.alonyx.traindataserver.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.alonyx.traindataserver.PersistantStore;
import com.alonyx.traindataserver.data.StationData;
import com.alonyx.traindataserver.data.TrainData;
import com.codahale.metrics.annotation.Timed;

@Path("/trains")
@Produces(value = MediaType.APPLICATION_JSON)
public class TrainResource {

	@GET
    @Timed
	@Produces(MediaType.APPLICATION_JSON)
	public TrainData[] getTrains(@QueryParam("station") String station) {
		StationData stationData = PersistantStore.INSTANCE.getStation(station);
		TrainData[] trains = new TrainData[0];
		if (null != stationData) {
			trains = stationData.getTrains();
		}
		// return all the trains in this station
		return trains;
	}
}

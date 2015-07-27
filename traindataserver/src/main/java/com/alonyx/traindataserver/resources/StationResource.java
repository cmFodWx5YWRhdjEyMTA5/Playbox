package com.alonyx.traindataserver.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.alonyx.traindataserver.data.DataCollectionManager;
import com.alonyx.traindataserver.data.StationData;
import com.codahale.metrics.annotation.Timed;

@Path("/stations")
@Produces(value = MediaType.APPLICATION_JSON)
public class StationResource {

	@GET
    @Timed
	@Produces(MediaType.APPLICATION_JSON)
	public String getStations() {
		StationData[] stations = DataCollectionManager.INSTANCE.getStations();
		String returnString = "[]";
		if (null != stations && stations.length > 0) {
			returnString = "";
			for (StationData station : sortStations(stations)) {
				returnString += station.getStationId() + ",";
			}
			returnString = "{\"stations\":\"" + returnString + "\"}";
		}
		// return all the trains in this station
		return returnString;
	}
	
	private List<StationData> sortStations(StationData[] toSort) {
		ArrayList<StationData> sorted = new ArrayList<StationData>(toSort.length);
		for (StationData toAdd : toSort) {
			sorted.add(toAdd);
		}
		// sort the list prior to returning
		Collections.sort(sorted, new Comparator<StationData>() {
			public int compare(StationData o1, StationData o2) {
				return o1.getStationId().compareTo(o2.getStationId());
			}
		});
		return sorted;
	}
}

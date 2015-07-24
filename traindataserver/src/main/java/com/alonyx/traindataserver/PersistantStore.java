package com.alonyx.traindataserver;

import java.util.ArrayList;

import com.alonyx.traindataserver.data.StationData;

public class PersistantStore {
	
	public static PersistantStore INSTANCE = new PersistantStore();
	
	private long lastStore;
	
	private final ArrayList<StationData> stations;
	
	private PersistantStore() {
		this.lastStore = System.currentTimeMillis();
		this.stations = new ArrayList<StationData>();
		// get the data from the store here
		updateStations();
	}
	
	public StationData[] getStations() {
		StationData[] stationData;
		synchronized (this.stations) {
			stationData = this.stations.toArray(new StationData[this.stations.size()]);
		}
		if (System.currentTimeMillis() - this.lastStore > 3000000) {
			// time for a store update
			storeStations();
			// stop doing it next time, for a while
			this.lastStore = System.currentTimeMillis();
		}
		return stationData;
	}

	public void addStation(StationData stationData) {
		synchronized (this.stations) {
			this.stations.add(stationData);
		}
	}

	public StationData getStation(String stationId) {
		StationData toReturn = null;
		synchronized (this.stations) {
			for (StationData stationData : this.stations) {
				if (stationData.getStationId().equals(stationId)) {
					// this is the station data, return this
					toReturn = stationData;
					break;
				}
			}
		}
		return toReturn;
	}
	
	private void updateStations() {
		
	}
	
	private void storeStations() {
		ArrayList<StationData> dataToStore = new ArrayList<StationData>();
		// store copies to get out of the synchronisation loop ASAP
		synchronized (this.stations) {
			for (StationData stationData : this.stations) {
				dataToStore.add(new StationData(stationData));
			}
		}
		
	}
}

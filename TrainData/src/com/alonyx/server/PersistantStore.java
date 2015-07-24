package com.alonyx.server;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

public class PersistantStore {
	
	public static PersistantStore INSTANCE = new PersistantStore();
	
	private long lastStore;
	
	private final ArrayList<StationData> stations;
	
	private final PersistenceManagerFactory factory;

	public static final Logger LOG = Logger.getLogger(PersistantStore.class.getName());
	
	private PersistantStore() {
		this.factory = JDOHelper.getPersistenceManagerFactory("transactions-optional");
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
		PersistenceManager pm = factory.getPersistenceManager();
		try {
			try {
				Extent<StationData> extent = pm.getExtent(StationData.class, false);
				synchronized (this.stations) {
					this.stations.clear();
					for (StationData station : extent) {
						this.stations.add(station);
					}
				}
			}
			catch (Exception e) {
				// fine, just not there yet is all
			}
		}
		finally {
			try {
				pm.close();
			}
			catch (Throwable e) {
				LOG.severe("Error closing persistant store when updating stations: " + e.getMessage());
			}
		}
	}
	
	private void storeStations() {
		ArrayList<StationData> dataToStore = new ArrayList<StationData>();
		// store copies to get out of the synchronisation loop ASAP
		synchronized (this.stations) {
			for (StationData stationData : this.stations) {
				dataToStore.add(new StationData(stationData));
			}
		}
		for (StationData stationData : dataToStore) {
			PersistenceManager pm = factory.getPersistenceManager();
			try {
				StationData databaseData = pm.getObjectById(StationData.class, StationData.idifyStation(stationData.getStationId()));
				if (null != databaseData) {
					// set all the database data from the one we have here
					databaseData.setTrainData(stationData.getTrains());
				}
				else {
					// add the data
					pm.makePersistent(stationData);
				}
			}
			catch (Exception e) {
				// fine, just not there yet is all
			}
			finally {
				try {
					pm.close();
				}
				catch (Throwable e) {
					LOG.severe("Error closing persistant store when storing stations: " + e.getMessage());
				}
			}
		}
	}
}

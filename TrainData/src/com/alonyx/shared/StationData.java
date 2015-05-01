package com.alonyx.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class StationData implements Serializable {
	
	private static final long serialVersionUID = -6594664408481087593L;
	
	@PrimaryKey
	private String stationId;
	@Persistent
	private List<Train> trains;

	public static String idifyStation(String stationId) {
		return stationId.replaceAll(" ", "_");
	}
	
	public StationData() {
		this.stationId = "";
		this.trains = new ArrayList<Train>();
	}

	public StationData(String stationId) {
		this();
		this.stationId = idifyStation(stationId);
		this.trains = new ArrayList<Train>();
	}
	
	public StationData(StationData toCopy) {
		this();
		this.stationId = toCopy.stationId;
		this.trains = new ArrayList<Train>();
		synchronized (this.trains) {
			for (Train trainToCopy : toCopy.trains) {
				this.trains.add(new Train(trainToCopy));
			}
		}
	}

	public String getStationId() {
		return this.stationId;
	}

	public void addTrainData(Train train) {
		synchronized (this.trains) {
			this.trains.add(train);
		}
	}

	public Train[] getTrains() {
		synchronized (this.trains) {
			return this.trains.toArray(new Train[this.trains.size()]);
		}
	}

	public void clearExpiredTrainData() {
		// TODO better data expiry, for now just keep the trains list at 20...
		synchronized (this.trains) {
			while (this.trains.size() > 20) {
				this.trains.remove(0);
			}
		}
	}
}

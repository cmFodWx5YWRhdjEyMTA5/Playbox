package com.alonyx.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
	private List<TrainData> trainData;

	public static String idifyStation(String stationId) {
		stationId = stationId.replaceAll("\\s+", "_");
		stationId = stationId.replaceAll("\"", "");
		return stationId;
	}
	
	public StationData() {
		this.stationId = "";
		this.trainData = new ArrayList<TrainData>();
	}

	public StationData(String stationId) {
		this();
		this.stationId = idifyStation(stationId);
		this.trainData = new ArrayList<TrainData>();
	}
	
	public StationData(StationData toCopy) {
		this();
		this.stationId = toCopy.stationId;
		this.trainData = new ArrayList<TrainData>();
		synchronized (this.trainData) {
			for (TrainData trainToCopy : toCopy.trainData) {
				this.trainData.add(new TrainData(trainToCopy));
			}
		}
	}

	public String getStationId() {
		return this.stationId;
	}
	
	public void setTrainData(TrainData[] trains) {
		synchronized (this.trainData) {
			this.trainData.clear();
			for (TrainData train : trains) {
				this.trainData.add(train);
			}
		}
		// but clear the expired data
		clearExpiredTrainData();
	}

	public void addTrainData(TrainData train) {
		synchronized (this.trainData) {
			// can we merge this into an exising one?
			boolean isMerged = false;
			for (TrainData extantTrain : this.trainData) {
				if (extantTrain.canMergeTrainEvent(train)) {
					// merge this
					extantTrain.mergeTrainEvent(train);
					isMerged = true;
					break;
				}
			}
			if (false == isMerged) {
				this.trainData.add(train);
			}
		}
	}
	
	private void sortTrains() {
		// sort the list prior to returning
		synchronized (this.trainData) {
			Collections.sort(this.trainData, new Comparator<TrainData>() {
		        @Override
		        public int compare(TrainData o1, TrainData o2) {
		        	Date eventTimeDate1 = o1.getEventTimeDate();
		        	Date eventTimeDate2 = o2.getEventTimeDate();
		        	if (null != eventTimeDate1 && null != eventTimeDate2) {
		        		return (int)(eventTimeDate1.getTime() - eventTimeDate2.getTime());
		        	}
		        	return 0;
		        }
			});
		}
	}

	public TrainData[] getTrains() {
		synchronized (this.trainData) {
			sortTrains();
			// and return as an array
			return this.trainData.toArray(new TrainData[this.trainData.size()]);
		}
	}

	public void clearExpiredTrainData() {
		// data expiry, for now just keep the trains list at 5...
		synchronized (this.trainData) {
			sortTrains();
			while (this.trainData.size() > 5) {
				this.trainData.remove(0);
			}
		}
	}
}

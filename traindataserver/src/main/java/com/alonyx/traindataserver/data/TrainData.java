package com.alonyx.traindataserver.data;


import com.alonyx.traindataserver.TrainTimeParser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainData {

	@JsonProperty("DESTINATION")
    private String destination;

	@JsonProperty("DIRECTION")
    private String direction;
	
	@JsonProperty("EVENT_TIME")
    private String eventTime;
	
	@JsonProperty("EVENT_TIMES")
    private String eventTimes;
	
	@JsonProperty("LINE")
    private String line;
	
	@JsonProperty("NEXT_ARR")
    private String nextArrival;
	
	@JsonProperty("STATION")
    private String station;
	
	@JsonProperty("TRAIN_ID")
    private int trainId;
	
	@JsonProperty("WAITING_SECONDS")
    private int waitingSeconds;
	
	@JsonProperty("WAITING_TIME")
    private String waitingTime;

    public TrainData() {
    }

	public void initialise() {
		getEventTimes();
	}
    
	public TrainData(String destination, String direction, String eventTime, String line, String nextArrival, 
			String station, int trainId, int waitingSeconds, String waitingTime) {
		this.destination = destination;
		this.direction = direction;
		this.eventTime = eventTime;
		this.eventTimes = eventTime + ",";
		this.line = line;
		this.nextArrival = nextArrival;
		this.station = station;
		this.trainId = trainId;
		this.waitingSeconds = waitingSeconds;
		this.waitingTime = waitingTime;
	}

	public TrainData(TrainData toCopy) {
		this.destination = toCopy.destination;
		this.direction = toCopy.direction;
		this.eventTime = toCopy.eventTime;
		this.eventTimes = toCopy.eventTimes;
		this.line = toCopy.line;
		this.nextArrival = toCopy.nextArrival;
		this.station = toCopy.station;
		this.trainId = toCopy.trainId;
		this.waitingSeconds = toCopy.waitingSeconds;
		this.waitingTime = toCopy.waitingTime;
	}
	
	public boolean canMergeTrainEvent(TrainData event) {
		// if the passed train data is a lot like us then we can combine the event into this one
		if (this.destination.equals(event.destination) &&
				this.direction.equals(event.direction) &&
				this.line.equals(event.line) &&
				this.trainId == event.trainId) {
			// all the basic data is the same, are the events close in time?
			return Math.abs(this.getEventTimeDate().getTime() - event.getEventTimeDate().getTime()) < 600000;
		}
		else {
			// basic data is not the same
			return false;
		}
	}
	
	public void mergeTrainEvent(TrainData event) {
		// update the newer data
		if (event.getEventTimeDate().getTime() > this.getEventTimeDate().getTime()) {
			// the event is newer, use some of it's data
			this.eventTime = event.eventTime;
			this.nextArrival = event.nextArrival;
			this.waitingSeconds = event.waitingSeconds;
			this.waitingTime = event.waitingTime;
		}
		else {
			// fine, leave us as we are, but add the event time to the list of event times
		}
		// add to the event times
		this.eventTimes = getEventTimes() + event.eventTime + ",";
	}
	
	public String getEventTimes() {
		if (null == this.eventTimes || this.eventTimes.isEmpty()) {
			// put in the first event time
			this.eventTimes = this.eventTime + ",";
		}
		// return the list of event times
		return this.eventTimes;
	}

	public String getDestination() {
		return destination;
	}

	public String getDirection() {
		return direction;
	}

	public String getEventTime() {
		return eventTime;
	}

	public String getLine() {
		return line;
	}

	public String getNextArrival() {
		return nextArrival;
	}

	public String getStation() {
		return station;
	}

	public int getTrainId() {
		return trainId;
	}

	public int getWaitingSeconds() {
		return waitingSeconds;
	}

	public String getWaitingTime() {
		return waitingTime;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public void setNextArrival(String nextArrival) {
		this.nextArrival = nextArrival;
	}

	public void setStation(String station) {
		this.station = station;
	}

	public void setTrainId(int trainId) {
		this.trainId = trainId;
	}

	public void setWaitingSeconds(int waitingSeconds) {
		this.waitingSeconds = waitingSeconds;
	}

	public void setWaitingTime(String waitingTime) {
		this.waitingTime = waitingTime;
	}
    
    // getter / setter goes here
	public Date getEventTimeDate() {
		return TrainTimeParser.getEventTimeDate(this.eventTime);
	}
    
    // getter / setter goes here
	public Date getNextArrivalDate() {
		return TrainTimeParser.getNextArrivalDate(this.nextArrival);
	}
}

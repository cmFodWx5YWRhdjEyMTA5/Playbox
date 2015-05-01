package com.alonyx.shared;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Train implements Serializable {
	
	private static final long serialVersionUID = 1591605955139192213L;
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	@Persistent
	@JsonProperty("DESTINATION")
    private String destination;

	@Persistent
	@JsonProperty("DIRECTION")
    private String direction;
	
	@Persistent
	@JsonProperty("EVENT_TIME")
    private String eventTime;
	
	@Persistent
	@JsonProperty("LINE")
    private String line;
	
	@Persistent
	@JsonProperty("NEXT_ARR")
    private String nextArrival;
	
	@Persistent
	@JsonProperty("STATION")
    private String station;
	
	@Persistent
	@JsonProperty("TRAIN_ID")
    private int trainId;
	
	@Persistent
	@JsonProperty("WAITING_SECONDS")
    private int waitingSeconds;
	
	@Persistent
	@JsonProperty("WAITING_TIME")
    private String waitingTime;

    public Train() {
    }

	public Train(Train toCopy) {
		this.destination = toCopy.destination;
		this.direction = toCopy.direction;
		this.eventTime = toCopy.eventTime;
		this.line = toCopy.line;
		this.nextArrival = toCopy.nextArrival;
		this.station = toCopy.station;
		this.trainId = toCopy.trainId;
		this.waitingSeconds = toCopy.waitingSeconds;
		this.waitingTime = toCopy.waitingTime;
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
    
}

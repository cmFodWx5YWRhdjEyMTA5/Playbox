package com.alonyx.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class TrainList {
	
    @JsonProperty("trains")
	private String trains = "";
	
	public TrainList(String trainString) {
		this.trains = trainString;
	}
	
	public void addTrain(TrainData train) {
		this.trains += train.toString() + ";";
	}
	
	public String toString() {
		return this.trains;
	}
	
	public List<TrainData> getTrains() {
		List<TrainData> list = new ArrayList<TrainData>();
		
		return list;
	}
}

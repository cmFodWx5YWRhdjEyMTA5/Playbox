package com.alonyx.shared;

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
	
	public void addTrain(Train train) {
		this.trains += train.toString() + ";";
	}
	
	public String toString() {
		return this.trains;
	}
	
	public List<Train> getTrains() {
		List<Train> list = new ArrayList<Train>();
		
		return list;
	}
}

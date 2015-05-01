package com.alonyx;

import org.codehaus.jackson.annotate.JsonProperty;

public class Fields {

	@JsonProperty("foo")
	private String fooVal;
	
	@JsonProperty("bar")
	private String barVal;
	
	public Fields() {
		
	}
	
	public void setFooValue(String foo) {
		this.fooVal = foo;
	}
	
	public void setBarValue(String bar) {
		this.barVal = bar;
	}

	public String getFoo() {
		return this.fooVal;
	}

	public String getBar() {
		return this.barVal;
	}

}
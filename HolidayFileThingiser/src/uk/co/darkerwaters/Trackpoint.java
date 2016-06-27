package uk.co.darkerwaters;

import java.util.Date;

public class Trackpoint {

	private double latitude;
	private double longitude;
	private double elevation;
	private Date time;

	public static Trackpoint fromWGS84(double lat, double lon, double ele, Date time) {
		Trackpoint point = new Trackpoint();
		point.latitude = lat;
		point.longitude = lon;
		point.elevation = ele;
		point.time = time;
		return point;
	}
	
	@Override
	public String toString() {
		return "Lat: " + this.latitude + 
			   " Lon: " + this.longitude +
			   " Ele: " + this.elevation +
			   " time: " + this.time;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getElevation() {
		return elevation;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
	

}

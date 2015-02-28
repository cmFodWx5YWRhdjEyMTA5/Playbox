package uk.co.darkerwaters.client.tracks;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TrackPointServiceAsync {
	public void addTrackPoint(TrackPointData point, AsyncCallback<TrackPointData> async);
	
	public void removeTrackPoint(TrackPointData point, AsyncCallback<Void> async);

	public void getTrackPoints(AsyncCallback<TrackPointData[]> async);
	
	public void getTrackPoints(String fromDayDate, String toDayDate, AsyncCallback<TrackPointData[]> async);
}

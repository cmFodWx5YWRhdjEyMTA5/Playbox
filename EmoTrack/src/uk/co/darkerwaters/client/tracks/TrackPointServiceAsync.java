package uk.co.darkerwaters.client.tracks;

import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TrackPointServiceAsync {
	public void addTrackPoint(TrackPointData point, AsyncCallback<Void> async);
	
	public void removeTrackPoint(TrackPointData point, AsyncCallback<Void> async);

	public void getTrackPoints(AsyncCallback<TrackPointData[]> async);
	
	public void getTrackPoints(Date fromDate, Date toDate, AsyncCallback<TrackPointData[]> async);
}

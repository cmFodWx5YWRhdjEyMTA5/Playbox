package uk.co.darkerwaters.client.tracks;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TrackPointServiceAsync {
	public void addTrackPoint(TrackPoint point, AsyncCallback<Void> async);

	public void getTrackPoints(AsyncCallback<TrackPoint[]> async);
}

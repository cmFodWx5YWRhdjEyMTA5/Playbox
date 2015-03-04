package uk.co.darkerwaters.client.graph;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.tracks.TrackPointService;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import uk.co.darkerwaters.shared.StatsResultsData;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DataGraphsDataHandler {
	
	private final TrackPointServiceAsync trackService = GWT.create(TrackPointService.class);
	
	private final String userId;
	
	public DataGraphsDataHandler(String userId) {
		this.userId = userId;
	}

	public void getTrackPoints(String fromDate, String toDate, AsyncCallback<TrackPointData[]> asyncCallback) {
		if (null == userId) {
			trackService.getTrackPoints(fromDate, toDate, asyncCallback);
		}
		else {
			trackService.getTrackPoints(this.userId, fromDate, toDate, asyncCallback);
		}
	}

	public void removeTrackPoint(TrackPointData point, AsyncCallback<Void> asyncCallback) {
		if (null == userId) {
			trackService.removeTrackPoint(point, asyncCallback);
		}
		else {
			EmoTrack.LOG.severe("User trying to delete another users data, stopping this");
		}
	}

	public void getStatsResults(AsyncCallback<StatsResultsData> asyncCallback) {
		if (null == userId) {
			trackService.getStatsResults(asyncCallback);
		}
		else {
			trackService.getStatsResults(this.userId, asyncCallback);
		}
	}

}

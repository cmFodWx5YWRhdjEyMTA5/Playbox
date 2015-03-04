package uk.co.darkerwaters.client.tracks;

import uk.co.darkerwaters.shared.StatsResultsData;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TrackPointServiceAsync {
	public void addTrackPoint(TrackPointData point, AsyncCallback<TrackPointData> async);
	
	public void removeTrackPoint(TrackPointData point, AsyncCallback<Void> async);
	
	public void purgeAllData(AsyncCallback<Integer> async);

	public void getTrackPoints(AsyncCallback<TrackPointData[]> async);
	
	public void getTrackPoints(String fromDayDate, String toDayDate, AsyncCallback<TrackPointData[]> async);
	
	public void getStatsResults(AsyncCallback<StatsResultsData> async);
	
	public void getTrackPoints(String userId, String fromDayDate, String toDayDate, AsyncCallback<TrackPointData[]> async);
	
	public void getStatsResults(String userId, AsyncCallback<StatsResultsData> async);
}

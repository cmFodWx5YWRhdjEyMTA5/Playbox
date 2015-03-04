package uk.co.darkerwaters.client.tracks;

import uk.co.darkerwaters.client.login.NotLoggedInException;
import uk.co.darkerwaters.shared.StatsResultsData;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("track")
public interface TrackPointService extends RemoteService {
	public TrackPointData addTrackPoint(TrackPointData point) throws NotLoggedInException;

	public void removeTrackPoint(TrackPointData point) throws NotLoggedInException;
	
	public Integer purgeAllData() throws NotLoggedInException;

	public TrackPointData[] getTrackPoints() throws NotLoggedInException;
	
	public TrackPointData[] getTrackPoints(String fromDayDate, String toDayDate) throws NotLoggedInException;
	
	public StatsResultsData getStatsResults() throws NotLoggedInException;
	
	public TrackPointData[] getTrackPoints(String userId, String fromDayDate, String toDayDate) throws NotLoggedInException;
	
	public StatsResultsData getStatsResults(String userId) throws NotLoggedInException;
}

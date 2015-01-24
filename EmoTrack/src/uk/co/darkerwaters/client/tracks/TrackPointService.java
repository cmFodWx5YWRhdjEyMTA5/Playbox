package uk.co.darkerwaters.client.tracks;

import uk.co.darkerwaters.client.login.NotLoggedInException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("track")
public interface TrackPointService extends RemoteService {
	public void addTrackPoint(TrackPointData point) throws NotLoggedInException;

	public void removeTrackPoint(TrackPointData point) throws NotLoggedInException;

	public TrackPointData[] getTrackPoints() throws NotLoggedInException;
	
	public TrackPointData[] getTrackPoints(String fromDayDate, String toDayDate) throws NotLoggedInException;
}

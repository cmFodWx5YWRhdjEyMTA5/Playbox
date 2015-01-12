package uk.co.darkerwaters.client.tracks;

import uk.co.darkerwaters.client.login.NotLoggedInException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("track")
public interface TrackPointService extends RemoteService {
	public void addTrackPoint(TrackPoint point) throws NotLoggedInException;

	public TrackPoint[] getTrackPoints() throws NotLoggedInException;
}

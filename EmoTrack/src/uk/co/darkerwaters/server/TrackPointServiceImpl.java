package uk.co.darkerwaters.server;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import uk.co.darkerwaters.client.login.NotLoggedInException;
import uk.co.darkerwaters.client.tracks.TrackPoint;
import uk.co.darkerwaters.client.tracks.TrackPointService;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TrackPointServiceImpl extends RemoteServiceServlet implements TrackPointService {

	private static final long serialVersionUID = 1607169848971697289L;
	private static final Logger LOG = Logger.getLogger(TrackPointServiceImpl.class.getName());
	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");

	public void addTrackPoint(TrackPoint point) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.makePersistent(point);
		} finally {
			pm.close();
		}
	}

	public TrackPoint[] getTrackPoints() throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		List<TrackPoint> trackPoints = new ArrayList<TrackPoint>();
		try {
			Query q = pm.newQuery(TrackPoint.class, "user == u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("createDate");
			trackPoints = (List<TrackPoint>) q.execute(getUser());
		} 
		catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get track points:" + e.getMessage());
		}
		finally {
			pm.close();
		}
		return (TrackPoint[]) trackPoints.toArray(new TrackPoint[0]);
	}

	private void checkLoggedIn() throws NotLoggedInException {
		if (getUser() == null) {
			throw new NotLoggedInException("Not logged in.");
		}
	}

	private User getUser() {
		UserService userService = UserServiceFactory.getUserService();
		return userService.getCurrentUser();
	}

	private PersistenceManager getPersistenceManager() {
		return PMF.getPersistenceManager();
	}
}

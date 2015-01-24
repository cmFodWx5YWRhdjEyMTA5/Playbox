package uk.co.darkerwaters.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import uk.co.darkerwaters.client.login.NotLoggedInException;
import uk.co.darkerwaters.client.tracks.TrackPoint;
import uk.co.darkerwaters.client.tracks.TrackPointData;
import uk.co.darkerwaters.client.tracks.TrackPointService;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TrackPointServiceImpl extends RemoteServiceServlet implements TrackPointService {

	private static final long serialVersionUID = 1607169848971697289L;
	private static final Logger LOG = Logger.getLogger(TrackPointServiceImpl.class.getName());
	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	
	public static SimpleDateFormat dayDate = new SimpleDateFormat("yyyy-MM-dd");

	public void addTrackPoint(TrackPointData point) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			// persist track data that includes the user and an ID
			pm.makePersistent(new TrackPoint(getUser(), point));
		} finally {
			pm.close();
		}
	}

	public void removeTrackPoint(TrackPointData point) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(TrackPoint.class, "user == u && trackDate == date");
			q.declareParameters("com.google.appengine.api.users.User u, java.util.Date date");
			Date deleteDate = point.getTrackDate();
			Object executeResult = q.execute(getUser(), deleteDate);
			if (null != executeResult && executeResult instanceof List<?>) {
				// delete the results from the execute result
				List<?> executeList = (List<?>) executeResult;
				for (Object item : executeList) {
					if (null != item && item instanceof TrackPoint) {
						// result is as expected
						TrackPoint trackPoint = (TrackPoint)item;
						// delete everything like this name (ignoring case)
						if (trackPoint.getTrackDate().equals(deleteDate)) {
							pm.deletePersistent(trackPoint);
						}
						else {
							// query should not have returned this
							LOG.log(Level.WARNING, "removeTrackPoint returned an item with incorrect date:" + trackPoint);
						}
					}
					else {
						// not expected
						LOG.log(Level.WARNING, "removeTrackPoint not returning expected object type:" + item);
					}
				}
			}
			else {
				LOG.log(Level.WARNING, "removeTrackPoint not returning expected execution object type:" + executeResult);
			}
		} finally {
			pm.close();
		}
	}

	public TrackPointData[] getTrackPoints() throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		TrackPointData[] toReturn = new TrackPointData[0];
		try {
			Query q = pm.newQuery(TrackPoint.class, "user == u");
			q.declareParameters("com.google.appengine.api.users.User u");
			q.setOrdering("trackDate");
			Object executeResult = q.execute(getUser());
			if (null != executeResult && executeResult instanceof List<?>) {
				// convert this data to the data to return (client accessible)
				List<?> executeList = (List<?>) executeResult;
				toReturn = new TrackPointData[executeList.size()];
				int i = 0;
				for (Object item : executeList) {
					if (null != item && item instanceof TrackPoint) {
						// result is as expected
						toReturn[i++] = ((TrackPoint)item).getData();
					}
					else {
						// not expected
						LOG.log(Level.WARNING, "getTrackPoints not returning expected object type:" + item);
						toReturn[i++] = null;
					}
				}
			}
			else {
				LOG.log(Level.WARNING, "getTrackPoints not returning expected execution object type:" + executeResult);
			}
		} 
		catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get track points:" + e.getMessage());
		}
		finally {
			pm.close();
		}
		return toReturn;
	}

	public TrackPointData[] getTrackPoints(String fromDayDate, String toDayDate) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		TrackPointData[] toReturn = new TrackPointData[0];
		try {
			Query q;
			Object executeResult;
			Date toDate = (toDayDate == null || toDayDate.isEmpty()) ? null : dayDate.parse(toDayDate);
			Date fromDate = fromDayDate == null ? null : dayDate.parse(fromDayDate);
			if (toDate == null) {
				// just do the from date
				q = pm.newQuery(TrackPoint.class, "user == u && trackDate >= fromDate");
				q.declareParameters("com.google.appengine.api.users.User u, java.util.Date fromDate");
				q.setOrdering("trackDate");
				executeResult = q.execute(getUser(), fromDate);
			}
			else {
				// do from and to
				q = pm.newQuery(TrackPoint.class, "user == u && trackDate >= fromDate && trackDate <= toDate");
				q.declareParameters("com.google.appengine.api.users.User u, java.util.Date fromDate, java.util.Date toDate");
				q.setOrdering("trackDate");
				executeResult = q.execute(getUser(), fromDate, toDate);
			}
			if (null != executeResult && executeResult instanceof List<?>) {
				// convert this data to the data to return (client accessible)
				List<?> executeList = (List<?>) executeResult;
				toReturn = new TrackPointData[executeList.size()];
				int i = 0;
				for (Object item : executeList) {
					if (null != item && item instanceof TrackPoint) {
						// result is as expected
						toReturn[i++] = ((TrackPoint)item).getData();
					}
					else {
						// not expected
						LOG.log(Level.WARNING, "getTrackPoints not returning expected object type:" + item);
						toReturn[i++] = null;
					}
				}
			}
			else {
				LOG.log(Level.WARNING, "getTrackPoints not returning expected execution object type:" + executeResult);
			}
		} 
		catch (Exception e) {
			LOG.log(Level.SEVERE, "Failed to get track points:" + e.getMessage());
		}
		finally {
			pm.close();
		}
		return toReturn;
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

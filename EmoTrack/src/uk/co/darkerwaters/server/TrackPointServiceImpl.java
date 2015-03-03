package uk.co.darkerwaters.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.login.NotLoggedInException;
import uk.co.darkerwaters.client.tracks.TrackPointService;
import uk.co.darkerwaters.shared.StatsResultsData;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class TrackPointServiceImpl extends RemoteServiceServlet implements TrackPointService {

	private static final long serialVersionUID = 1607169848971697289L;
	private static final Logger LOG = Logger.getLogger(TrackPointServiceImpl.class.getName());
	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	private static final Integer K_THRESHOLD_NONE = new Integer(2);
	
	public static SimpleDateFormat dayDate = new SimpleDateFormat("yyyy-MM-dd");
	
	public static SimpleDateFormat monthDate = new SimpleDateFormat("yyyy-MM");
	public static SimpleDateFormat weekDate = new SimpleDateFormat("yyyy-ww");

	public TrackPointData addTrackPoint(TrackPointData point) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			// persist track data that includes the user and an ID, first get any data at this date to merge the two
			Object executeResult = getPointData(pm, point);
			TrackPointData extantPoint = null;
			if (null != executeResult && executeResult instanceof List<?>) {
				// delete the results from the execute result
				List<?> executeList = (List<?>) executeResult;
				Date pointDate = point.getTrackDate();
				for (Object item : executeList) {
					if (null != item && item instanceof TrackPoint) {
						// result is as expected
						TrackPoint trackPoint = (TrackPoint)item;
						// delete everything like this name (ignoring case)
						if (trackPoint.getTrackDate().equals(pointDate)) {
							if (null == extantPoint) {
								extantPoint = trackPoint.getData();
							}
							// delete this old data, are to replace now
							pm.deletePersistent(trackPoint);
						}
						else {
							// query should not have returned this
							LOG.log(Level.WARNING, "addTrackPoint returned an item with incorrect date:" + trackPoint);
						}
					}
					else {
						// not expected
						LOG.log(Level.WARNING, "addTrackPoint not returning expected object type:" + item);
					}
				}
			}
			else {
				LOG.log(Level.WARNING, "addTrackPoint not returning expected execution object type:" + executeResult);
			}
			if (null != extantPoint) {
				// merge the new data to replace this existing data
				String event = point.getEvent();
				if (null != event && false == event.isEmpty()) {
					// there is an event in the new data, set it on the one to replace it with
					extantPoint.setEvent(event);
				}
				// and overwrite any data with that in the new point data, create a list of the extant data
				HashMap<String, Integer> values = new HashMap<String, Integer>(); 
				String[] valuesNames = extantPoint.getValuesNames();
				Integer[] valuesValues = extantPoint.getValuesValues();
				for (int i = 0; i < valuesNames.length && i < valuesValues.length; ++i) {
					values.put(valuesNames[i], valuesValues[i]);
				}
				// overwrite with the new ones
				valuesNames = point.getValuesNames();
				valuesValues = point.getValuesValues();
				for (int i = 0; i < valuesNames.length && i < valuesValues.length; ++i) {
					values.put(valuesNames[i], valuesValues[i]);
				}
				// clear the data
				extantPoint.clearValues();
				// and put the new ones in
				for (Entry<String, Integer> value : values.entrySet()) {
					extantPoint.addValue(value.getKey(), value.getValue());
				}
				// replace the data to make persistant with this newly merged data
				point = extantPoint;
			}
			// make this point persistent
			pm.makePersistent(new TrackPoint(getUser(), point));
		} finally {
			pm.close();
		}
		// return the new point added
		return point;
	}

	public void removeTrackPoint(TrackPointData point) throws NotLoggedInException {
		checkLoggedIn();
		PersistenceManager pm = getPersistenceManager();
		try {
			Object executeResult = getPointData(pm, point);
			if (null != executeResult && executeResult instanceof List<?>) {
				// delete the results from the execute result
				List<?> executeList = (List<?>) executeResult;
				Date pointDate = point.getTrackDate();
				for (Object item : executeList) {
					if (null != item && item instanceof TrackPoint) {
						// result is as expected
						TrackPoint trackPoint = (TrackPoint)item;
						// delete everything like this name (ignoring case)
						if (trackPoint.getTrackDate().equals(pointDate)) {
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

	private Object getPointData(PersistenceManager pm, TrackPointData point) {
		Query q = pm.newQuery(TrackPoint.class, "user == u && trackDate == date");
		q.declareParameters("com.google.appengine.api.users.User u, java.util.Date date");
		Date pointDate = point.getTrackDate();
		return q.execute(getUser(), pointDate);
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

	public StatsResultsData getStatsResults() throws NotLoggedInException {
		// create a list of montly values to get data for, this month, less six months...
		Date to = new Date();
		GregorianCalendar calendar = new GregorianCalendar();
	    calendar.add(Calendar.MONTH, -6);
	    calendar.set(Calendar.DATE, 1);
		Date from = calendar.getTime();
		// get these as search strings
		String fromDayDate = dayDate.format(from);
		String toDayDate = dayDate.format(to);
		ArrayList<String> seriesEncountered = new ArrayList<String>();
		TrackPointData[] trackPoints = getTrackPoints(fromDayDate, toDayDate);
		HashMap<String, Integer> numberOfNoneValues = new HashMap<String, Integer>();
		HashMap<String, ArrayList<Integer>> monthlyValues = new HashMap<String, ArrayList<Integer>>();
		HashMap<String, ArrayList<Integer>> weeklyValues = new HashMap<String, ArrayList<Integer>>();
		for (TrackPointData point : trackPoints) {
			if (null == point) {
				// ignore
				continue;
			}
			// ok, so we can process this
			String[] valuesNames = point.getValuesNames();
			Integer[] valuesValues = point.getValuesValues();
			for (int i = 0; i < valuesNames.length && i < valuesValues.length; ++i) {
				String title = valuesNames[i];
				Integer value = valuesValues[i];
				if (null == title || null == value) {
					// ignore
					continue;
				}
				if (TrackPointData.IsSleepKey(title)) {
					// is a sleep value
				}
				else if (TrackPointData.IsActivityKey(title)) {
					// is an activity value
				}
				else {
					// is a value we are tracking
					if (value < K_THRESHOLD_NONE) {
						// increment the count of none values
						Integer count = numberOfNoneValues.get(title);
						if (null == count) {
							count = new Integer(1);
						}
						else {
							count = new Integer(count + 1);
						}
						numberOfNoneValues.put(title, count);
					}
				}
				if (false == seriesEncountered.contains(title)) {
					// this is a new series, populate the maps for this
					populateMap(Calendar.MONTH, monthDate, title, monthlyValues);
					populateMap(Calendar.WEEK_OF_YEAR, weekDate, title, weeklyValues);
					seriesEncountered.add(title);
				}
				// format the monthly string for this data
				String monthDataKey = monthDate.format(point.getTrackDate()) + " " + title;
				ArrayList<Integer> monthKeyValues = monthlyValues.get(monthDataKey);
				if (null != monthKeyValues) {
					// add the data to this monthly tally
					monthKeyValues.add(value);
				}
				else {
					//oops
					EmoTrack.LOG.severe("Encountered a month not collating stats for: " + monthDataKey);
				}
				
				// format the weekly string for this data
				String weekDataKey = weekDate.format(point.getTrackDate()) + " " + title;
				ArrayList<Integer> weekKeyValues = weeklyValues.get(weekDataKey);
				if (null != weekKeyValues) {
					// add the data to this weekly tally
					weekKeyValues.add(value);
				}
			}
		}
		StatsResultsData results = new StatsResultsData();
		// add the instances below the threshold
		for (Entry<String, Integer> entry : numberOfNoneValues.entrySet()) {
			results.addNumberOfNone(entry.getKey(), entry.getValue().intValue());
		}
		// add monthly averages, in order please
		List<String> sortedList = new ArrayList<String>();
		sortedList.addAll(monthlyValues.keySet());
		Collections.sort(sortedList);
		for (String key : sortedList) {
			// for each monthly entry, add the average to the results
			int total = 0;
			ArrayList<Integer> values = monthlyValues.get(key);
			for (Integer value : values) {
				total += value.intValue();
			}
			results.addMonthlyAverage(key, values.size() == 0 ? 0 : (total / (float)values.size()));
		}
		// add weekly averages, in order please
		sortedList.clear();
		sortedList.addAll(weeklyValues.keySet());
		Collections.sort(sortedList);
		for (String key : sortedList) {
			// for each weekly entry, add the average to the results
			int total = 0;
			ArrayList<Integer> values = weeklyValues.get(key);
			for (Integer value : values) {
				total += value.intValue();
			}
			results.addWeeklyAverage(key, values.size() == 0 ? 0 : (total / (float)values.size()));
		}
		
		return results;
	}
	
	private void populateMap(int field, SimpleDateFormat format, String seriesTitle, HashMap<String, ArrayList<Integer>> map) {
		// populate the map with the month keys to collate data for this series
		//String keys = "";
		for (int i = -6; i <= 0; ++i) {
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
		    calendar.add(field, i);
		    String key = format.format(calendar.getTime()) + " " + seriesTitle;
		    //keys += key + " ";
		    map.put(key, new ArrayList<Integer>());
		}
		//EmoTrack.LOG.info("Added " + keys);
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

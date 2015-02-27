package uk.co.darkerwaters.client;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import uk.co.darkerwaters.client.tracks.TrackPointData;

public class TrackPointGraphDataHandler implements DataGraph.DataHandler<Date, Integer> {
	
	public static DateTimeFormat K_DATE_FORMAT = DateTimeFormat.getFormat("MMM-dd");
	
	public static enum Type {
		emotion,
		sleep,
		activity
	}
	private final Type type;
	
	private Date dateStart = null;
	
	private Date dateEnd = null;

	private DataGraph<Date, Integer> graph = null;
	
	public TrackPointGraphDataHandler(Type type) {
		this.type = type;
	}
	
	public void setDateRange(Date start, Date end) {
		this.dateStart = start;
		this.dateEnd = end;
		if (null != this.graph) {
			// and re-initialise the ranges and data for this by clearing it now
			this.graph.clearData();
		}
	}
	
	@Override
	public void initialise(DataGraph<Date, Integer> graph) {
		// remember the graph
		this.graph  = graph;
		// set the ranges initially
		switch(this.type) {
		case emotion :
			graph.setMinY(0);
			graph.setMaxY(10);
			break;
		case sleep :
			graph.setMinY(0);
			graph.setMaxY(Math.max(graph.getMaxY() == null ? 0 : graph.getMaxY(), 8 * 60));
			break;
		case activity :
			break;
		}
		graph.setMinX(this.dateStart);
		graph.setMaxX(this.dateEnd);
	}
	
	@Override
	public int compareX(Date x1, Date x2) {
		return x1.compareTo(x2);
	}
	@Override
	public int compareY(Integer y1, Integer y2) {
		return y1.compareTo(y2);
	}
	@Override
	public float ratioX(Date min, Date max, Date x) {
		if (max == null || min == null) {
			return 0f;
		}
		else {
			float range = max.getTime() - min.getTime();
			float ratio = (x.getTime() - min.getTime()) / range;
			return ratio;
		}
	}
	@Override
	public float ratioY(Integer min, Integer max, Integer y) {
		if (max == null || min == null) {
			return 0f;
		}
		else {
			float range = max - min;
			return (y - min) / range;
		}
	}
	
	@Override
	public boolean isValid(String seriesTitle, Date x, Integer y) {
		switch (this.type) {
		case sleep :
			return TrackPointData.IsSleepKey(seriesTitle);
		case activity :
			return TrackPointData.IsActivityKey(seriesTitle);
		case emotion :
			return false == TrackPointData.IsSleepKey(seriesTitle)
				&& false == TrackPointData.IsActivityKey(seriesTitle);
		}
		return true;
	}
	public static ArrayList<Date> getXDates(Date start, Date end) {
		Date date = new Date(start.getTime());
		ArrayList<Date> dates = new ArrayList<Date>();
		while (date.before(end)) {
			dates.add(new Date(date.getTime()));
			CalendarUtil.addDaysToDate(date, 7);
		}
		return dates;
	}
	@Override
	public String[] getXAxisTitles(Date min, Date max) {
		ArrayList<Date> dates = getXDates(this.dateStart, this.dateEnd);
		String[] toReturn = new String[dates.size()];
		for (int i = 0; i < toReturn.length; ++i) {
			toReturn[i] = K_DATE_FORMAT.format(dates.get(i));
		}
		return toReturn;
	}
	@Override
	public Date[] getXAxisValues(Date min, Date max) {
		ArrayList<Date> dates = getXDates(this.dateStart, this.dateEnd);
		Date[] toReturn = new Date[dates.size()];
		for (int i = 0; i < toReturn.length; ++i) {
			toReturn[i] = dates.get(i);
		}
		return toReturn;
	}
	@Override
	public String[] getYAxisTitles(Integer min, Integer max) {
		switch (this.type){
		case sleep :
			return new String[] {"0", "4", "8", "12", "16", "20", "24"};
		case activity :
			//int range = max - min;
			return new String[0];
		case emotion :
			return new String[] {"0", "2", "4", "6", "8", "10"};
		}
		return new String[0];
	}
	@Override
	public Integer[] getYAxisValues(Integer min, Integer max) {
		switch (this.type){
		case sleep :
			return new Integer[] {0, 4 * 60, 8 * 60, 12 * 60, 16 * 60, 20 * 60, 24 * 60};
		case activity :
			//int range = max - min;
			return new Integer[0];
		case emotion :
			return new Integer[] {0, 2, 4, 6, 8, 10};
		}
		return new Integer[0];
	}

	@Override
	public void drawingPoint(DataGraph<Date, Integer> graph, String title, Date x, Integer y, int canvasX, int canvasY) {
		// fine, whatever
	}

}

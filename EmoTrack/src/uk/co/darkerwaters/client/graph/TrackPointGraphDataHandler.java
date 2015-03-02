package uk.co.darkerwaters.client.graph;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.shared.TrackPointData;

public class TrackPointGraphDataHandler implements DataGraph.DataHandler<Date, Integer> {
	
	public static DateTimeFormat K_DATE_FORMAT = DateTimeFormat.getFormat("MMM-dd");
	
	public static enum Type {
		emotion,
		sleep,
		activity;
		public static Type getTypeForSeriesTitle(String seriesTitle) {
			Type type;
			if (TrackPointData.IsActivityKey(seriesTitle)) {
				type = Type.activity; 
			}
			else if (TrackPointData.IsSleepKey(seriesTitle)) {
				type = Type.sleep; 
			}
			else {
				type = Type.emotion;
			}
			return type;
		}
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
			// get the max Y from the range of values we want...
			Integer maxValue = graph.getMaxY();
			if (null != maxValue) {
				Integer[] yAxisValues = getYAxisValues(graph.getMinY(), maxValue);
				Integer maxY = null;
				for (int i = 0; i < yAxisValues.length; ++i) {
					// use this value as a max
					maxY = yAxisValues[i];
					if (maxY >= maxValue) {
						// this is OK as a max, but stop, this is good
						break;
					}
				}
				graph.setMaxY(maxY == null ? 0 : maxY);
			}
			break;
		case activity :
			graph.setMinY(0);
			// get a new max that is a nice factor of the max number
			Integer maxY = getExponentMax(graph.getMaxY());
			if (null != maxY) {
				graph.setMaxY(maxY);
			}
			break;
		}
		graph.setMinX(this.dateStart);
		graph.setMaxX(this.dateEnd);
	}
	
	public static Integer getExponentMax(Integer max) {
		// get the max of the graph as a string, add one to the most significant bit and
		// pad with zeros to get a nice max
		if (null != max) {
			String maxValue = max.toString();
			try {
				int significantBit = Integer.parseInt(maxValue.substring(0, 1));
				String newValue = Integer.toString(significantBit + 1);
				// pad with zeros
				while (newValue.length() < maxValue.length()) {
					newValue += "0";
				}
				// and create the new value
				max = Integer.parseInt(newValue);
			}
			catch (NumberFormatException e) {
				EmoTrack.LOG.severe("Getting the exponent max failed: " + e.getMessage());
			}
		}
		return max;
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
		float ratio;
		if (max == null || min == null) {
			ratio = 0f;
		}
		else {
			float range = max.getTime() - min.getTime();
			ratio = (x.getTime() - min.getTime()) / range;
		}
		if (ratio > 1f) {
			EmoTrack.LOG.severe("X Ratio for trach graph is " + ratio);
		}
		return ratio;
	}
	@Override
	public float ratioY(Integer min, Integer max, Integer y) {
		float ratio;
		if (max == null || min == null) {
			ratio = 0f;
		}
		else {
			float range = max - min;
			ratio = (y - min) / range;
		}
		if (ratio > 1f) {
			EmoTrack.LOG.severe("Y Ratio for trach graph is " + ratio);
		}
		return ratio;
	}
	
	@Override
	public boolean isValid(String seriesTitle, Date x, Integer y) {
		return this.type.equals(Type.getTypeForSeriesTitle(seriesTitle));
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
		default:
			// just change the values to strings
			Integer[] yAxisValues = getYAxisValues(min, max);
			String[] yAxisTitles = new String[yAxisValues.length];
			for (int i = 0; i < yAxisTitles.length; ++i) {
				yAxisTitles[i] = yAxisValues[i].toString();
			}
			return yAxisTitles;
		}
	}
	@Override
	public Integer[] getYAxisValues(Integer min, Integer max) {
		switch (this.type){
		case sleep :
			return new Integer[] {0, 4 * 60, 8 * 60, 12 * 60, 16 * 60, 20 * 60, 24 * 60};
		case activity :
			Integer[] values = new Integer[0];
			if (null != min && null != max) {
				// get the range - should be nice and round as zero to a nice rounded max, and
				// segment it nicely
				int range = max - min;
				// and create the values array
				values = new Integer[] {
					min,
					min + (int)(range * 0.25f),
					min + (int)(range * 0.5f),
					min + (int)(range * 0.75f),
					max
				};
			}
			return values;
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

package uk.co.darkerwaters;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public abstract class DataGraph {
	
	private static ArrayList<ArrayList<String>> DATA_SERIES = new ArrayList<ArrayList<String>>();
	private static String[] DATA_SERIES_HEADINGS = new String[0];
	
	protected ArrayList<DataGraphSeries> graphSeries = new ArrayList<DataGraphSeries>();
	
	protected int widthX;
	protected int widthY;
	
	protected String title;
	protected Double min;
	protected Double max;

	private String id;
	
	public class DataGraphSeries {
		public DataGraphSeries(String title, int index, int count) {
			this.seriesIndex = index;
			this.seriesTitle = title;
			this.seriesCount = count;
			this.lineWidth = 1;
		}
		int seriesIndex;
		int seriesCount;
		String seriesTitle;
		double min;
		double max;
		double[] data;
		public double range;
		public Color colour;
		public int lineWidth;
	}
	

	public DataGraph(String id) {
		this.id = id;
		widthX = 8;
		widthY = 4;
		this.min = null;
		this.max = null;
	}
	
	public String getId() {
		return this.id;
	}
	
	public int getWidthX() {
		return widthX;
	}

	public void setWidthX(int widthX) {
		this.widthX = widthX;
	}

	public int getWidthY() {
		return widthY;
	}

	public void setWidthY(int widthY) {
		this.widthY = widthY;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public void addDataSeries(String title, int seriesIndex, int seriesCount) {
		this.graphSeries.add(new DataGraphSeries(title, seriesIndex, seriesCount));
	}
	
	public int getNumberDataSeries() {
		return this.graphSeries.size();
	}
	
	public DataGraphSeries getDataSeries(int index) {
		return this.graphSeries.get(index);
	}
	
	public static int getAvailableSeries() {
		synchronized (DATA_SERIES) {
			return DATA_SERIES.size();
		}
	}
	
	public static String getAvailableSeriesHeading(int index) {
		synchronized (DATA_SERIES) {
			if (null != DATA_SERIES_HEADINGS && index < DATA_SERIES_HEADINGS.length) {
				// there are headings
				return DATA_SERIES_HEADINGS[index];
			}
			else {
				return "Series " + (index + 1);
			}
		}
	}

	public void updateGraphHeadings(String[] headings) {
		synchronized (DATA_SERIES) {
			DATA_SERIES_HEADINGS = headings;
		}
		// update the titles of the series we have
		for (int i = 0; i < headings.length; ++i) {
			// find any series with the index of this heading
			for (DataGraphSeries series : this.graphSeries) {
				if (series.seriesIndex == i) {
					// this is the series for this heading
					series.seriesTitle = headings[i];
				}
			}
		}
	}

	public static void addData(String[] dataEntries) {
		// add the data for this graph, store the series the data could be
		synchronized (DATA_SERIES) {
			for (int i = 0; i < dataEntries.length; ++i) {
				if (DATA_SERIES.size() < i + 1) {
					// there is no series for this, add one
					DATA_SERIES.add(new ArrayList<String>(1000));
				}
				ArrayList<String> series = DATA_SERIES.get(i);
				while (series.size() > 999) {
					series.remove(0);
				}
				series.add(dataEntries[i]);
			}
		}
	}
	
	public DataGraphSeries populateSeries(DataGraphSeries series) {
		series.min = Double.MAX_VALUE;
		series.max = Double.MIN_VALUE;
		String[] dataAsStrings;
		synchronized (DATA_SERIES) {
			if (DATA_SERIES.size() > series.seriesIndex) {
				// cool
				ArrayList<String> data = DATA_SERIES.get(series.seriesIndex);
				dataAsStrings = data.toArray(new String[data.size()]);
			}
			else {
				dataAsStrings = new String[0];
			}
		}
		// get the last of the data, up to the count we require
		int i = dataAsStrings.length - series.seriesCount;
		if (i < 0) {
			// there isn't enough data really, just get it all...
			i = 0;
			series.data = new double[dataAsStrings.length];
		}
		else {
			// populate the data count
			series.data = new double[series.seriesCount];
		}
		int dataCounter = 0;
		for (;i < dataAsStrings.length; ++i) {
			try {
				double value = Double.parseDouble(dataAsStrings[i]);
				if (value < series.min) {
					series.min = value;
				}
				if (value > series.max) {
					series.max = value;
				}
				series.data[dataCounter] = value;
			}
			catch (Exception e) {
				// fine
				series.data[dataCounter] = Double.NaN;
			}
			// increment the counter
			++dataCounter;
		}
		// have the range now
		series.range = series.max - series.min;
		// return the populated series
		return series;
	}

	public abstract void drawDataSeries(Rectangle clientArea, GC gc, Display display);

}

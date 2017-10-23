package uk.co.darkerwaters;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

public abstract class DataGraph {
	
	private static ArrayList<ArrayList<String>> DATA_SERIES = new ArrayList<ArrayList<String>>();
	
	protected int seriesIndex;

	public DataGraph(int seriesIndex) {
		this.seriesIndex = seriesIndex;
	}
	
	public int getSeriesIndex() {
		return this.seriesIndex;
	}

	public void updateGraphHeadings(String[] headings) {
		// do something with this then
		
	}

	public static void addData(String[] dataEntries) {
		// add the data for this graph, store the series the data could be
		synchronized (DATA_SERIES) {
			for (int i = 0; i < dataEntries.length; ++i) {
				if (DATA_SERIES.size() < i + 1) {
					// there is no series for this, add one
					DATA_SERIES.add(new ArrayList<String>(100));
				}
				ArrayList<String> series = DATA_SERIES.get(i);
				while (series.size() > 99) {
					series.remove(0);
				}
				series.add(dataEntries[i]);
			}
		}
	}
	
	public String[] getSeries(int index) {
		String[] toReturn = new String[0];
		synchronized (DATA_SERIES) {
			if (DATA_SERIES.size() > index) {
				// cool
				ArrayList<String> series = DATA_SERIES.get(index);
				toReturn = series.toArray(new String[series.size()]);
			}
		}
		return toReturn;
	}

	public abstract void drawDataSeries(Rectangle clientArea, GC gc, Display display);

}

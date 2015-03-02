package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.graph.DataGraph;
import uk.co.darkerwaters.client.graph.DataGraph.DataHandler;
import uk.co.darkerwaters.client.graph.TrackPointGraphDataHandler;
import uk.co.darkerwaters.client.graph.TrackPointGraphDataHandler.Type;

public class AnalysisAverageGraphDataHandler implements DataHandler<Integer, Float> {
	
	private final Type type;
	private DataGraph<Integer, Float> graph;

	public AnalysisAverageGraphDataHandler(String seriesTitle) {
		// constructor
		this.type = Type.getTypeForSeriesTitle(seriesTitle);
	}

	@Override
	public int compareX(Integer x1, Integer x2) {
		return x1.compareTo(x2);
	}
	@Override
	public int compareY(Float y1, Float y2) {
		return y1.compareTo(y2);
	}
	@Override
	public float ratioX(Integer min, Integer max, Integer x) {
		float ratio;
		if (max == null || min == null) {
			ratio = 0f;
		}
		else {
			float range = max - min;
			ratio = (x - min) / range;
		}
		return ratio;
	}
	@Override
	public float ratioY(Float min, Float max, Float y) {
		float ratio;
		if (max == null || min == null) {
			ratio = 0f;
		}
		else {
			float range = max - min;
			ratio = (y - min) / range;
		}
		return ratio;
	}

	@Override
	public boolean isValid(String seriesTitle, Integer x, Float y) {
		return true;
	}

	@Override
	public String[] getXAxisTitles(Integer min, Integer max) {
		return new String[0];
	}

	@Override
	public Integer[] getXAxisValues(Integer min, Integer max) {
		return new Integer[0];
	}
	@Override
	public String[] getYAxisTitles(Float min, Float max) {
		switch (this.type){
		case sleep :
			return new String[] {"0", "4", "8", "12", "16", "20", "24"};
		default:
			// just change the values to strings
			Float[] yAxisValues = getYAxisValues(min, max);
			String[] yAxisTitles = new String[yAxisValues.length];
			for (int i = 0; i < yAxisTitles.length; ++i) {
				yAxisTitles[i] = new Integer((int)yAxisValues[i].floatValue()).toString();
			}
			return yAxisTitles;
		}
	}
	@Override
	public Float[] getYAxisValues(Float min, Float max) {
		switch (this.type){
		case sleep :
			return new Float[] {0f, 4f * 60f, 8f * 60f, 12f * 60f, 16f * 60f, 20f * 60f, 24f * 60f};
		case activity :
			Float[] values = new Float[0];
			if (null != min && null != max) {
				// get the range - should be nice and round as zero to a nice rounded max, and
				// segment it nicely
				float range = max - min;
				// and create the values array
				values = new Float[] {
					min,
					min + (range * 0.25f),
					min + (range * 0.5f),
					min + (range * 0.75f),
					max
				};
			}
			return values;
		case emotion :
			return new Float[] {0f, 2f, 4f, 6f, 8f, 10f};
		}
		return new Float[0];
	}
	
	@Override
	public void initialise(DataGraph<Integer, Float> graph) {
		// remember the graph
		this.graph  = graph;
		// set the ranges initially
		switch(this.type) {
		case emotion :
			graph.setMinY(0f);
			graph.setMaxY(10f);
			break;
		case sleep :
			graph.setMinY(0f);
			// get the max Y from the range of values we want...
			Float maxValue = graph.getMaxY();
			if (null != maxValue) {
				Float[] yAxisValues = getYAxisValues(graph.getMinY(), maxValue);
				Float maxY = null;
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
			graph.setMinY(0f);
			// get a new max that is a nice factor of the max number
			Integer maxY = TrackPointGraphDataHandler.getExponentMax(graph.getMaxY() == null ? null : new Integer((int)graph.getMaxY().floatValue()));
			if (null != maxY) {
				graph.setMaxY((float)maxY);
			}
			break;
		}
	}

	@Override
	public void drawingPoint(DataGraph<Integer, Float> graph, String title, Integer x, Float y, int canvasX, int canvasY) {
		// fine
	}

}

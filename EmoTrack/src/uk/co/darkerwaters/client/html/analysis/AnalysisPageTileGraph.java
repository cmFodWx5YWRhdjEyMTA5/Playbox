package uk.co.darkerwaters.client.html.analysis;

import java.util.HashMap;

import org.vaadin.gwtgraphics.client.shape.Circle;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.entry.SleepTab;
import uk.co.darkerwaters.client.graph.DataGraph;
import uk.co.darkerwaters.client.graph.DataGraph.DataGraphListener;
import uk.co.darkerwaters.client.graph.EventGraphDataHandler;
import uk.co.darkerwaters.client.graph.EventGraphDataHandler.EventLabel;
import uk.co.darkerwaters.client.graph.TrackPointGraphDataHandler.Type;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Panel;

public class AnalysisPageTileGraph extends AnalysisPageTile {

	private DataGraph<Integer, Float> graph;
	private final HashMap<String, Integer> seriesIndices = new HashMap<String, Integer>();
	private final Type type;

	public AnalysisPageTileGraph(String title) {
		super(title);
		this.type = Type.getTypeForSeriesTitle(title);
		this.graph = new DataGraph<Integer, Float>(null, new AnalysisAverageGraphDataHandler(title), createListener(), this.type == Type.sleep ? SleepTab.sleepColours : null);
		this.graph.setIsAreaChart(true);
		this.graph.setIsDrawLegend(false);
		this.graph.setIsBoxInValues(true);
		Panel graphPanel = this.graph.getContent();
		graphPanel.addStyleName("analysis-graph");
		getContent().add(graphPanel);
	}

	private DataGraphListener<Integer, Float> createListener() {
		return new DataGraphListener<Integer, Float>() {
			@Override
			public void seriesSelected(String seriesTitle, boolean isSelected) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void pointSelected(final DataGraph<Integer, Float> source, Circle point, String seriesTitle, Integer x, Float y) {
				String description = y.toString() + " " + (seriesTitle == null ? "" : (seriesTitle + " ") + EmoTrackConstants.Instance.at() + " " + x.toString());
				
				final EventLabel eventLabel = new EventGraphDataHandler.EventLabel(point.getX() + 5, point.getY() + 5, description, true);
				eventLabel.addToGraph(source);
				
				Timer timer = new Timer() {
					@Override
					public void run() {
						eventLabel.removeFromGraph(source);
					}
				};
				timer.schedule(5000);
			}
		};
	}

	public void addGraphValue(String seriesTitle, float average) {
		Integer index = this.seriesIndices.get(seriesTitle);
		if (null == index) {
			index = new Integer(0);
		}
		this.graph.addData(seriesTitle, index, average);
		this.seriesIndices.put(seriesTitle, new Integer(index + 1));
	}
	
	public void drawGraph() {
		// resize the window to draw it
		this.graph.resizeWindow();
		if (type.equals(Type.sleep)) {
			//ensure that deep sleep is in front
			this.graph.bringSeriesToFront(TrackPointData.SLEEPKEY[1]);
		}
	}

	public Type getType() {
		return this.type;
	}

	public String[] getSeriesTitles() {
		return this.graph.getSeriesTitles();
	}

}

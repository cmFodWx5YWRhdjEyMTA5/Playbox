package uk.co.darkerwaters.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.vaadin.gwtgraphics.client.animation.Animate;
import org.vaadin.gwtgraphics.client.shape.Rectangle;
import org.vaadin.gwtgraphics.client.shape.Text;

public class EventGraphDataHandler implements DataGraph.DataHandler<Date, String> {
	
	private static final int K_UNSELECTED_SIZE = 16;
	private static final int K_SELECTED_SIZE = 24;

	private Date dateStart = null;
	
	private Date dateEnd = null;

	private DataGraph<Date, String> graph = null;
	
	public static class EventLabel {

		final Text text;
		final Rectangle rect;

		public EventLabel(int canvasX, int canvasY, Date x, String y, boolean isBorder) {
			this.text = new Text(canvasX, canvasY, y);
			text.setStrokeColor("grey");
			text.setFillColor("grey");
			text.setFontSize(K_UNSELECTED_SIZE);
			this.rect = new Rectangle(canvasX - 5, canvasY - (int)((text.getTextHeight() * 0.7f) + 5), text.getTextWidth() + 10, text.getTextHeight() + 10);
			if (isBorder) {
				rect.setStrokeColor("grey");
				rect.setRoundedCorners(3);
			}
			else {
				rect.setStrokeColor("white");
			}
			rect.setFillColor("white");
		}
		
		public void addToGraph(DataGraph<?,?> graph) {
			graph.add(this.rect);
			graph.add(this.text);
		}
		
		public void removeFromGraph(DataGraph<?,?> graph) {
			graph.remove(this.rect);
			graph.remove(this.text);
		}

		public void updateRect() {
			this.rect.setWidth(text.getTextWidth() + 10);
			this.rect.setHeight(text.getTextHeight() + 10);
		}
		
	}
	
	private HashMap<Date, ArrayList<EventLabel>> eventLabels = new HashMap<Date, ArrayList<EventLabel>>();
	
	public EventGraphDataHandler() {
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
	public void initialise(DataGraph<Date, String> graph) {
		// remember the graph
		this.graph  = graph;
		this.eventLabels.clear();
		// set the ranges initially
		graph.setMinX(this.dateStart);
		graph.setMaxX(this.dateEnd);
	}
	
	@Override
	public int compareX(Date x1, Date x2) {
		return x1.compareTo(x2);
	}
	@Override
	public int compareY(String y1, String y2) {
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
	public float ratioY(String min, String max, String y) {
		return 0.5f;
	}
	
	@Override
	public boolean isValid(String seriesTitle, Date x, String y) {
		return true;
	}
	@Override
	public String[] getXAxisTitles(Date min, Date max) {
		ArrayList<Date> dates = TrackPointGraphDataHandler.getXDates(this.dateStart, this.dateEnd);
		String[] toReturn = new String[dates.size()];
		for (int i = 0; i < toReturn.length; ++i) {
			toReturn[i] = TrackPointGraphDataHandler.K_DATE_FORMAT.format(dates.get(i));
		}
		return toReturn;
	}
	@Override
	public Date[] getXAxisValues(Date min, Date max) {
		ArrayList<Date> dates = TrackPointGraphDataHandler.getXDates(this.dateStart, this.dateEnd);
		Date[] toReturn = new Date[dates.size()];
		for (int i = 0; i < toReturn.length; ++i) {
			toReturn[i] = dates.get(i);
		}
		return toReturn;
	}
	@Override
	public String[] getYAxisTitles(String min, String max) {
		return new String[0];
	}
	@Override
	public String[] getYAxisValues(String min, String max) {
		return new String[0];
	}

	@Override
	public void drawingPoint(DataGraph<Date, String> graph, String title, Date x, String y, int canvasX, int canvasY) {
		// add the title for this point
		EventLabel label = new EventLabel(canvasX, canvasY, x, y, false);

		ArrayList<EventLabel> labelList = eventLabels.get(x);
		if (null == labelList) {
			labelList = new ArrayList<EventLabel>();
			eventLabels.put(x, labelList);
		}
		labelList.add(label);
		
		graph.add(label.rect);
		graph.add(label.text);
	}

	public void eventSelected(DataGraph<Date, String> graph, Date x, String y) {
		ArrayList<EventLabel> labelList = this.eventLabels.get(x);
		boolean pointOrderChanged = false;
		if (null != labelList) {
			// highlight this label
			for (EventLabel label : labelList) {
				label.text.setFontSize(K_SELECTED_SIZE);
				int smallWidth = label.rect.getWidth();
				int smallHeight = label.rect.getHeight();
				label.updateRect();
				new Animate(label.text, "fontsize", K_SELECTED_SIZE, K_UNSELECTED_SIZE, 5000).start();
				new Animate(label.rect, "width", label.rect.getWidth(), smallWidth, 5000).start();
				new Animate(label.rect, "heignt", label.rect.getHeight(), smallHeight, 5000).start();

				graph.bringToFront(label.rect);
				graph.bringToFront(label.text);
				pointOrderChanged = true;
			}
		}
		if (pointOrderChanged) {
			graph.bringSeriesDataPointsToFront();
		}
	}

}

package uk.co.darkerwaters.client.graph;

import java.util.ArrayList;

import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Line;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.animation.Animate;
import org.vaadin.gwtgraphics.client.shape.Circle;
import org.vaadin.gwtgraphics.client.shape.Path;
import org.vaadin.gwtgraphics.client.shape.Text;

import uk.co.darkerwaters.client.EmoTrack;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;

public class DataGraph<X, Y> {

	private static final String K_AXIS_LINECOLOR = "grey";
	
	private static final String[] K_SERIES_COLOURS = new String[] {
		"#3366CC","#DC3912","#FF9900","#109618","#990099","#3B3EAC","#0099C6",
		"#DD4477","#66AA00","#B82E2E","#316395","#994499","#22AA99","#AAAA11","#6633CC","#E67300",
		"#8B0707","#329262","#5574A6","#3B3EAC"};

	private static final int K_SELECTED_WIDTH = 4;
	private static final int K_UNSELECTED_WIDTH = 2;
	
	private static final double K_SELECTED_OPACITY = 1;
	private static final double K_UNSELECTED_OPACITY = 0.8;
	
	private static final int K_SELECTED_RADIUS = 6;
	private static final int K_UNSELECTED_RADIUS = 3;
	
	private SimplePanel mainPanel = new SimplePanel();
	private final DrawingArea canvas;
	
	private X minX;
	private X maxX;
	
	private Y minY;
	private Y maxY;
	
	private boolean isInitialised = false;
	
	private final DataGraphListener<X, Y> listener;
	
	private boolean isDrawLegend = true;
	
	private boolean isDrawPath = true;
	
	private boolean isAreaChart = false;
	
	private final String[] colours;
	
	private class Series {
		final String title;
		final ArrayList<DataPair> data = new ArrayList<DataPair>();
		final ClickHandler selector;
		Path path = null;
		Text legend = null;
		final ArrayList<Circle> points = new ArrayList<Circle>();
		boolean isSelected = false;
		Series(String title, ClickHandler selector) {
			this.title = title;
			this.selector = selector;
		}
	}
	
	private class DataPair {
		final X x;
		final Y y;
		DataPair(X x, Y y) {
			this.x = x;
			this.y = y;
		}
	}
	
	public interface DataGraphListener<X, Y> {
		void seriesSelected(String seriesTitle, boolean isSelected);
		void pointSelected(DataGraph<X, Y> source, Circle point, String seriesTitle, X x, Y y);
	}
	
	public interface DataHandler<X, Y> {
		public abstract int compareX(X x1, X x2);
		public abstract int compareY(Y y1, Y y2);
		
		public abstract float ratioX(X min, X max, X x);
		public abstract float ratioY(Y min, Y max, Y y);
		
		public abstract boolean isValid(String seriesTitle, X x, Y y);
		
		public String[] getXAxisTitles(X min, X max);
		public X[] getXAxisValues(X min, X max);
		
		public String[] getYAxisTitles(Y min, Y max);
		public Y[] getYAxisValues(Y min, Y max);
		public abstract void initialise(DataGraph<X, Y> graph);
		public abstract void drawingPoint(DataGraph<X, Y> graph, String title, X x, Y y, int canvasX, int canvasY);
	}
	
	private final DataHandler<X, Y> handler; 
	
	final ArrayList<Series> dataSeries = new ArrayList<Series>();

	private String graphTitle;
	
	public DataGraph(String graphTitle, DataHandler<X, Y> handler, DataGraphListener<X, Y> listener, String[] colours) {
		if (null == colours) {
			this.colours = K_SERIES_COLOURS;
		}
		else {
			this.colours = colours;
		}
		this.handler = handler;
		this.graphTitle = graphTitle;
		this.listener = listener;
		this.canvas = new DrawingArea(mainPanel.getOffsetWidth(), mainPanel.getOffsetHeight());
		// add to the panel to draw
		mainPanel.add(canvas);
		mainPanel.addStyleName("data-graph");
		// be informed on changes in size and update our drawing
		Window.addResizeHandler(new ResizeHandler() {
			@Override
			public void onResize(ResizeEvent event) {
				resizeWindow(event.getHeight(), event.getWidth());
			}
		});
	}
	
	public void resizeWindow(int height, int width) {
		// set the canvas size
		this.canvas.setHeight(this.mainPanel.getOffsetHeight());
		this.canvas.setWidth(this.mainPanel.getOffsetWidth());
		// and draw the data
		drawData();
	}
	
	public void setMinX(X x) {
		this.minX = x;
	}
	
	public void setMaxX(X x) {
		this.maxX = x;
	}
	
	public void setMinY(Y y) {
		this.minY = y;
	}
	
	public void setMaxY(Y y) {
		this.maxY = y;
	}
	
	public void clearData() {
		this.dataSeries.clear();
		this.minX = null;
		this.maxX = null;
		this.minY = null;
		this.maxY = null;
		this.isInitialised = false;
	}
	
	public boolean addData(final String seriesTitle, X x, Y y) {
		// check the data
		if (false == this.handler.isValid(seriesTitle, x, y)) {
			return false;
		}
		// add the data series
		Series series = null;
		for (Series extant : this.dataSeries) {
			if (extant.title.equals(seriesTitle)) {
				series = extant;
				break;
			}
		}
		// ensure there is a series
		if (null == series) {
			series = new Series(seriesTitle, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					handleSeriesSelection(seriesTitle);
				}
			});
			this.dataSeries.add(series);
		}
		// add the data to the series
		if (null != x && null != y) {
			series.data.add(new DataPair(x, y));
			// find the X ranges
			if (null == this.minX || this.handler.compareX(x, minX) < 0) {
				this.minX = x;
			}
			if (null == this.maxX || this.handler.compareX(x, maxX) > 0) {
				this.maxX = x;
			}
			// find the Y ranges
			if (null == this.minY || this.handler.compareY(y, minY) < 0) {
				this.minY = y;
			}
			if (null == this.maxY || this.handler.compareY(y, maxY) > 0) {
				this.maxY = y;
			}
			if (this.isInitialised) {
				this.isInitialised = false;
			}
		}
		return true;
	}

	protected void handleSeriesSelection(String seriesTitle) {
		Series series = null;
		for (Series extant : this.dataSeries) {
			if (extant.title.equals(seriesTitle)) {
				series = extant;
			}
			else if (extant.isSelected) {
				// send a message that this is un-selected now
				extant.isSelected = false;
				DataGraph.this.listener.seriesSelected(extant.title, extant.isSelected);
			}
			// unselect everything
			if (null != extant.path) {
				extant.path.setStrokeWidth(K_UNSELECTED_WIDTH);
				extant.path.setStrokeOpacity(K_UNSELECTED_OPACITY);
				extant.path.setFillOpacity(this.isAreaChart ? K_UNSELECTED_OPACITY : 0);
				extant.legend.setFillOpacity(K_UNSELECTED_OPACITY);
				extant.legend.setStrokeOpacity(K_UNSELECTED_OPACITY);
			}
		}
		if (null == series) {
			EmoTrack.LOG.severe("Unknown series clicked: " + seriesTitle);
		}
		else {
			// toggle the selection
			series.isSelected = !series.isSelected;
			DataGraph.this.listener.seriesSelected(seriesTitle, series.isSelected);
			// select if now selected
			if (null != series.path && series.isSelected) {
				series.path.setStrokeWidth(K_SELECTED_WIDTH);
				series.path.setStrokeOpacity(K_SELECTED_OPACITY);
				series.path.setFillOpacity(this.isAreaChart ? K_SELECTED_OPACITY : 0);
				series.legend.setFillOpacity(K_SELECTED_OPACITY);
				series.legend.setStrokeOpacity(K_SELECTED_OPACITY);
			}
		}
		
	}

	public void drawData() {
		if (false == isInitialised) {
			this.handler.initialise(this);
			isInitialised = true;
		}
		// clear old data
		canvas.clear();
		// get the data ranges
		int width = this.canvas.getWidth();
		int height = this.canvas.getHeight();
		// get the box range
		int left = (int)(width * 0.05f);
		int right = (int)(width * 0.8f);
		int top = (int)(20);
		int bottom = (int)(height - 40);
		
		// draw in the series data now
		int axisHeight = top - bottom;
		int axisWidth = right - left;
		int legendPosition = top;
		for (int i = 0; i < this.dataSeries.size(); ++i) {
			Series series = this.dataSeries.get(i);
			// draw in the legend
			String seriesColour;
			if (i >= this.colours.length) {
				seriesColour = K_AXIS_LINECOLOR;
			}
			else {
				seriesColour = this.colours[i];
			}

			// create the series selector
			series.legend = createText(series.title, right, legendPosition, 0f, 0f);
			if (false == this.isDrawLegend) {
				this.canvas.remove(series.legend);
			}
			series.legend.setStrokeColor(seriesColour);
			series.legend.setFillColor(seriesColour);
			series.legend.setFillOpacity(K_UNSELECTED_OPACITY);
			series.legend.setStrokeOpacity(K_UNSELECTED_OPACITY);
			series.legend.addClickHandler(series.selector);
			// move the position of the legends down
			legendPosition += series.legend.getTextHeight() * 1.1f;
			// draw in the points
			series.path = null;
			series.points.clear();
			int x = 0;
			for (DataPair item : series.data) {
				// determine where to put this
				x = left + (int)(this.handler.ratioX(minX, maxX, item.x) * axisWidth); 
				int y = bottom + (int)(this.handler.ratioY(minY, maxY, item.y) * axisHeight);
				this.handler.drawingPoint(this, series.title, item.x, item.y, x, y);
				// create a point for this item 
				Circle circle = new Circle(x, y, K_UNSELECTED_RADIUS);
				circle.setStrokeColor(seriesColour);
				circle.setFillColor(seriesColour);
				series.points.add(circle);
				// create the data selection for this item of data
				ClickHandler itemHandler = createClickHandler(series, item, circle);
				circle.addClickHandler(itemHandler);
				if (null == series.path) {
					if (this.isAreaChart) {
						// put the first item in as zero to draw up from the x-axis
						series.path = createSeriesPath(x, bottom, seriesColour);
						series.path.lineTo(x, y);
					}
					else {
						series.path = createSeriesPath(x, y, seriesColour);
					}
					// not adding to the path as clicking on the filled part (most of it) detects a hit
					//series.path.addClickHandler(series.selector);
				}
				else {
					series.path.lineTo(x, y);
				}
			}
			if (this.isAreaChart && null != series.path) {
				// put the last item in as zero to draw down to the x-axis
				series.path.lineTo(x, bottom);
			}
			if (null != series.path && this.isDrawPath) {
				this.canvas.add(series.path);
			}
		}
		// draw in the axis lines
		drawChartAxis(width, height, left, right, top, bottom);
		// lastly add all the circles - to ensure they are on top and selectable
		for (Series series : this.dataSeries) {
			for (Circle point : series.points) {
				this.canvas.add(point);
			}
		}
	}

	private Path createSeriesPath(int x, int y, String seriesColour) {
		Path path = new Path(x, y);
		path.setStrokeColor(seriesColour);
		path.setStrokeWidth(K_UNSELECTED_WIDTH);
		path.setStrokeOpacity(K_UNSELECTED_OPACITY);
		path.setFillOpacity(this.isAreaChart ? K_SELECTED_OPACITY : 0);
		path.setFillColor(seriesColour);
		return path;
	}

	private ClickHandler createClickHandler(final Series series, final DataPair item, final Circle circle) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// highlight what they clicked...
				circle.setRadius(K_SELECTED_WIDTH);
				new Animate(circle, "radius", K_SELECTED_RADIUS, K_UNSELECTED_RADIUS, 1000).start();
				DataGraph.this.listener.pointSelected(DataGraph.this, circle, series.title, item.x, item.y);
			}
		};
		
	}

	private void drawChartAxis(int width, int height, int left, int right, int top, int bottom) {
		createLine(left, top, left, bottom, K_AXIS_LINECOLOR);
		createLine(left, bottom, right, bottom, K_AXIS_LINECOLOR);
		
		String[] yAxisTitles = this.handler.getYAxisTitles(minY, maxY);
		Y[] yAxisValues = this.handler.getYAxisValues(minY, maxY);
		int axisHeight = top - bottom;
		for (int i = 0; i < yAxisTitles.length && i < yAxisValues.length; ++i) {
			int y = bottom + (int)(this.handler.ratioY(minY, maxY, yAxisValues[i]) * axisHeight);
			if (y >= top) {
				createText(yAxisTitles[i], left, y, 1f, 0.5f);
			}
		}
		
		String[] xAxisTitles = this.handler.getXAxisTitles(minX, maxX);
		X[] xAxisValues = this.handler.getXAxisValues(minX, maxX);
		int axisWidth = right - left;
		for (int i = 0; i < xAxisTitles.length && i < xAxisValues.length; ++i) {
			int x = left + (int)(this.handler.ratioX(minX, maxX, xAxisValues[i]) * axisWidth);
			if (x <= right) {
				createText(xAxisTitles[i], x, bottom, 0.5f, 1f);
			}
		}
		
		createText(this.graphTitle, right, bottom, 0f, 0f);
	}

	public Text createText(String content, int x, int y, float offsetX, float offsetY) {
		Text text = new Text(x, y, content);
		// offset the text to centre it
		y += (int)(offsetY * text.getTextHeight());
		x -= (int)(offsetX * text.getTextWidth());
		text.setY(y);
		text.setX(x);
		text.setStrokeColor("grey");
		text.setFillColor("grey");
		canvas.add(text);
		return text;
	}

	private Line createLine(int x1, int y1, int x2, int y2, String color) {
		Line line = new Line(x1, y1, x2, y2);
		line.setStrokeColor(color);
		canvas.add(line);
		return line;
	}
	
	public void setIsDrawLegend(boolean isDrawLegend) {
		this.isDrawLegend = isDrawLegend;
	}
	
	public void setIsDrawPath(boolean isDrawPath) {
		this.isDrawPath = isDrawPath;
	}

	public void setIsAreaChart(boolean isAreaChart) {
		this.isAreaChart = isAreaChart;
	}

	public Panel getContent() {
		return this.mainPanel;
	}

	public Y getMaxY() {
		return this.maxY;
	}
	
	public Y getMinY() {
		return this.minY;
	}
	
	public X getMaxX() {
		return this.maxX;
	}
	
	public X getMinX() {
		return this.minX;
	}

	public DrawingArea getCanvas() {
		return canvas;
	}

	public void add(VectorObject object) {
		this.canvas.add(object);
	}

	public void bringToFront(VectorObject object) {
		// bring it to the front
		this.canvas.bringToFront(object);
	}
	
	public void bringSeriesDataPointsToFront() {
		// bring all the circles back in too
		for (Series series : this.dataSeries) {
			for (Circle point : series.points) {
				this.canvas.bringToFront(point);
			}
		}
	}

	public void remove(VectorObject object) {
		this.canvas.remove(object);
	}
}

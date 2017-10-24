package uk.co.darkerwaters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class DataLineGraph extends DataGraph {
	
	public static final int K_BORDER = 10;
	
	public DataLineGraph(String title) {
		super(title);
	}

	@Override
	public void drawDataSeries(Rectangle clientArea, GC gc, Display display) {
		// background is nice black
		
		// move in from the area a little for borders
		clientArea.x += K_BORDER;
		clientArea.y += K_BORDER;
		clientArea.width -= K_BORDER * 2;
		clientArea.height -= K_BORDER * 2;
		
        
		
		gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
		gc.setLineWidth(1);
		gc.drawText(this.title, clientArea.x, clientArea.y);
		
		gc.setLineWidth(2);
		gc.drawLine(clientArea.x, clientArea.y, clientArea.x, clientArea.y + clientArea.height);
		gc.drawLine(clientArea.x, clientArea.y + clientArea.height, clientArea.x + clientArea.width, clientArea.y + clientArea.height);

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		int xMax = 0;
		for (DataGraphSeries series : this.graphSeries) {
			populateSeries(series);
			if (series.min < min) {
				min = series.min;
			}
			if (series.max > max) {
				max = series.max;
			}
			if (series.data.length > xMax) {
				xMax = series.data.length;
			}
		}
		
		if (this.min != null) {
			min = this.min.doubleValue();
		}
		if (this.max != null) {
			max = this.max.doubleValue();
		}
		// have the range now
		double range = max - min;
		if (xMax > 0 && range > 0.0) {
			// work out the factors to draw this data with
			double xFactor = (clientArea.width - K_BORDER) * 1.0 / xMax;
			double yFactor = (clientArea.height - K_BORDER) * 1.0 / range;
			for (DataGraphSeries series : this.graphSeries) { 
				// draw the data
				int x = 0;
				int y = 0;
				boolean lastSet = false;
				int lastX = -1;
				int lastY = -1;
				gc.setForeground(series.colour == null ? display.getSystemColor(SWT.COLOR_GREEN) : series.colour);
				gc.setLineWidth(series.lineWidth);
				for (int i = 0; i < series.data.length; ++i) {
					if (series.data[i] != Double.NaN) {
						// get the x
						x = (int)((i * xFactor) + clientArea.x);
						y = clientArea.height + clientArea.y - (int)((series.data[i] - min) * yFactor);
						if (y > clientArea.height + clientArea.y) {
							// clamp to the y
							y = clientArea.height + clientArea.y;
						}
						else if (y < clientArea.y) {
							// clamp to the top
							y = clientArea.y;
						}
						else {
							if (lastSet) {
								gc.drawLine(lastX, lastY, x, y);
							}
						}
						lastX = x;
						lastY = y;
						lastSet = true;
					}
				}
			}
		}
	}
}

package uk.co.darkerwaters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

public class DataLineGraph extends DataGraph {
	
	public static final int K_BORDER = 10;
	
	public DataLineGraph(int seriesIndex) {
		super(seriesIndex);
	}

	@Override
	public void drawDataSeries(Rectangle clientArea, GC gc, Display display) {
		// background is nice black
		gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK)); 
        gc.fillRectangle(clientArea);
		
		// move in from the area a little for borders
		clientArea.x += K_BORDER;
		clientArea.y += K_BORDER;
		clientArea.width -= K_BORDER * 2;
		clientArea.height -= K_BORDER * 2;
        
		gc.setLineWidth(2);
		gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
		gc.drawLine(clientArea.x, clientArea.y, clientArea.x, clientArea.y + clientArea.height);
		gc.drawLine(clientArea.x, clientArea.y + clientArea.height, clientArea.width, clientArea.y + clientArea.height);

		String[] series = getSeries(this.seriesIndex);
		if (series.length > 0) {
			double[] seriesData = new double[series.length];
			// find the min and the max and parse to numbers
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			for (int i = 0; i < series.length; ++i) {
				try {
					double value = Double.parseDouble(series[i]);
					if (value < min) {
						min = value;
					}
					if (value > max) {
						max = value;
					}
					seriesData[i] = value;
				}
				catch (Exception e) {
					// fine
					seriesData[i] = Double.NaN;
				}
			}
			double range = max - min;
			double xFactor = (clientArea.width - K_BORDER) * 1.0 / seriesData.length;
			double yFactor = (clientArea.height - K_BORDER) * 1.0 / range; 
			// draw the data
			int x = 0;
			int y = 0;
			int lastX = -1;
			int lastY = -1;
			gc.setLineWidth(1);
			for (int i = 0; i < seriesData.length; ++i) {
				if (seriesData[i] != Double.NaN) {
					// get the x
					x = (int)((i * xFactor) + clientArea.x);
					y = clientArea.height + clientArea.y - (int)((seriesData[i] - min) * yFactor);
					gc.setForeground(display.getSystemColor(SWT.COLOR_GREEN));
					if (lastX >= 0 && lastY >= 0 && x != lastX) {
						gc.drawLine(lastX, lastY, x, y);
					}
					gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
					gc.drawPoint(x, y);
					lastX = x;
					lastY = y;
				}
			}
		}
		
	}

}

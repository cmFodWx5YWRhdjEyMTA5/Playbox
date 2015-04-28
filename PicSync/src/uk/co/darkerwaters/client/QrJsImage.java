package uk.co.darkerwaters.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;

public class QrJsImage {
	
	public static DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyyMMddHHmmss");
	
	public QrJsImage() {
		Timer timer = new Timer() {	
			@Override
			public void run() {
				drawChart();
			}
		};
		timer.scheduleRepeating(100);
	}

	protected void drawChart() {
		String dateString = dateFormat.format(new Date());
		makeCode(dateString);
	}
	
	private static native void makeCode(String content) /*-{
	    $wnd.makeCode(content);
	}-*/;
}

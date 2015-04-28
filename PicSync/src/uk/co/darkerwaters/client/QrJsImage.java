package uk.co.darkerwaters.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class QrJsImage {
	
	public static DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyyMMddHHmmss");
	private Label timeLabel;
	
	public QrJsImage() {
		RootPanel rootPanel = RootPanel.get("time_sync_qr");
		this.timeLabel = new Label();
		this.timeLabel.addStyleName("timeLabel");
		rootPanel.add(this.timeLabel);
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
		timeLabel.setText(dateString);
	}
	
	private static native void makeCode(String content) /*-{
	    $wnd.makeCode(content);
	}-*/;
}

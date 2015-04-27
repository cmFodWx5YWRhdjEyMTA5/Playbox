package uk.co.darkerwaters.client;

import java.util.Date;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

public class QrGoogleImage {
	
	public static DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd-HH:mm:ss.SSS");

	private Image codeImage;
	
	private Label codeLabel;

	private FlowPanel mainPanel;
	
	public QrGoogleImage(Panel parent) {
		this.mainPanel = new FlowPanel();
		
		this.codeImage = new Image();
		this.codeLabel = new Label("Please wait, charts initialising...");
		this.mainPanel.add(this.codeImage);
		this.mainPanel.add(this.codeLabel);
		parent.add(this.mainPanel);
		
		// each time an image is loaded, get a new one
		this.codeImage.addLoadHandler(new LoadHandler() {
			@Override
			public void onLoad(LoadEvent event) {
				drawChart();
			}
		});
		// draw in the initial chart
		drawChart();
	}

	protected void drawChart() {
		InlineLabel timeLabel = new InlineLabel("Please wait, charts initialising...");
		String dateString = dateFormat.format(new Date());
		timeLabel.setText(dateString);
		
		this.codeImage.setUrl("http://chart.apis.google.com/chart?cht=qr&chl=" + dateString + "&chs=480x480");
		this.codeLabel.setText(dateString);
	}
}

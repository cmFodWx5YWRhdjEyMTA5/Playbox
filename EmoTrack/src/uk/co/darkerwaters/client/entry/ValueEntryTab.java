package uk.co.darkerwaters.client.entry;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import uk.co.darkerwaters.shared.ListItemWidget;
import uk.co.darkerwaters.shared.MirrorLabel;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

public abstract class ValueEntryTab {

	protected final ValueEntryListener listener;
	protected final TrackPointServiceAsync trackPointService;
	protected ListItemWidget listHeader;
	
	private Label atLabel = new Label(EmoTrackConstants.Instance.at());
	private MirrorLabel dateLabel;
	private MirrorLabel timeLabel;
	
	private FlowPanel resultsPanel = new FlowPanel();
	private InlineLabel resultsLabel = new InlineLabel();

	public ValueEntryTab(ValueEntryListener listener, TrackPointServiceAsync trackPointService) {
		this.listener = listener;
		this.trackPointService = trackPointService;
	}

	public abstract Panel getContent();

	public boolean checkLoginStatus() {
		return listener.checkLoginStatus();
	}
	
	public void setActiveItem(boolean isActive) {
		if (isActive) {
			this.listHeader.addStyleName("active");
		}
		else {
			this.listHeader.removeStyleName("active");
		}
	}

	public void setHeader(ListItemWidget item) {
		this.listHeader = item;
	}

	protected FlowPanel createResultsPanel() {
		InlineLabel imageLabel = new InlineLabel(" ");
		imageLabel.addStyleName("fui-upload");
		this.resultsPanel.addStyleName("results-panel");
		this.resultsPanel.add(imageLabel);
		// and the label
		this.resultsPanel.add(this.resultsLabel);
		this.resultsLabel.setText("testing this is working");
		this.resultsPanel.setVisible(false);
		return this.resultsPanel;
	}

	protected FlowPanel createLogValuesButtonPanel(Button button, DateSelectTab dateSelectPanel) {
		FlowPanel logValPanel = new FlowPanel();

		atLabel.addStyleName("entryValue date-label");
		this.dateLabel = new MirrorLabel(dateSelectPanel.getDateSelectLabel(), "entryValue date-label");
		this.timeLabel = new MirrorLabel(dateSelectPanel.getTimeSelectLabel(), "entryValue date-label");
		logValPanel.add(button);
		logValPanel.add(atLabel);
		logValPanel.add(dateLabel);
		logValPanel.add(timeLabel);
		return logValPanel;
	}
	
	protected String getDateString() {
		StringBuilder stringBuilder = new StringBuilder(" ");
		stringBuilder.append(atLabel.getText());
		stringBuilder.append(" ");
		stringBuilder.append(dateLabel.getText());
		stringBuilder.append(" ");
		stringBuilder.append(timeLabel.getText());
		stringBuilder.append(" ");
		return stringBuilder.toString();
	}

	protected void alertResult(String displayString) {
		resultsLabel.setText(displayString);
		resultsPanel.setVisible(true);
        Timer showTimer = new Timer() {
			@Override
			public void run() {
				resultsPanel.setVisible(false);
			}
		};
		showTimer.schedule(5000);
	}
}

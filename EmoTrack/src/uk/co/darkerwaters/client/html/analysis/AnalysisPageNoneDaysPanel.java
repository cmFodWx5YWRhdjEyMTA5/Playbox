package uk.co.darkerwaters.client.html.analysis;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.tracks.StatsResults;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class AnalysisPageNoneDaysPanel {
	
	private final FlowPanel mainPanel = new FlowPanel();
	
	private AnalysisPanelListener listener;

	private FlowPanel contentPanel;

	private Button refreshButton;
	
	public interface AnalysisPanelListener extends ValueEntryListener {
		public void refreshData();
	}

	public AnalysisPageNoneDaysPanel(AnalysisPanelListener listener) {
		this.listener = listener;
		
		mainPanel.addStyleName("sub-page-section");
		this.refreshButton = createRefreshButton();
		mainPanel.add(this.refreshButton);
		mainPanel.add(createTitle(EmoTrackConstants.Instance.noneDaysTitle()));
		
		this.contentPanel = new FlowPanel();
		mainPanel.add(contentPanel);
	}
	
	public Panel getContent() {
		return this.mainPanel;
	}

	private Widget createTitle(String title) {
		Label label = FlatUI.createLabel(title, null, true);
		label.addStyleName("h5");
		return label;
	}

	protected Button createRefreshButton() {
		Button refreshButton = new Button("refresh");
		FlatUI.makeButton(refreshButton, null, EmoTrackConstants.Instance.refreshStatistics());
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AnalysisPageNoneDaysPanel.this.listener.refreshData();
			}
		});
		return refreshButton;
	}

	public void populateGrid(StatsResults result) {
		this.contentPanel.clear();
		if (result == null) {
			FlatUI.createErrorMessage("Unable to get any data for this date range, sorry.", this.refreshButton);
			return;
		}
		int index = 0;
		for (String title : result.getNoneValueTitles()) {
			int value = result.getNoneValue(index++);
			AnalysisPageTileRosette tile = new AnalysisPageTileRosette(title, value);
			this.contentPanel.add(tile.getContent());
		}
	}
	
}


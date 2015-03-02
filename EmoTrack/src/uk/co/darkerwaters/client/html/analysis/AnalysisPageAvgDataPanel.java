package uk.co.darkerwaters.client.html.analysis;

import java.util.ArrayList;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.graph.TrackPointGraphDataHandler.Type;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageNoneDaysPanel.AnalysisPanelListener;
import uk.co.darkerwaters.client.tracks.StatsResults;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AnalysisPageAvgDataPanel {
	
	private final FlowPanel mainPanel = new FlowPanel();
	
	private AnalysisPanelListener listener;

	private FlowPanel contentPanel;

	private Button refreshButton;
	
	private final ArrayList<AnalysisPageTileGraph> tiles = new ArrayList<AnalysisPageTileGraph>();

	public AnalysisPageAvgDataPanel(String title, AnalysisPanelListener listener) {
		this.listener = listener;
		
		mainPanel.addStyleName("sub-page-section");
		this.refreshButton = createRefreshButton();
		mainPanel.add(this.refreshButton);
		mainPanel.add(createTitle(title));
		
		this.contentPanel = new FlowPanel();
		mainPanel.add(contentPanel);
		
		// and refresh our data
		this.listener.refreshData();
	}
	
	protected Button createRefreshButton() {
		Button refreshButton = new Button("refresh");
		FlatUI.makeButton(refreshButton, null, EmoTrackConstants.Instance.refreshStatistics());
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AnalysisPageAvgDataPanel.this.listener.refreshData();
			}
		});
		return refreshButton;
	}
	
	public Panel getContent() {
		return this.mainPanel;
	}

	private Widget createTitle(String title) {
		Label label = FlatUI.createLabel(title, null, true);
		label.addStyleName("h5");
		return label;
	}

	public void populateGrid(StatsResults result) {
		// clear out any existing data
		this.contentPanel.clear();
		this.tiles.clear();
		if (result == null) {
			FlatUI.createErrorMessage("Unable to get any data for this date range, sorry.", this.refreshButton);
			return;
		}
		String[] averageKeys = getAverageKeys(result);
		int index = 0;
		for (String key : averageKeys) {
			// add a tile for this data
			String seriesTitle = getTitleNameFromKey(result, key);
			AnalysisPageTileGraph tile = getTile(seriesTitle);
			tile.addGraphValue(seriesTitle, getAverage(result, index));
			++index;
		}
		
		for (AnalysisPageTileGraph tile : this.tiles) {
			// for each graph we need to add a last item to block out the previous one
			for (String seriesTitle : tile.getSeriesTitles()) {
				tile.addGraphValue(seriesTitle, 0f);
			}
			tile.drawGraph();
		}
	}

	protected abstract float getAverage(StatsResults result, int index);

	protected abstract String getTitleNameFromKey(StatsResults result, String key);

	protected abstract String[] getAverageKeys(StatsResults result);

	private AnalysisPageTileGraph getTile(String title) {
		AnalysisPageTileGraph tile = null;
		Type type = Type.getTypeForSeriesTitle(title);
		for (AnalysisPageTileGraph extant : this.tiles) {
			if (type.equals(Type.sleep) && extant.getType().equals(type)) {
				// the data is for sleep, and this tile is a sleep tile, use this
				tile = extant;
			}
			else if (extant.getTitle().equals(title)) {
				tile = extant;
				break;
			}
		}
		if (null == tile) {
			tile = new AnalysisPageTileGraph(title);
			this.tiles.add(tile);
			this.contentPanel.add(tile.getContent());
		}
		return tile;
	}
	
}


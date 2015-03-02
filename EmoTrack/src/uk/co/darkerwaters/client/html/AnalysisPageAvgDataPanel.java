package uk.co.darkerwaters.client.html;

import java.util.ArrayList;
import java.util.Date;

import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.graph.DataGraphsPanel;
import uk.co.darkerwaters.client.graph.TrackPointGraphDataHandler.Type;
import uk.co.darkerwaters.client.tracks.StatsResults;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import uk.co.darkerwaters.shared.StatsResultsData;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public abstract class AnalysisPageAvgDataPanel {
	
	private final FlowPanel mainPanel = new FlowPanel();
	
	private ValueEntryListener listener;
	
	private final TrackPointServiceAsync trackService;

	private FlowPanel contentPanel;

	private Button refreshButton;
	
	private final ArrayList<AnalysisPageTileGraph> tiles = new ArrayList<AnalysisPageTileGraph>();

	public AnalysisPageAvgDataPanel(String title, TrackPointServiceAsync trackService, ValueEntryListener listener) {
		this.listener = listener;
		this.trackService = trackService;
		
		mainPanel.addStyleName("sub-page-section");
		this.refreshButton = createRefreshButton();
		mainPanel.add(this.refreshButton);
		mainPanel.add(createTitle(title));
		
		this.contentPanel = new FlowPanel();
		mainPanel.add(contentPanel);
		
		// and refresh our data
		refreshData();
	}
	
	public Panel getContent() {
		return this.mainPanel;
	}

	private Widget createTitle(String title) {
		Label label = FlatUI.createLabel(title, null, true);
		label.addStyleName("h5");
		return label;
	}

	protected abstract Button createRefreshButton();

	public void refreshData() {
		// clear out any existing data
		this.contentPanel.clear();
		this.tiles.clear();
		Date to = new Date();
		// get data until today, end of today please, ie tomorrow (o;
		CalendarUtil.addDaysToDate(to, 1);
		Date from = new Date(to.getTime());
		// from a some weeks ago please
		CalendarUtil.addDaysToDate(from, -6 * 7);
		// check our login status to proceed
		if (false == listener.checkLoginStatus()) {
			return;
		}
		
		String fromDayDate = DataGraphsPanel.dayDate.format(from);
		String toDayDate = DataGraphsPanel.dayDate.format(to);
		trackService.getStatsResults(fromDayDate, toDayDate, new AsyncCallback<StatsResultsData>() {
			@Override
			public void onSuccess(StatsResultsData result) {
				populateGrid(new StatsResults(result));
			}
			@Override
			public void onFailure(Throwable caught) {
				populateGrid(null);
			}
		});
	}

	protected void populateGrid(StatsResults result) {
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


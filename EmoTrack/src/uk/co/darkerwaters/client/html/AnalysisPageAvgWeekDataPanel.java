package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.tracks.StatsResults;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

public class AnalysisPageAvgWeekDataPanel extends AnalysisPageAvgDataPanel {

	public AnalysisPageAvgWeekDataPanel(TrackPointServiceAsync trackService, ValueEntryListener listener) {
		super(EmoTrackConstants.Instance.weeklyAverages(), trackService, listener);
	}

	@Override
	protected Button createRefreshButton() {
		Button refreshButton = new Button("refresh");
		FlatUI.makeButton(refreshButton, null, "Refresh the weekly averages calculated from the current data");
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refreshData();
			}
		});
		return refreshButton;
	}	

	@Override
	protected float getAverage(StatsResults result, int index) {
		return result.getWeeklyAverage(index);
	}

	@Override
	protected String getTitleNameFromKey(StatsResults result, String key) {
		return result.getTitleNameFromWeeklyKey(key);
	}

	@Override
	protected String[] getAverageKeys(StatsResults result) {
		return result.getWeeklyAverageKeys();
	}
}

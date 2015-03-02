package uk.co.darkerwaters.client.html.analysis;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageNoneDaysPanel.AnalysisPanelListener;
import uk.co.darkerwaters.client.tracks.StatsResults;

public class AnalysisPageAvgWeekDataPanel extends AnalysisPageAvgDataPanel {

	public AnalysisPageAvgWeekDataPanel(AnalysisPanelListener listener) {
		super(EmoTrackConstants.Instance.weeklyAverages(), listener);
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

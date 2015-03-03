package uk.co.darkerwaters.client.html.analysis;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageNoneDaysPanel.AnalysisPanelListener;
import uk.co.darkerwaters.client.tracks.StatsResults;

public class AnalysisPageAvgMonthDataPanel extends AnalysisPageAvgDataPanel {

	public AnalysisPageAvgMonthDataPanel(AnalysisPanelListener listener) {
		super(EmoTrackConstants.Instance.monthlyAverages(), listener);
	}

	@Override
	protected float getAverage(StatsResults result, int index) {
		return result.getMonthlyAverage(index);
	}

	@Override
	protected String getTitleNameFromKey(StatsResults result, String key) {
		return result.getTitleNameFromMonthlyKey(key);
	}

	@Override
	protected String[] getAverageKeys(StatsResults result) {
		return result.getMonthlyAverageKeys();
	}

	@Override
	protected boolean getIsShowMonths() {
		return true;
	}
}

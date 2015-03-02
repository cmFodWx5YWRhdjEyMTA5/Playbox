package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.EmoTrackResources;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.tracks.TrackPointService;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

public class AnalysisPageContainer extends PageContainer {
	
	private final TrackPointServiceAsync trackService = GWT.create(TrackPointService.class);

	public AnalysisPageContainer() {
		super(EmoTrackResources.INSTANCE.analysisPage().getText());
	}
	
	@Override
	public void initialisePage(ValueEntryListener listener) {
		// make the panels
		RootPanel.get("analysis").add(new AnalysisPageAvgWeekDataPanel(trackService, listener).getContent());
		RootPanel.get("analysis").add(new AnalysisPageAvgMonthDataPanel(trackService, listener).getContent());
		RootPanel.get("analysis").add(new AnalysisPageExportDataPanel(trackService, listener).getContent());
	}

}

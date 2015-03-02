package uk.co.darkerwaters.client.html;

import java.util.Date;

import uk.co.darkerwaters.client.EmoTrackResources;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.graph.DataGraphsPanel;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageAvgMonthDataPanel;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageAvgWeekDataPanel;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageExportDataPanel;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageNoneDaysPanel;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageNoneDaysPanel.AnalysisPanelListener;
import uk.co.darkerwaters.client.tracks.StatsResults;
import uk.co.darkerwaters.client.tracks.TrackPointService;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;
import uk.co.darkerwaters.shared.StatsResultsData;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class AnalysisPageContainer extends PageContainer {
	
	private final TrackPointServiceAsync trackService = GWT.create(TrackPointService.class);
	
	private AnalysisPageNoneDaysPanel noneDays;
	private AnalysisPageAvgWeekDataPanel avgWeek;
	private AnalysisPageAvgMonthDataPanel avgMonth;
	private AnalysisPageExportDataPanel export;

	private AnalysisPanelListener panelListener;

	public AnalysisPageContainer() {
		super(EmoTrackResources.INSTANCE.analysisPage().getText());
	}
	
	@Override
	public void initialisePage(ValueEntryListener listener) {
		// wrap this listener with the analysis one
		this.panelListener = createListener(listener);
		// make the panels
		this.noneDays = new AnalysisPageNoneDaysPanel(panelListener);
		this.avgWeek = new AnalysisPageAvgWeekDataPanel(panelListener);
		this.avgMonth = new AnalysisPageAvgMonthDataPanel(panelListener);
		this.export = new AnalysisPageExportDataPanel(trackService, panelListener);
		RootPanel.get("analysis").add(noneDays.getContent());
		RootPanel.get("analysis").add(avgWeek.getContent());
		RootPanel.get("analysis").add(avgMonth.getContent());
		RootPanel.get("analysis").add(export.getContent());
	}
	
	private AnalysisPanelListener createListener(final ValueEntryListener listener) {
		return new AnalysisPanelListener() {
			@Override
			public void loadingComplete() {
				listener.loadingComplete();
			}
			@Override
			public void handleError(Throwable error) {
				listener.handleError(error);
			}
			@Override
			public void updateVariableValues(String[] titles, int[] values) {
				listener.updateVariableValues(titles, values);
			}
			@Override
			public void updateTrackEntry(TrackPointData newPoint) {
				listener.updateTrackEntry(newPoint);
			}
			@Override
			public void removeTrackEntry(Date trackDate) {
				listener.removeTrackEntry(trackDate);
			}
			@Override
			public boolean checkLoginStatus() {
				return listener.checkLoginStatus();
			}
			@Override
			public void refreshData() {
				AnalysisPageContainer.this.refreshData();
			}
		};
	}
	
	public void refreshData() {
		Date to = new Date();
		// get data until today, end of today please, ie tomorrow (o;
		CalendarUtil.addDaysToDate(to, 1);
		Date from = new Date(to.getTime());
		// from a some weeks ago please
		CalendarUtil.addDaysToDate(from, -6 * 7);
		// check our login status to proceed
		if (false == this.panelListener.checkLoginStatus()) {
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

	protected void populateGrid(StatsResults statsResults) {
		AnalysisPageContainer.this.noneDays.populateGrid(statsResults);
		AnalysisPageContainer.this.avgWeek.populateGrid(statsResults);
		AnalysisPageContainer.this.avgMonth.populateGrid(statsResults);
	}

}

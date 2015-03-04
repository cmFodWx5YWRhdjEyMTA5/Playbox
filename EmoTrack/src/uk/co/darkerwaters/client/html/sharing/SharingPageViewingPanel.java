package uk.co.darkerwaters.client.html.sharing;

import java.util.Date;
import java.util.HashMap;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.EmoTrackListener;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.graph.DataGraphsDataHandler;
import uk.co.darkerwaters.client.graph.DataGraphsPanel;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageAvgMonthDataPanel;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageAvgWeekDataPanel;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageNoneDaysPanel;
import uk.co.darkerwaters.client.html.analysis.AnalysisPageNoneDaysPanel.AnalysisPanelListener;
import uk.co.darkerwaters.client.tracks.StatsResults;
import uk.co.darkerwaters.client.variables.VariablesServiceAsync;
import uk.co.darkerwaters.shared.StatsResultsData;
import uk.co.darkerwaters.shared.TrackPointData;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class SharingPageViewingPanel {
	
	private final FlowPanel mainPanel = new FlowPanel();
	
	private final ValueEntryListener listener;

	private final VariablesServiceAsync variablesService;

	private Button refreshButton;

	private FlowPanel contentPanel;

	private ListBox userSelect;
	
	private Label searchingLabel;

	private FlowPanel searchingPanel;

	private InlineLabel searchingImageLabel;
	
	private HashMap<String, String> userNicknameToIdMap = new HashMap<String, String>();
	
	public SharingPageViewingPanel(VariablesServiceAsync variablesService, ValueEntryListener listener) {
		this.listener = listener;
		this.variablesService = variablesService;
		
		mainPanel.addStyleName("sub-page-section");
		this.refreshButton = createRefreshButton();
		mainPanel.add(createTitle(EmoTrackConstants.Instance.viewedSharedData()));
		
		FlowPanel selectPanel = new FlowPanel();
		
		selectPanel.add(this.refreshButton);
		this.userSelect = FlatUI.createSelect("sharedDataSelect", new String[] {EmoTrackConstants.Instance.selectUserToView()});
		this.userSelect.addStyleName("sharing-control");
		selectPanel.add(this.userSelect);
		selectPanel.add(createSearchingPanel());
		
		this.userSelect.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				showSelectedSharedData();
			}
		});
		
		this.mainPanel.add(selectPanel);
		
		this.contentPanel = new FlowPanel();
		mainPanel.add(contentPanel);
	}

	public void initialisePanel() {
		FlatUI.configureSelect(this.userSelect);
		refreshUsers();
	}
	
	protected Button createRefreshButton() {
		Button refreshButton = new Button("Search for users sharing");
		refreshButton.addStyleName("sharing-control");
		FlatUI.makeButton(refreshButton, null, EmoTrackConstants.Instance.refreshStatistics());
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refreshUsers();
			}
		});
		return refreshButton;
	}

	protected FlowPanel createSearchingPanel() {
		this.searchingImageLabel = new InlineLabel(" ");
		this.searchingImageLabel.getElement().setInnerHTML("<img src=\"images/ajax-loader-dark.gif\"/>");
		// and the label
		this.searchingLabel = FlatUI.createLabel(EmoTrackConstants.Instance.searchingForUsers(), null);
		// add to the panel
		this.searchingPanel = new FlowPanel();
		this.searchingPanel.add(this.searchingImageLabel);
		this.searchingPanel.add(this.searchingLabel);
		this.searchingPanel.setVisible(false);
		return this.searchingPanel;
	}
	
	protected void refreshUsers() {
		if (false == this.listener.checkLoginStatus()) {
			return;
		}
		this.searchingPanel.setVisible(true);
		// search for users that have shared their data with us
		this.variablesService.getUsersSharing(new AsyncCallback<String[]>() {
			@Override
			public void onFailure(Throwable caught) {
				EmoTrack.LOG.severe("Failed to get users sharing: " + caught.getMessage());
				populateUserSelection(null);
			}

			@Override
			public void onSuccess(String[] result) {
				populateUserSelection(result);
			}
		});
		
	}

	protected void populateUserSelection(String[] result) {
		while (this.userSelect.getItemCount() > 1) {
			this.userSelect.removeItem(this.userSelect.getItemCount() - 1);
		}
		this.userSelect.setItemSelected(0, true);
		this.userNicknameToIdMap.clear();
		this.searchingPanel.setVisible(false);
		if (null == result) {
			FlatUI.createErrorMessage("Failed to get the user's sharing data with you, try again?", this.contentPanel);
		}
		FlatUI.createErrorMessage(EmoTrackMessages.Instance.foundSharingUsers(result.length), this.contentPanel);
		for (String userIdAndName : result) {
			String[] split = userIdAndName.split(":");
			if (split.length != 2) {
				EmoTrack.LOG.severe("User ID and name not 2 strings as expected: " + userIdAndName);
			}
			else {
				this.userNicknameToIdMap.put(split[1], split[0]);
				this.userSelect.addItem(split[1]);
			}
		}
		showSelectedSharedData();
	}

	public Panel getContent() {
		return this.mainPanel;
	}

	private Widget createTitle(String title) {
		Label label = FlatUI.createLabel(title, null, true);
		label.addStyleName("h5");
		return label;
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
				//nothing
			}
			@Override
			public boolean isRefreshHandled() {
				return false;
			}
		};
	}

	protected void showSelectedSharedData() {
		this.contentPanel.clear();
		
		// get the current selected user
		int selectedIndex = this.userSelect.getSelectedIndex();
		if (selectedIndex > 0 && selectedIndex < this.userSelect.getItemCount()) {
			String userNickname = this.userSelect.getItemText(selectedIndex);
			String userId = this.userNicknameToIdMap.get(userNickname);
			if (null == userId) {
				EmoTrack.LOG.severe("Failing to select sharing user id for " + userNickname);
				return;
			}
			this.userSelect.setItemText(selectedIndex, userNickname);
			// wrap the listener to the correct type
			AnalysisPanelListener panelListener = createListener(this.listener);
			DataGraphsDataHandler dataHandler = new DataGraphsDataHandler(userId);
			// make the panels
			DataGraphsPanel dataGraphsPanel = new DataGraphsPanel(createChartListener(), dataHandler);
			final AnalysisPageNoneDaysPanel noneDays = new AnalysisPageNoneDaysPanel(panelListener);
			final AnalysisPageAvgWeekDataPanel avgWeek = new AnalysisPageAvgWeekDataPanel(panelListener);
			final AnalysisPageAvgMonthDataPanel avgMonth = new AnalysisPageAvgMonthDataPanel(panelListener);
			
			this.contentPanel.add(dataGraphsPanel);
			this.contentPanel.add(noneDays.getContent());
			this.contentPanel.add(avgWeek.getContent());
			this.contentPanel.add(avgMonth.getContent());
			
			// populate the stats data, graphs do themselves
			dataHandler.getStatsResults(new AsyncCallback<StatsResultsData>() {
				@Override
				public void onSuccess(StatsResultsData result) {
					StatsResults statsResults = new StatsResults(result);
					noneDays.populateGrid(statsResults);
					avgWeek.populateGrid(statsResults);
					avgMonth.populateGrid(statsResults);
					
				}
				@Override
				public void onFailure(Throwable caught) {
					noneDays.populateGrid(null);
					avgWeek.populateGrid(null);
					avgMonth.populateGrid(null);
				}
			});
		}
	}

	private EmoTrackListener createChartListener() {
		return new EmoTrackListener() {
			@Override
			public void handleError(Throwable error) {
				SharingPageViewingPanel.this.listener.handleError(error);
			}
			@Override
			public void loadingComplete() {
				loadingCompleted();
			}
		};
	}

	protected void loadingCompleted() {
		// load all the data into the graphs panel
		
	}
	
}

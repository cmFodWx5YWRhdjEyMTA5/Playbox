package uk.co.darkerwaters.client.html.analysis;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.tracks.TrackPointServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;

public class AnalysisPageAdvancedDataPanel {
	
	private final DisclosurePanel mainPanel = new DisclosurePanel(EmoTrackConstants.Instance.advancedData());
	
	private CheckBox purgeConfirmCheck;
	
	private Button purgeDataButton;

	private final TrackPointServiceAsync trackService;

	private ValueEntryListener listener;

	public AnalysisPageAdvancedDataPanel(TrackPointServiceAsync trackService, ValueEntryListener listener) {
		this.listener = listener;
		this.trackService = trackService;
		
		FlowPanel purgePanel = new FlowPanel();
		
		mainPanel.addStyleName("sub-page-section");
		
		// set the from controls
		this.purgeConfirmCheck = FlatUI.createCheckBox("WARNING: Purging data will clear all tracked data FOREVER, confirm you wish to do this", null);
		purgePanel.add(this.purgeConfirmCheck);
		this.purgeDataButton = new Button("Purge All Tracked Data");
		FlatUI.makeButton(purgeDataButton, null, "This will delete all tracked data from the server FOREVER, no getting it back.");
		purgePanel.add(this.purgeDataButton);
		
		this.purgeConfirmCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				Boolean value = event.getValue();
				purgeDataButton.setEnabled(null != value && value.booleanValue());
			}
		});
		this.purgeDataButton.setEnabled(false);
		this.purgeDataButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				purgeAllTrackedData();
			}
		});
		
		FlowPanel contentPanel = new FlowPanel();
		contentPanel.add(purgePanel);
		
		mainPanel.add(contentPanel);
	}

	protected void purgeAllTrackedData() {
		this.purgeConfirmCheck.setValue(false);
		this.purgeDataButton.setEnabled(false);
		if (false == this.listener.checkLoginStatus()) {
			return;
		}
		trackService.purgeAllData(new AsyncCallback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				FlatUI.createErrorMessage("All " + result.toString() + " items of data have been purged from the server, a <refresh> of this page will show all data is now removed", purgeDataButton);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				FlatUI.createErrorMessage("Sorry the purge failed \"" + caught.getMessage() + "\" please try again?", purgeDataButton);
			}
		});
	}

	public DisclosurePanel getContent() {
		return mainPanel;
	}
}

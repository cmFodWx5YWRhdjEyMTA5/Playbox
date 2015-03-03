package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.EmoTrackConstants;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.variables.VariablesServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;

public class SharingPageSetupPanel {
	
	private final DisclosurePanel mainPanel = new DisclosurePanel(EmoTrackConstants.Instance.setupSharingData());
	
	private final ValueEntryListener listener;

	private final VariablesServiceAsync variablesService;

	private TextBox newUserIdText;
	
	public SharingPageSetupPanel(VariablesServiceAsync variablesService, ValueEntryListener listener) {
		this.listener = listener;
		this.variablesService = variablesService;
		
		mainPanel.addStyleName("sub-page-section");
		
		FlowPanel contentPanel = new FlowPanel();
		this.newUserIdText = new TextBox();
		FlatUI.makeEntryText(this.newUserIdText, "newSharedUserText", "Enter the User ID of the person to share your data with");

		contentPanel.add(this.newUserIdText);
		
		mainPanel.add(contentPanel);
		
		refreshSharedUsers();
	}

	private void refreshSharedUsers() {
		this.variablesService.getSharedUsers(new AsyncCallback<String[]>() {
			@Override
			public void onSuccess(String[] result) {
				// create the users with whom to share data with
				populateGrid(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				FlatUI.createErrorMessage("Failed to get the users with which you are sharing", newUserIdText);
			}
		});
	}

	protected void populateGrid(String[] result) {
		// TODO Auto-generated method stub
		
	}

	public DisclosurePanel getContent() {
		return mainPanel;
	}
	
}

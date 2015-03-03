package uk.co.darkerwaters.client.html;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import uk.co.darkerwaters.client.EmoTrack;
import uk.co.darkerwaters.client.EmoTrackMessages;
import uk.co.darkerwaters.client.EmoTrackResources;
import uk.co.darkerwaters.client.controls.FlatUI;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.variables.VariablesService;
import uk.co.darkerwaters.client.variables.VariablesServiceAsync;

public class SharingPageContainer extends PageContainer {
	
	private VariablesServiceAsync variablesService = GWT.create(VariablesService.class);
	private SharingPageSetupPanel setup;
	
	public SharingPageContainer() {
		super(EmoTrackResources.INSTANCE.sharingPage().getText());
	}
	
	@Override
	public void initialisePage(ValueEntryListener listener) {
		
		this.setup = new SharingPageSetupPanel(variablesService, listener);
		RootPanel.get("sharing").add(this.setup.getContent());
		

		if (listener.checkLoginStatus()) {
			variablesService.getNumberUsers(new AsyncCallback<Integer>() {
				@Override
				public void onSuccess(Integer result) {
					// show the users
					addUsers(result);
				}
				@Override
				public void onFailure(Throwable caught) {
					EmoTrack.LOG.severe("Information page failed to get the number of users: " + caught.getMessage());
				}
			});
		}
	}

	protected void addUsers(Integer result) {
		Label label = FlatUI.createLabel(EmoTrackMessages.Instance.thereAreUsers(result == null ? 0 : result.intValue()), "informationUserCount", false);
		RootPanel.get("information").add(label);
	}

}

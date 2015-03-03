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

public class InformationPageContainer extends PageContainer {
	
	public InformationPageContainer() {
		super(EmoTrackResources.INSTANCE.informationPage().getText());
	}
	
	@Override
	public void initialisePage(ValueEntryListener listener) {
		if (false == listener.checkLoginStatus()) {
			return;
		}
		
		VariablesServiceAsync variablesService = GWT.create(VariablesService.class);
		variablesService.getAllUsers(new AsyncCallback<String[]>() {
			@Override
			public void onSuccess(String[] result) {
				// show the users
				addUsers(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				EmoTrack.LOG.severe("Information page failed to get all the users: " + caught.getMessage());
			}
		});
		
	}

	protected void addUsers(String[] result) {
		Label label = FlatUI.createLabel(EmoTrackMessages.Instance.thereAreUsers(result.length), "informationUserCount", false);
		RootPanel.get("information").add(label);
	}

}

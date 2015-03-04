package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.EmoTrackResources;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;
import uk.co.darkerwaters.client.html.sharing.SharingPageSetupPanel;
import uk.co.darkerwaters.client.html.sharing.SharingPageViewingPanel;
import uk.co.darkerwaters.client.variables.VariablesService;
import uk.co.darkerwaters.client.variables.VariablesServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class SharingPageContainer extends PageContainer {
	
	private VariablesServiceAsync variablesService = GWT.create(VariablesService.class);
	private SharingPageSetupPanel setup;
	private SharingPageViewingPanel viewing;
	
	public SharingPageContainer() {
		super(EmoTrackResources.INSTANCE.sharingPage().getText());
	}
	
	@Override
	public void initialisePage(ValueEntryListener listener) {
		this.setup = new SharingPageSetupPanel(variablesService, listener);
		this.viewing = new SharingPageViewingPanel(variablesService, listener);
		
		RootPanel.get("sharing").add(this.setup.getContent());
		RootPanel.get("sharing").add(this.viewing.getContent());
		
		this.setup.initialisePanel();
		this.viewing.initialisePanel();
	}
}

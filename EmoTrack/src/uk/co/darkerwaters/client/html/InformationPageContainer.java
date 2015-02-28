package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.EmoTrackResources;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;

public class InformationPageContainer extends PageContainer {

	public InformationPageContainer() {
		super(EmoTrackResources.INSTANCE.informationPage().getText());
	}
	
	@Override
	public void initialisePage(ValueEntryListener listener) {
		if (false == listener.checkLoginStatus()) {
			return;
		}
		
	}

}

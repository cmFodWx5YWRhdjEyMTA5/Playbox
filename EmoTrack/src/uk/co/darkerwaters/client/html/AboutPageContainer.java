package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.EmoTrackResources;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;

public class AboutPageContainer extends PageContainer {

	public AboutPageContainer() {
		super(EmoTrackResources.INSTANCE.aboutPage().getText());
	}

	@Override
	public void initialisePage(ValueEntryListener listener) {
		if (false == listener.checkLoginStatus()) {
			return;
		}
		// TODO Auto-generated method stub
		
	}
}

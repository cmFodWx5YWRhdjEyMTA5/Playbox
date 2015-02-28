package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.EmoTrackResources;
import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.RootPanel;

public class AnalysisPageContainer extends PageContainer {

	public AnalysisPageContainer() {
		super(EmoTrackResources.INSTANCE.analysisPage().getText());
	}
	
	@Override
	public void initialisePage(ValueEntryListener listener) {
		
		DisclosurePanel exportPanel = new ExportDataPanel(listener).getContent();
		RootPanel.get("analysis").add(exportPanel);
	}

}

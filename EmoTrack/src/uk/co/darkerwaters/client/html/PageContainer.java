package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.EmoTrackResources;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class PageContainer {
	private final HTMLPanel page;
	
	public PageContainer(String htmlContent) {
		this.page = new HTMLPanel(htmlContent);
	}
	
	public abstract void initialisePage();
	
	public HTMLPanel getPage() {
		return this.page;
	}
}

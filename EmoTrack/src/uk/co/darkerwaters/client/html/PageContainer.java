package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.entry.ValueEntryPanel.ValueEntryListener;

import com.google.gwt.user.client.ui.HTMLPanel;

public abstract class PageContainer {
	private final HTMLPanel page;
	
	public PageContainer(String htmlContent) {
		this.page = new HTMLPanel(htmlContent);
	}
	
	public abstract void initialisePage(ValueEntryListener listener);
	
	public HTMLPanel getPage() {
		return this.page;
	}
}

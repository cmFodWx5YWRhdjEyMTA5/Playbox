package uk.co.darkerwaters.client.html;

import com.google.gwt.user.client.ui.HTMLPanel;

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

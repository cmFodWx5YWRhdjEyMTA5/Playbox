package uk.co.darkerwaters.client.html;

import uk.co.darkerwaters.client.controls.FlatUI;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;

public class AnalysisPageTile {
	
	private String title;
	
	private final FlowPanel content;

	public AnalysisPageTile(String title) {
		this.content = new FlowPanel();
		this.content.addStyleName("analysis-tile");
		this.title = title;
		this.content.add(FlatUI.createLabel(title, null, false));
	}

	public String getTitle() {
		return title;
	}
	
	public Panel getContent() {
		return this.content;
	}

}

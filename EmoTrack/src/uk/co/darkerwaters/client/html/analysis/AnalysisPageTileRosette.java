package uk.co.darkerwaters.client.html.analysis;

import com.google.gwt.user.client.ui.FlowPanel;

public class AnalysisPageTileRosette extends AnalysisPageTile {

	public AnalysisPageTileRosette(String title, int value) {
		super(title);
		
		FlowPanel rosettePanel = new FlowPanel();
		rosettePanel.addStyleName("analysis-rosette");
		
		rosettePanel.getElement().setInnerText(Integer.toString(value));
		getContent().add(rosettePanel);
		
		FlowPanel rosetteExplanPanel = new FlowPanel();
		rosetteExplanPanel.addStyleName("analysis-rosette-text");
		
		rosetteExplanPanel.getElement().setInnerText(Integer.toString(value) + " days without " + title);
		getContent().add(rosetteExplanPanel);
	}

}

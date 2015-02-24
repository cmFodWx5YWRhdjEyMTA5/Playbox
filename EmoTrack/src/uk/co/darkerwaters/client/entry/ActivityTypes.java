package uk.co.darkerwaters.client.entry;

import uk.co.darkerwaters.client.EmoTrackConstants;

public enum ActivityTypes {
	points(EmoTrackConstants.Instance.activityPoints()),
	steps(EmoTrackConstants.Instance.activitySteps());
	
	public final String title;
	
	ActivityTypes(String title) {
		this.title = title;
	}
}
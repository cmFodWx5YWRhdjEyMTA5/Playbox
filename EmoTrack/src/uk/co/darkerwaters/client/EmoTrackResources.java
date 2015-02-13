package uk.co.darkerwaters.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface EmoTrackResources extends ClientBundle {
	public static final EmoTrackResources INSTANCE = GWT.create(EmoTrackResources.class);

    @Source("html/contact.html")
    TextResource contactPage();
    
    @Source("html/about.html")
    TextResource aboutPage();
    
    @Source("html/information.html")
    TextResource informationPage();
    
    @Source("html/analysis.html")
    TextResource analysisPage();
}

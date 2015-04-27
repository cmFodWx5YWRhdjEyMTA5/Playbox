package uk.co.darkerwaters.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class PicSync implements EntryPoint {

	
	// A panel where the thumbnails of uploaded images will be shown
	private FlowPanel panelImages = new FlowPanel();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		new QrJsImage();
		//new QrGoogleImage(RootPanel.get());
		//this.browser = new ImageBrowser(RootPanel.get());
		
		// Attach the image viewer to the document
	    
	}
}

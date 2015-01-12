package uk.co.darkerwaters.client;

import uk.co.darkerwaters.client.ImageButton.ImageButtonClickHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AppearingPanel extends VerticalPanel {
	private enum Icons {
		slide_in("images/icons/slide-in.png"),
		slide_out("images/icons/slide-out.png");
	
		private final String filename;
		private Icons(String s) {
			this.filename = s;
		}
	}
	private boolean isShown = false;
	private ImageButton showButton;
	
	public AppearingPanel() {
		// add the pre-set style name
		this.addStyleName("left-panel");
		
		// add click support to this simple panel
		this.sinkEvents(Event.ONCLICK);
		this.setTitle("Click to reveal...");
		this.addHandler(new ClickHandler(){
	        @Override
	        public void onClick(ClickEvent event) {
	        	if (!AppearingPanel.this.isShown) {
	        		// this is not shown, take any click on the panel to show the item
	        		//toggleAppearance();
	        	}
	        }

	    }, ClickEvent.getType());
		// add the button
		this.showButton = new ImageButton(new String[] {
				Icons.slide_in.filename,
				Icons.slide_out.filename,
		}, "");
		this.showButton.setWidth("48px");
		this.showButton.setHeight("48px");
		this.showButton.addStyleName("float-right");
		this.showButton.addStyleName("fade");
		this.showButton.setCurrentImage(Icons.slide_out.ordinal());
		// listen for a click
		this.showButton.addHandler(new ImageButtonClickHandler() {
			@Override
			public void onImageButtonClick(ClickEvent event, int imageIndex) {
				// toggle the show / hide of this panel
				toggleAppearance();
			}
		});
		this.add(this.showButton);
		this.setCellHeight(this.showButton, "50px");
	}

	protected void toggleAppearance() {
		if (this.isShown) {
    		this.removeStyleName("show");
    		showButton.setCurrentImage(Icons.slide_out.ordinal());
    	}
    	else {
    		this.addStyleName("show");
    		showButton.setCurrentImage(Icons.slide_in.ordinal());
    	}
    	this.isShown = !this.isShown;
	}
}

package uk.co.darkerwaters.client.legacy;

import uk.co.darkerwaters.client.legacy.ImageButton.ImageButtonClickHandler;

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
	
	/** to have the images and things fade in and out, include the following in the css
		.float-right {
			float: right;
		}
		.fade {
			opacity: 1;
			transition: opacity .25s ease-in-out;
			-moz-transition: opacity .25s ease-in-out;
			-webkit-transition: opacity .25s ease-in-out;
		}
		.fade:hover {
			opacity: 0.5;
		}
		.left-panel {
			width: 30%;
			height: 40%;
			border: 5px solid red;
			border-radius: 10px;
			background-color: #fcc;
			position: fixed;
			top: 10%;
			left: -28%;
			-webkit-transition:all .50s ease-in-out;
			-moz-transition:all 0.5s ease-in-out;
			-o-transition:all 0.5s ease-in-out;
			transition:all 0.5s ease-in-out;
		}
		.left-panel:hover {
			left: -25%;
		}
		.left-panel.show {
			left: -10px;
		}
	 */
	
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

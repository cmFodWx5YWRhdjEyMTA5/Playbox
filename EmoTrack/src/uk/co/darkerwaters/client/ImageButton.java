package uk.co.darkerwaters.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class ImageButton extends SimplePanel {
	private final Image[] images;
	private int currentImageIndex = -1;
	private final List<ImageButtonClickHandler> handlers;
	
	public static interface ImageButtonClickHandler {
		public void onImageButtonClick(ClickEvent event, int imageIndex);
	}
	
    public ImageButton(String[] imgSrc, String title) {
    	// create the images
    	this.images = new Image[imgSrc.length];
    	for (int i = 0; i < this.images.length; ++i) {
    		this.images[i] = new Image(imgSrc[i]);
    		this.images[i].setWidth("80%");
    		this.images[i].setHeight("80%");
    	}
    	this.handlers = new ArrayList<ImageButton.ImageButtonClickHandler>();
    	// set the panel title
        this.setTitle(title);
        // set the current image to the first one
        this.setCurrentImage(0);
        // add click support to this simple panel
		this.sinkEvents(Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);
		this.addHandler(new ClickHandler(){
	        @Override
	        public void onClick(ClickEvent event) {
	        	// the user had clicked this button, inform handlers
	        	synchronized (handlers) {
					for (ImageButtonClickHandler handler : handlers) {
						handler.onImageButtonClick(event, currentImageIndex);
					}
				}
	        }
	
	    }, ClickEvent.getType());
		// add the mouse over handler to change the appearance on hover
        this.addHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				// the mouse is over this button, show this
				ImageButton.this.addStyleName("mouse-over");
			}
		}, MouseOverEvent.getType());
        // add the mouse out handler to change the appearance on hover
		addHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				// the mouse is no longer over this button, show this
				ImageButton.this.removeStyleName("mouse-over");
			}
		}, MouseOutEvent.getType());
    }
    
    public boolean addHandler(ImageButtonClickHandler handler) {
    	synchronized (handlers) {
			return this.handlers.add(handler);
    	}
    }
    
    public boolean removeHandler(ImageButtonClickHandler handler) {
    	synchronized (handlers) {
			return this.handlers.remove(handler);
    	}
    }

	public void setCurrentImage(int index) {
		// if there is already an image, remove it
		if (-1 != this.currentImageIndex) {
			this.remove(this.images[this.currentImageIndex]);
		}
		// set the image index showing
		this.currentImageIndex = index;
		// and show the image on this panel
		this.add(this.images[this.currentImageIndex]);
		
	}
}

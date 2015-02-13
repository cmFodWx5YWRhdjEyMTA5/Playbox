package uk.co.darkerwaters.client;

import java.util.HashMap;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class FlatUI {
	private static int sliderId = 0;
	private static boolean isFunctionsExported = false;
	public interface SliderListener {
		public void valueChanged(int value);
	}
	private static HashMap<String, SliderListener> sliderListeners = new HashMap<String, FlatUI.SliderListener>();

	public static void makeEntryText(TextBox control, String id, String placeholderText) {
		makeEntryText(control, id, placeholderText, "text");
	}
	
	public static void makeEntryText(TextBox control, String id, String placeholderText, String type) {
		if (null != id) {
			control.getElement().setId(id);
		}
		control.addStyleName("form-control");
		control.getElement().setAttribute("type", type);
		if (null != placeholderText) {
			control.getElement().setAttribute("placeholder", placeholderText);
		}
	}
	
	public static void makeEntryTextArea(TextArea control, String id, String placeholderText) {
		if (null != id) {
			control.getElement().setId(id);
		}
		control.addStyleName("form-control");
		control.getElement().setAttribute("rows", "5");
		if (null != placeholderText) {
			control.getElement().setAttribute("placeholder", placeholderText);
		}
	}
	
	public static void makeButton(Button control, String id) {
		if (null != id) {
			control.getElement().setId(id);
		}
		control.addStyleName("btn btn-primary");
		//control.addStyleName("btn-default");
	}

	public static void makeCombo(ListBox control, String id) {
		if (null != id) {
			control.getElement().setId(id);
		}
		control.addStyleName("form-control");
		control.getElement().setAttribute("style", "display: block;");
	}
	
	public static SimplePanel makeSlider(String id, SliderListener listener) {
		SimplePanel simplePanel = new SimplePanel();
		if (null == id) {
			 // all sliders need an ID for us to configure it, make one
			id = "slider-" + (++sliderId );
		}
		if (null != listener) {
			sliderListeners.put(id, listener);
		}
		simplePanel.getElement().setId(id);
		simplePanel.addStyleName("flat-slider");
		return simplePanel;
	}
	
	public static void configureSlider(SimplePanel slider) {
		if (false == isFunctionsExported) {
			//exportSliderChange();
			isFunctionsExported = true;
		}
		String id = slider.getElement().getId();
		configureSlider("#" + id);
		// send an initial change message
		sliderValueChange(0, id);
	}
	
	public static void sliderValueChange(int value, String id) {
		// get the listener
		SliderListener listener = sliderListeners.get(id.replace("#", ""));
		if (null != listener) {
			// inform it of the new value
			listener.valueChanged(value);
		}
	}
	
	private static native void configureSlider(String sliderID) /*-{
	    var $slider = $wnd.$(sliderID);
	    var $thatWnd = $wnd;
	    if ($slider.length > 0) {
	      $slider.slider({
	        max: 10,
	        step: 1,
	        value: 0,
	        orientation: 'horizontal',
	        range: 'min'
	      }).addSliderSegments();
	    }
      	$slider.slider({
    		change: function(event, ui) {
    			@uk.co.darkerwaters.client.FlatUI::sliderValueChange(ILjava/lang/String;)(ui.value, sliderID);
   			} 
      	});​
	}-*/;
	
	public static native void exportSliderChange() /*-{
	    var that = this;
	    $wnd.sliderValueChange = $entry(function(value, id) {
	      @uk.co.darkerwaters.client.FlatUI::sliderValueChange(ILjava/lang/String;)(value, id);
	    });
	}-*/;
}

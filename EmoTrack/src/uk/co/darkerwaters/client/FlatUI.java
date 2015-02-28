package uk.co.darkerwaters.client;

import java.util.HashMap;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

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
	
	public static void makeButton(Button control, String id, String tooltip) {
		if (null != id) {
			control.getElement().setId(id);
		}
		control.addStyleName("btn btn-primary");
		if (null != tooltip) {
			makeTooltip(control, tooltip);
		}
		//control.addStyleName("btn-default");
	}
	
	public static void makeTooltip(Widget control, String tooltip) {
		control.getElement().setAttribute("data-toggle", "tooltip");
		//TODO somehow add title to the HTML without any content at all to make FlatUI tooltips work <button title data-placement="top" etc...
		control.getElement().setAttribute("title", tooltip);
		control.getElement().setAttribute("data-original-title", tooltip);
		control.getElement().setAttribute("data-placement", "top");
	}
	
	public static Label createLabel(String content, String id, boolean isInline) {
		if (isInline) {
			return createLabel(content, id);
		}
		else {
			Label control = new Label(content);
			if (null != id) {
				control.getElement().setId(id);
			}
			return control;
		}
	}

	public static InlineLabel createLabel(String content, String id) {
		InlineLabel control = new InlineLabel(content);
		if (null != id) {
			control.getElement().setId(id);
		}
		return control;
	}

	public static void makeCombo(ListBox control, String id) {
		if (null != id) {
			control.getElement().setId(id);
		}
		control.addStyleName("form-control");
		control.getElement().setAttribute("style", "display: block;");
	}
	
	public static HTML createHeader(int headerLevel, String headerContent) {
		HTML headingElement= new HTML();
		headingElement.setHTML("<h" + headerLevel + ">" + headerContent);        
		return headingElement;
	}
	
	public static ListBox createSelect(String id, String[] options) {
		ListBox select = new ListBox();
		if (null == id) {
			 // all selectors need an ID for us to configure it, make one
			id = "selector-" + (++sliderId );
		}
		select.getElement().setId(id);
		select.addStyleName("form-control select select-primary select-block mbl");
		/*StringBuilder innerHtml = new StringBuilder();
		for (String option : options) {
			HTML optionHtml = new HTML("<option value=\"" + option + "\"");
			innerHtml.append(optionHtml.getHTML());
		}
		select.getElement().setInnerHTML(innerHtml.toString());*/
		if (null != options) {
			for (String option : options) {
				select.addItem(option);
			}
		}
		return select;
		/*<select class="form-control select select-primary select-block mbl">
		  <optgroup label="Profile">
		    <option value="0">My Profile</option>
		    <option value="1">My Friends</option>
		  </optgroup>
		  <optgroup label="System">
		    <option value="2">Messages</option>
		    <option value="3">My Settings</option>
		    <option value="4">Logout</option>
		  </optgroup>
		</select>
		*/
	}
	
	public static void configureSelect(ListBox select) {
		String id = select.getElement().getId();
		configureSelect("#" + id);
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
	
	public static void configureSlider(SimplePanel slider, int maxValue) {
		if (false == isFunctionsExported) {
			//exportSliderChange();
			isFunctionsExported = true;
		}
		String id = slider.getElement().getId();
		configureSlider("#" + id, maxValue);
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
	
	private static native void configureSlider(String sliderID, int maxVal) /*-{
	    var $slider = $wnd.$(sliderID);
	    var $thatWnd = $wnd;
	    if ($slider.length > 0) {
	      $slider.slider({
	        max: maxVal,
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
	
	private static native void exportSliderChange() /*-{
	    var that = this;
	    $wnd.sliderValueChange = $entry(function(value, id) {
	      @uk.co.darkerwaters.client.FlatUI::sliderValueChange(ILjava/lang/String;)(value, id);
	    });
	}-*/;
	
	public static native void configureSelect(String selectID) /*-{
	    var $select = $wnd.$(selectID);
	    $select.select2();​
	}-*/;

	
}

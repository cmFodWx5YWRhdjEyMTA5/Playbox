package uk.co.darkerwaters.client;

import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class FlatUI {
	private static final Integer K_ERROR_SHOWTIME = new Integer(10000);
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
		//control.getElement().setAttribute("data-original-title", tooltip);
		//control.getElement().setAttribute("data-placement", "top");
	}
	
	public static boolean addControlForSource(Widget source, final Widget control, Integer showTime) {
		Widget parent = source;
		while (parent != null && false == parent instanceof Panel) {
			parent = parent.getParent();
		}
		boolean isAdded = false;
		if (null != parent) {
			((Panel)parent).add(control);
			isAdded = true;
			// if there is a limit, remove after it
			if (null != showTime) {
				Timer timer = new Timer() {
					@Override
					public void run() {
						// when running, remove the control from the parent
						if (null != control.getParent()) {
							control.removeFromParent();
						}
					}
				};
				timer.schedule(showTime.intValue());
			}
		}
		// return the success of this creation
		return isAdded;
	}
	
	public static Panel createErrorMessage(String content, Widget source) {
		Panel message = createPopup(content, source.getAbsoluteLeft(), source.getAbsoluteTop() + source.getOffsetHeight(), true, true);
		addControlForSource(source, message, K_ERROR_SHOWTIME);
		return message;
	}
	
	public static Panel createTooltipMessage(String content, Widget source) {
		final Panel message = createPopup(content, source.getAbsoluteLeft(), source.getAbsoluteTop(), false, false);
		source.addHandler(new MouseOverHandler() {
			@Override
			public void onMouseOver(MouseOverEvent event) {
				message.setVisible(true);
			}
		}, MouseOverEvent.getType());
		source.addHandler(new MouseOutHandler() {
			@Override
			public void onMouseOut(MouseOutEvent event) {
				message.setVisible(true);
			}
		}, MouseOutEvent.getType());
		source.sinkEvents(Event.ONMOUSEOUT | Event.ONMOUSEOVER);
		message.setVisible(true);
		addControlForSource(source, message, null);
		return message;
	}
	
	public static Panel createPopup(String content, int x, int y, boolean showWaitCursor, boolean showCloseButton) {
		final FlowPanel container = new FlowPanel();
		container.addStyleName("demo-tooltips");
		FlowPanel tooltip = new FlowPanel();
		tooltip.addStyleName("tooltip fade bottom in");
		tooltip.getElement().setAttribute("style", "top: " + y + "px; left: " + x + "px; display: block;");
		tooltip.getElement().setId("panel-tooltip");
		tooltip.getElement().setAttribute("role", "tooltip");
		FlowPanel arrow = new FlowPanel();
		arrow.addStyleName("tooltip-arrow");
		FlowPanel inner = new FlowPanel();
		inner.addStyleName("tooltip-inner");
		
		if (showCloseButton) {
			Button closeButton = new Button();
			FlatUI.makeButton(closeButton, null, "Close this message");
			closeButton.addStyleName("popup-close");
			closeButton.getElement().setInnerHTML("<img src=\"images/gtk-close.png\" style=\"position:relative; top:-12px; left: -4px;\"/>");
			closeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (null != container.getParent()) {
						container.removeFromParent();
					}
				}
			});
			inner.add(closeButton);
		}
		inner.add(FlatUI.createLabel(content, null));
		if (showWaitCursor) {
			Image image = new Image("images/ajax-loader-dark.gif");
			image.addStyleName("popup-wait");
			inner.add(image);
		}
		tooltip.add(arrow);
		tooltip.add(inner);
		container.add(tooltip);

		/*
		<div class="demo-tooltips">
			<div class="tooltip fade top in" role="tooltip" id="tooltip635534" style="top: -191px; left: 0px; display: block;">
				<div class="tooltip-arrow">
				</div>
				<div class="tooltip-inner">Here is a sample of a long dark tooltip. Hell yeah.
				</div>
			</div>
		</div>
		*/
		return container;
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

package uk.co.darkerwaters.client.variables.slider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.kiouri.sliderbar.client.view.SliderBarHorizontal;

public class VariableValueSlider extends SliderBarHorizontal {
	
	ImagesVariableValueSlider images = GWT.create(ImagesVariableValueSlider.class);

	public VariableValueSlider(int maxValue, String width) {
		setLessWidget(new Image(images.less()) );
		setScaleWidget(new Image(images.scaleh().getUrl()), 20);
		setMoreWidget(new Image(images.more()));
		setDragWidget(new Image(images.drag()));
		this.setWidth(width);
		this.setMaxValue(maxValue);
	}

	interface ImagesVariableValueSlider extends ClientBundle{
		
		@Source("draghthin.png")
		ImageResource drag();

		@Source("less.png")
		ImageResource less();

		@Source("more.png")
		ImageResource more();

		@Source("scalehthin.png")
		DataResource scaleh();		
	}	
		
}

package uk.co.darkerwaters.shared;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Label;

public class MirrorLabel extends Label {
	private final Label toMirror;
	
	private static final List<MirrorLabel> mirrors = new ArrayList<MirrorLabel>();

	public MirrorLabel(Label toMirror, String... styles) {
		this.toMirror = toMirror;
		for (String style : styles) {
			this.addStyleName(style);
		}
		this.setText(toMirror.getText());
		mirrors.add(this);
	}
	
	public static void update() {
		for (MirrorLabel mirror : mirrors) {
			mirror.setText(mirror.toMirror.getText());
		}
	}
}

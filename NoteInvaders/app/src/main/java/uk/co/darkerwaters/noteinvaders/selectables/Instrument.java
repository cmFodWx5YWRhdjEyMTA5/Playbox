package uk.co.darkerwaters.noteinvaders.selectables;

import android.app.Activity;

import uk.co.darkerwaters.noteinvaders.SelectableItem;

public class Instrument extends SelectableItem {

    public Instrument(Activity context, String name, int thumbnail) {
        super(context, name, thumbnail);
    }

    public String getSubtitle() {
        return "Some progress";
    }
}

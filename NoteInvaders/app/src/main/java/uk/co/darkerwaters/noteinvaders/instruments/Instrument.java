package uk.co.darkerwaters.noteinvaders.instruments;

import uk.co.darkerwaters.noteinvaders.SelectableItem;

public class Instrument extends SelectableItem {

    public Instrument(String name, int thumbnail) {
        super(name, thumbnail);
    }

    public String getSubtitle() {
        return "Some progress";
    }
}

package uk.co.darkerwaters.staveinvaders.input;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.notes.Chord;

public abstract class Input {
    protected final Application application;

    public Input(Application application) {
        this.application = application;
    }

    public abstract void shutdown();

    protected void onNoteDetected(Chord chord, boolean isPressed, float probability) {
        // do some debugging for now
        if (chord == null) {
            Log.error("NULL note depressed");
        }
        else if (isPressed) {
            Log.debug("note " + chord.getTitle() + " depressed with " + probability + " probability");
        }
        else {
            Log.debug("note " + chord.getTitle() + " released");
        }
        // inform all the listeners on the handler of this action
        InputSelector inputSelector = this.application.getInputSelector();
        if (null != inputSelector) {
            inputSelector.onNoteDetected(this, chord, isPressed, probability);
        }
    }
}

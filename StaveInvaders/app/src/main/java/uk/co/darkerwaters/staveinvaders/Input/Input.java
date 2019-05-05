package uk.co.darkerwaters.staveinvaders.input;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.notes.Chord;

public abstract class Input {
    protected final Application application;

    public Input(Application application) {
        this.application = application;
    }

    public abstract void shutdown();

    protected void signalIsProcessing() {
        // called when the derived class is processing something, inform the listeners
        InputSelector inputSelector = this.application.getInputSelector();
        inputSelector.signalIsProcessing();
    }

    protected void setStatus(InputSelector.Status status) {
        InputSelector inputSelector = this.application.getInputSelector();
        if (null != inputSelector) {
            inputSelector.setStatus(status);
        }
    }

    public void initialiseConnection() {
        // initialise our connection here
        setStatus(InputSelector.Status.unknown);
    }

    public int getStatusDrawable(InputSelector.Status status) {
        switch (status) {
            case unknown:
            case connecting:
                return R.drawable.ic_baseline_sync_24px;
            case connected:
                return R.drawable.ic_baseline_check_circle_24px;
            case disconnecting:
                return R.drawable.ic_baseline_sync_24px;
            case disconnected:
                return R.drawable.ic_baseline_sync_disabled_24px;
            case error: default:
                return R.drawable.ic_baseline_sync_problem_24px;
        }
    }

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

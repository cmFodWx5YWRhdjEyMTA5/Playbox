package uk.co.darkerwaters.noteinvaders.state.input;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.darkerwaters.noteinvaders.UsbSetupActivity;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;

public abstract class InputConnection {

    protected final Activity context;
    private final List<InputConnectionInterface> listeners;

    protected InputConnection(Activity context) {
        this.context = context;
        this.listeners = new ArrayList<InputConnectionInterface>();
        // first thing to do is to create the notes
        if (null == Notes.instance()) {
            Notes.CreateNotes(this.context);
        }
    }

    public boolean addListener(InputConnectionInterface listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(InputConnectionInterface listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    public abstract boolean startConnection();

    public abstract boolean stopConnection();

    protected void informNoteDetection(Note note, boolean isDetection, float probability, int frequency) {
        synchronized (InputConnection.this.listeners) {
            for (InputConnectionInterface listener : InputConnection.this.listeners) {
                listener.onNoteDetected(note, isDetection, probability, frequency);
            }
        }
    }
}

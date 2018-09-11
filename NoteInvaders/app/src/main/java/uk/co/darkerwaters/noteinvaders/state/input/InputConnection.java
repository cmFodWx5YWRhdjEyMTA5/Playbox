package uk.co.darkerwaters.noteinvaders.state.input;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;

public abstract class InputConnection {

    private final static int K_NOTEREDUCTIONDELAY = 500;

    protected final Activity context;
    private final List<InputConnectionInterface> listeners;

    private final Map<Note, Integer> noteDetectionFrequency;

    private Thread reductionThread = null;
    private volatile boolean isThreadStarted = false;
    private final Object waitingObject = new Object();

    protected InputConnection(Activity context) {
        this.context = context;
        this.listeners = new ArrayList<InputConnectionInterface>();
        this.noteDetectionFrequency = new HashMap<Note, Integer>(Notes.instance().getNoteCount());
    }

    public void initialiseConnection() {
        // first thing to do is to create the notes
        if (null == Notes.instance()) {
            Notes.CreateNotes(this.context);
        }
        Notes notes = Notes.instance();
        for (int i = 0; i < notes.getNoteCount(); ++i) {
            // put all the notes into the map and the detection frequency of zero
            Note note = notes.getNote(i);
            synchronized (this.noteDetectionFrequency) {
                this.noteDetectionFrequency.put(note, new Integer(0));
            }
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

    protected boolean startReductionThread() {
        // start up the reduction thread this class needs to diminish the detection frequencies
        this.isThreadStarted = true;
        this.reductionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadStarted) {
                    synchronized (InputConnection.this.noteDetectionFrequency) {
                        for (Map.Entry<Note, Integer> noteFreq : InputConnection.this.noteDetectionFrequency.entrySet()) {
                            Integer value = noteFreq.getValue();
                            if (value > 0) {
                                value = value - 1;
                                noteFreq.setValue(value);
                                // inform the listeners of this change
                                informNoteDetection(noteFreq.getKey(), -1, value, false);
                            }
                        }
                    }
                    try {
                        synchronized (InputConnection.this.waitingObject) {
                            InputConnection.this.waitingObject.wait(K_NOTEREDUCTIONDELAY);
                        }
                    } catch (InterruptedException e) {
                        // fine, will exit the thread
                    }
                }
            }
        });
        // start the reducer thread
        this.reductionThread.start();
        // return if the thread was started ok
        return this.isThreadStarted;
    }

    protected boolean stopReductionThread() {
        if (this.isThreadStarted) {
            // show we stopped this
            this.isThreadStarted = false;
            synchronized (this.waitingObject) {
                this.waitingObject.notifyAll();
            }
        }
        return !this.isThreadStarted;
    }

    protected void informNoteDetection(Note note, float probability, int frequency, boolean isPitched) {
        synchronized (InputConnection.this.listeners) {
            for (InputConnectionInterface listener : InputConnection.this.listeners) {
                listener.onNoteDetected(note, probability, frequency, isPitched);
            }
        }
    }

    protected int incrementNoteFrequency(Note note) {
        int noteFreq = -1;
        synchronized (this.noteDetectionFrequency) {
            // get the frequency and add one
            noteFreq = this.noteDetectionFrequency.get(note) + 1;
            // put the new value into the map
            this.noteDetectionFrequency.put(note, noteFreq);
        }
        return noteFreq;
    }

    protected boolean clearNoteFrequencyData(boolean isInformListeners) {
        boolean isFrequencyChanged = false;
        // go through the map and set all the data to zero, return if any wasn't zero in the first place
        synchronized (this.noteDetectionFrequency) {
            for (Map.Entry<Note, Integer> noteFreq : this.noteDetectionFrequency.entrySet()) {
                Integer value = noteFreq.getValue();
                if (value > 0) {
                    // this is a change
                    isFrequencyChanged = false;
                    // reset the value
                    value = 0;
                    noteFreq.setValue(value);
                    if (isInformListeners) {
                        // inform the listeners
                        informNoteDetection(noteFreq.getKey(), -1, value, false);
                    }
                }
            }
        }
        // return if something is different
        return isFrequencyChanged;
    }
}

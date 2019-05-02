package uk.co.darkerwaters.staveinvaders;

import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Notes;

public class Application extends android.app.Application {

    private Log log = null;
    private Settings settings = null;
    private InputSelector input = null;
    private Notes notes = null;
    private Chords singleChords = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // create the log and the settings so can access our state
        this.log = Log.CreateLog(this);
        this.settings = new Settings(this);
        this.input = new InputSelector(this);

        Log.debug("Application initialised...");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // clear all the notes from memory to help out
        clearNotes();
    }

    public InputSelector getInputSelector() {
        return this.input;
    }

    @Override
    public void onTerminate() {
        // close things down
        this.settings.commitChanges();
        this.input.disconnect();

        Log.debug("Application terminated...");
        // set everything to null, no longer around
        this.settings = null;
        this.input = null;
        clearNotes();
        this.log = null;

        // and terminate the app
        super.onTerminate();
    }

    public Log getLog() {
        return this.log;
    }

    public Settings getSettings() {
        // return the settings (exist as long as the application does)
        return this.settings;
    }

    private void clearNotes() {
        synchronized (this) {
            this.notes = null;
            this.singleChords = null;
        }
    }

    public Chords getSingleChords() {
        synchronized (this) {
            if (this.singleChords == null) {
                // create them
                getNotes();
            }
            return this.singleChords;
        }
    }

    public Notes getNotes() {
        synchronized (this) {
            if (this.notes == null) {
                this.notes = Notes.CreateNotes(this);
            }
            if (this.singleChords == null) {
                this.singleChords = Chords.CreateSingleChords(this.notes);
            }
            return this.notes;
        }
    }
}

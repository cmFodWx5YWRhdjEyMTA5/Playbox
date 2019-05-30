package uk.co.darkerwaters.staveinvaders;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.util.SizeF;

import uk.co.darkerwaters.staveinvaders.activities.MainActivity;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Scores;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Notes;

public class Application extends android.app.Application {

    private Log log = null;
    private Settings settings = null;
    private Scores scores = null;
    private InputSelector input = null;
    private Notes notes = null;
    private Chords singleChords = null;
    private MainActivity mainActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // create the log and the settings so can access our state
        this.log = Log.CreateLog(this);
        this.settings = new Settings(this);
        this.scores = new Scores(this);
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

    public SizeF getDisplaySize(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return new SizeF(dpWidth, dpHeight);
    }

    @Override
    public void onTerminate() {
        // close things down
        this.settings.commitChanges();
        this.input.disconnect();

        Log.debug("Application terminated...");
        // set everything to null, no longer around
        this.settings = null;
        this.scores = null;
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

    public Scores getScores() {
        // return the active scores
        return this.scores;
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

    public void setMainActivity(MainActivity activity) {
        // set the activity to use to set things up
        this.mainActivity = activity;
        // set the input type to set this up
        this.input.changeInputType(getSettings().getActiveInput());
    }

    public MainActivity getMainActivity() {
        return this.mainActivity;
    }
}

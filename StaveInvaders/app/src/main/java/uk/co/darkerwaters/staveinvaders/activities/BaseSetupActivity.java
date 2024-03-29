package uk.co.darkerwaters.staveinvaders.activities;

import android.support.v7.app.AppCompatActivity;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.input.Input;
import uk.co.darkerwaters.staveinvaders.input.InputMidi;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Range;
import uk.co.darkerwaters.staveinvaders.views.KeysView;
import uk.co.darkerwaters.staveinvaders.views.PianoPlaying;

public abstract class BaseSetupActivity extends AppCompatActivity implements
        InputSelector.InputListener,
        KeysView.IKeysViewListener,
        InputMidi.MidiListener {

    private PianoPlaying piano = null;
    protected Application application;

    protected void initialiseSetupActivity() {
        // get the application for reference
        this.application = (Application) this.getApplication();

        this.piano = findViewById(R.id.microphone_setup_piano);
        this.piano.setIsForcePiano(true);

        Chords singleChords = this.application.getSingleChords();
        Range noteRange = new Range(singleChords.getChord("A1"), singleChords.getChord("C8"));
        this.piano.setNoteRange(noteRange);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // add the listener back to the piano
        this.piano.addListener(this);
        // listen to changes in connection
        this.application.getInputSelector().addListener(this);
    }

    @Override
    protected void onPause() {
        // remove us as a listener
        this.piano.removeListener(this);
        this.application.getInputSelector().removeListener(this);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        this.piano.closeView();
        super.onDestroy();
    }

    @Override
    public void onNoteDetected(Settings.InputType type, Chord chord, boolean isDetection, float probability) {
        // add to our range of notes we can detect
        if (isDetection && probability > Input.K_DETECTION_PROBABILITY_THRESHOLD) {
            // depress this chord
            this.piano.depressNote(chord);
        }
        else {
            this.piano.releaseNote(chord);
            noteReleased(chord);
        }
        // invalidate the view, the piano released a note
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                piano.invalidate();
            }
        });
    }

    @Override
    public void noteReleased(Chord chord) {
        // invalidate the view, the piano released a note
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                piano.invalidate();
            }
        });
    }

    @Override
    public void noteDepressed(Chord chord) {
        // interesting, this is from the piano so we don't really care
    }

}

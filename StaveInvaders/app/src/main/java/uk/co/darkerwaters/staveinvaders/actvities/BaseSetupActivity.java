package uk.co.darkerwaters.staveinvaders.actvities;

import android.support.v7.app.AppCompatActivity;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.input.InputMidi;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.views.PianoPlaying;
import uk.co.darkerwaters.staveinvaders.views.PianoView;

public abstract class BaseSetupActivity extends AppCompatActivity implements
        InputSelector.InputListener,
        PianoView.IPianoViewListener,
        InputMidi.MidiListener {

    protected PianoPlaying piano = null;
    protected Application application;

    protected void initialiseSetupActivity() {
        // get the application for reference
        this.application = (Application) this.getApplication();

        this.piano = (PianoPlaying) findViewById(R.id.microphone_setup_piano);
        this.piano.setIsDrawNoteName(false);
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
        if (isDetection && probability > 50f) {
            // depress this chord
            this.piano.depressNote(chord);
            // invalidate the view, the piano released a note
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    piano.invalidate();
                }
            });
        }
    }

    @Override
    public void pianoViewSizeChanged(int w, int h, int oldw, int oldh) {
        // interesting but don't really care
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

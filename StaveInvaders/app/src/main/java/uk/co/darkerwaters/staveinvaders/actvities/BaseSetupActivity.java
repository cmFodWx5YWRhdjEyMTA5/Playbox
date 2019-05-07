package uk.co.darkerwaters.staveinvaders.actvities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.input.InputMidi;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Range;
import uk.co.darkerwaters.staveinvaders.views.PianoTouchable;
import uk.co.darkerwaters.staveinvaders.views.PianoView;

public abstract class BaseSetupActivity extends AppCompatActivity implements
        InputSelector.InputListener,
        PianoView.IPianoViewListener,
        InputMidi.MidiListener {

    protected PianoTouchable piano = null;
    private TextView pianoRangeText = null;

    protected Application application;

    private float minPitchDetected = -1f;
    private float maxPitchDetected = -1f;

    protected void initialiseSetupActivity() {
        // get the application for reference
        this.application = (Application) this.getApplication();

        this.piano = (PianoTouchable) findViewById(R.id.microphone_setup_piano);
        this.pianoRangeText = (TextView) findViewById(R.id.piano_range_text);

        // show the range of the piano
        this.piano.setIsAllowTouch(false);
        setPianoRange(this.piano.getDefaultNoteRange());
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

    protected void setPianoRange(Range range) {
        if (null != range) {
            this.piano.setNoteRange(range, null);
        }
        // update the text and view now on the main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // invalidate the view to display it okay
                piano.invalidate();
                // show the range of the piano
                pianoRangeText.setText(piano.getRangeText());
            }
        });
    }

    @Override
    protected void onDestroy() {
        this.piano.closeView();
        super.onDestroy();
    }

    @Override
    public void onNoteDetected(Settings.InputType type, Chord chord, boolean isDetection, float probability) {
        // add to our range of notes we can detect
        if (isDetection && probability > 1f) {
            addDetectedPitch(chord);
            setPianoRange(null);
        }
    }

    private void addDetectedPitch(Chord chord) {
        if (null != chord) {
            float pitch = chord.getLowest().getFrequency();
            // add to the range of pitch we can detect
            if (minPitchDetected < 0 || pitch < minPitchDetected) {
                minPitchDetected = pitch;
            }
            pitch = chord.getHighest().getFrequency();
            if (maxPitchDetected < 0 || pitch > maxPitchDetected) {
                maxPitchDetected = pitch;
            }
            // depress this chord
            this.piano.depressNote(chord);
            // set the detected pitch on the piano we are showing
            this.piano.setNoteRange(minPitchDetected, maxPitchDetected, null);
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

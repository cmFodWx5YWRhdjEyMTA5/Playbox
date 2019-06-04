package uk.co.darkerwaters.staveinvaders.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.input.Input;
import uk.co.darkerwaters.staveinvaders.input.InputKeys;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Note;
import uk.co.darkerwaters.staveinvaders.notes.Range;
import uk.co.darkerwaters.staveinvaders.sounds.SoundPlayer;
import uk.co.darkerwaters.staveinvaders.views.KeysView;
import uk.co.darkerwaters.staveinvaders.views.PianoTouchable;

public class KeyboardSetupActivity extends AppCompatActivity implements KeysView.IKeysViewListener {

    private static final int K_MIN_LETTERS = 5;
    private static final int K_MAX_LETTERS = 20;

    private Application application;
    private InputKeys input;

    private PianoTouchable piano;
    private Switch switchPiano;
    private Switch switchLettersPiano;
    private Switch switchLetters;
    private View pianoExtraLayout;
    private View lettersExtraLayout;

    private EditText startLetter;
    private EditText endLetter;
    private Range lettersRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_setup);

        // get the application for reference
        this.application = (Application) this.getApplication();

        this.piano = findViewById(R.id.microphone_setup_piano);
        this.pianoExtraLayout = findViewById(R.id.pianoExtraLayout);
        this.switchLetters = findViewById(R.id.lettersChoiceSwitch);
        this.lettersExtraLayout = findViewById(R.id.lettersExtraLayout);
        this.switchPiano = findViewById(R.id.pianoChoiceSwitch);
        this.switchLettersPiano = findViewById(R.id.lettersOnPianoSwitch);

        this.startLetter = findViewById(R.id.startLetter);
        this.endLetter = findViewById(R.id.endLetter);

        // setup the controls
        setControlsFromData();

        // listen for changes in the data
        this.switchPiano.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings settings = application.getSettings();
                settings.setIsKeyInputPiano(b).commitChanges();
                setControlsFromData();
            }
        });
        this.switchLetters.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings settings = application.getSettings();
                settings.setIsKeyInputPiano(!b).commitChanges();
                setControlsFromData();
            }
        });
        this.switchLettersPiano.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings settings = application.getSettings();
                settings.setIsShowPianoLetters(b).commitChanges();
                setControlsFromData();
            }
        });

        findViewById(R.id.agButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLetterRange("A3", "G4");
            }
        });
        findViewById(R.id.cButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLetterRange("C4", "B4");
            }
        });
        findViewById(R.id.ccButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLetterRange("C4", "C5");
            }
        });

        findViewById(R.id.startLessButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // reduce the start letter by one
                changeLetterRange(-1, 0);
            }
        });
        findViewById(R.id.startMoreButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // increase the start letter by one
                changeLetterRange(1, 0);
            }
        });
        findViewById(R.id.endLessButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // reduce the end letter by one
                changeLetterRange(0, -1);
            }
        });
        findViewById(R.id.endMoreButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // increase the end letter by one
                changeLetterRange(0, 1);
            }
        });

    }

    private void setLetterRange(String startNote, String endNote) {
        Settings settings = this.application.getSettings();
        Chords singleChords = this.application.getSingleChords();
        // set this range to that specified
        this.lettersRange.setStart(singleChords.getChord(startNote));
        this.lettersRange.setEnd(singleChords.getChord(endNote));
        // set this on the application as required
        settings.setPianoLettersRange(this.lettersRange).commitChanges();
        // show the data on the page here
        setLettersData();
    }

    private void changeLetterRange(int startDelta, int endDelta) {
        Settings settings = this.application.getSettings();
        Chords singleChords = this.application.getSingleChords();
        // unpack the range
        Note startNote = this.lettersRange.getStart().root();
        int startIndex = singleChords.getChordIndex(startNote);
        Note endNote = this.lettersRange.getEnd().root();
        int endIndex = singleChords.getChordIndex(endNote);

        // check our data
        int noteCount = endIndex - startIndex + 1;
        if (noteCount < K_MIN_LETTERS && (startDelta > 0 || endDelta < 0)) {
            // don't allow the shrinking below this min size
            return;
        }
        else if (noteCount > K_MAX_LETTERS && (startDelta < 0 || endDelta > 0)) {
            // don't allow the growing larger than this max size
            return;
        }

        // and make the movements
        if (startDelta != 0) {
            // are moving the start, find the note index and change it here
            // change this index
            Chord newStart = null;
            while (startIndex >= 0 && startIndex < singleChords.getSize()) {
                startIndex += startDelta;
                newStart = singleChords.getChord(startIndex);
                if (false == newStart.hasSharp() && false == newStart.hasFlat()) {
                    // this new note is fine, quit the loop
                    break;
                }
            }
            if (null != newStart) {
                // set this on the range
                this.lettersRange.setStart(newStart);
            }
        }
        if (endDelta != 0) {
            // are moving the end, find the note index and change it here
            // change this index
            Chord newEnd = null;
            while (endIndex >= 0 && endIndex < singleChords.getSize()) {
                endIndex += endDelta;
                newEnd = singleChords.getChord(endIndex);
                if (false == newEnd.hasSharp() && false == newEnd.hasFlat()) {
                    // this new note is fine, quit the loop
                    break;
                }
            }
            if (null != newEnd) {
                // set this on the range
                this.lettersRange.setEnd(newEnd);
            }
        }

        // set this on the application as required
        settings.setPianoLettersRange(this.lettersRange).commitChanges();
        // show the data on the page here
        setLettersData();
    }

    private void setControlsFromData() {
        Settings settings = this.application.getSettings();
        // set which input type we have
        this.switchPiano.setChecked(settings.getIsKeyInputPiano());
        this.switchLettersPiano.setChecked(settings.getIsShowPianoLetters());
        this.switchLetters.setChecked(!settings.getIsKeyInputPiano());

        // set the letters on the key range accordingly
        this.lettersRange = settings.getPianoLettersRange();
        setLettersData();

        // show / hide the extra controls
        if (this.switchPiano.isChecked()) {
            this.pianoExtraLayout.setVisibility(View.VISIBLE);
            this.lettersExtraLayout.setVisibility(View.GONE);
            // setup the piano range here to be the default as a demo
            this.piano.setNoteRange(this.piano.getDefaultNoteRange());
        }
        else {
            this.pianoExtraLayout.setVisibility(View.GONE);
            this.lettersExtraLayout.setVisibility(View.VISIBLE);
        }
        // and invalidate the piano accordingly
        this.piano.invalidate();
    }

    private void setLettersData() {
        // set the data to the letters controls
        String startLetter = Character.toString(lettersRange.getStart().root().getNotePrimitive());
        String endLetter = Character.toString(lettersRange.getEnd().root().getNotePrimitive());
        this.startLetter.setText(startLetter);
        this.endLetter.setText(endLetter);
        // setup the piano keys range to that specified in the boxes
        this.piano.setNoteRange(this.lettersRange);
        // and invalidate the piano accordingly
        this.piano.invalidate();
    }

    private void setInputToKeys() {
        this.application.getInputSelector().changeInputType(Settings.InputType.keys);
        Input activeInput = this.application.getInputSelector().getActiveInput();
        if (false == activeInput instanceof InputKeys) {
            // there is no input
            Log.error("Active INPUT is not KEYS despite us setting it to be, it is " + activeInput);
        }
        else {
            this.input = (InputKeys) activeInput;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // change to be Key for sure
        setInputToKeys();
        // and listen to this piano
        this.piano.addListener(this);
        // we will need sounds
        SoundPlayer.initialise(this, this.application);
    }

    @Override
    protected void onPause() {
        // remove us as a listener
        this.piano.removeListener(this);
        // shut down the sounds
        SoundPlayer.close();
        // and close us
        super.onPause();
    }

    @Override
    public void noteReleased(Chord chord) {
        // fine
    }

    @Override
    public void noteDepressed(Chord chord) {
        // play the note
        SoundPlayer.getINSTANCE().playSound(chord);
    }
}

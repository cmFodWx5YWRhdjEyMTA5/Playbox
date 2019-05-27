package uk.co.darkerwaters.staveinvaders.actvities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.input.Input;
import uk.co.darkerwaters.staveinvaders.input.InputKeys;
import uk.co.darkerwaters.staveinvaders.input.InputUsb;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.sounds.SoundPlayer;
import uk.co.darkerwaters.staveinvaders.views.KeysView;
import uk.co.darkerwaters.staveinvaders.views.PianoPlaying;
import uk.co.darkerwaters.staveinvaders.views.PianoTouchable;

public class KeyboardSetupActivity extends AppCompatActivity implements KeysView.IKeysViewListener {

    private Application application;
    private InputKeys input;

    private PianoTouchable piano;
    private Switch switchPiano;
    private Switch switchLettersPiano;
    private Switch switchLetters;
    private View pianoExtraLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_setup);

        // get the application for reference
        this.application = (Application) this.getApplication();

        this.piano = findViewById(R.id.microphone_setup_piano);
        this.pianoExtraLayout = findViewById(R.id.pianoExtraLayout);
        this.switchLetters = findViewById(R.id.lettersChoiceSwitch);
        this.switchPiano = findViewById(R.id.pianoChoiceSwitch);
        this.switchLettersPiano = findViewById(R.id.lettersOnPianoSwitch);
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
    }

    private void setControlsFromData() {
        Settings settings = this.application.getSettings();
        this.switchPiano.setChecked(settings.getIsKeyInputPiano());
        this.switchLettersPiano.setChecked(settings.getIsShowPianoLetters());
        this.switchLetters.setChecked(!settings.getIsKeyInputPiano());
        // show / hide the extra controls
        if (this.switchPiano.isChecked()) {
            this.pianoExtraLayout.setVisibility(View.VISIBLE);
        }
        else {
            this.pianoExtraLayout.setVisibility(View.GONE);
        }
        // setup the piano here
        this.piano.setNoteRange(this.piano.getDefaultNoteRange());
        // and invalidate the piano accordingly
        this.piano.invalidate();
    }

    private void setInputToKeys() {
        this.application.getInputSelector().changeInputType(Settings.InputType.keys);
        Input activeInput = this.application.getInputSelector().getActiveInput();
        if (activeInput == null || false == activeInput instanceof InputUsb) {
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

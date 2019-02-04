package uk.co.darkerwaters.noteinvaders;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.midi.MidiDeviceInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.games.GamePlayer;
import uk.co.darkerwaters.noteinvaders.sounds.SoundPlayer;
import uk.co.darkerwaters.noteinvaders.state.ActiveScore;
import uk.co.darkerwaters.noteinvaders.state.Chord;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.NoteRange;
import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.Playable;
import uk.co.darkerwaters.noteinvaders.state.input.InputConnectionInterface;
import uk.co.darkerwaters.noteinvaders.state.input.InputMicrophone;
import uk.co.darkerwaters.noteinvaders.state.input.InputMidi;
import uk.co.darkerwaters.noteinvaders.views.MusicView;
import uk.co.darkerwaters.noteinvaders.views.MusicViewNoteProviderTempo;
import uk.co.darkerwaters.noteinvaders.views.PianoView;
import uk.co.darkerwaters.noteinvaders.views.ScoreActiveView;

public class PlayActivity extends HidingFullscreenActivity implements
        MusicView.MusicViewListener,
        PianoView.IPianoViewListener,
        MicrophonePermissionHandler.MicrophonePermissionListener,
        InputMidi.MidiListener,
        InputConnectionInterface {

    private Thread noteThread = null;
    private volatile boolean isRunNotes = true;
    private final Object waitObject = new Object();

    private MusicView musicView;
    private PianoView pianoView;
    private ScoreActiveView scoreView;
    private Spinner tempoSpinner;
    private TextView tempoChangeTime;
    private FloatingActionButton floatingPauseButton;
    private FloatingActionButton floatingStopButton;
    private FloatingActionButton floatingTempoButton;

    private View tempoIncreaseIcon;
    private View tempoDecreaseIcon;
    private View gameOverDisplay;

    private View mControlsView;
    private TextView microphonePermissionText;
    private Button microphonePermissionButton;

    private Button soundButton;

    private Button[] levelButtons = new Button[3];
    private View settingsLayout;
    private View levelButtonsLayout;

    private Switch showNoteNamesSwitch;

    private Game level;
    private GamePlayer levelPlayer;

    private MusicViewNoteProviderTempo noteProvider;
    private MicrophonePermissionHandler micPermissionsHandler;
    private PlayFabsHandler fabsHandler = null;
    private InputMicrophone inputMicrophone = null;
    private InputMidi inputMidi = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this is a new game
        NoteInvaders.getAppContext().getCurrentActiveScore().reset();

        this.noteProvider = new MusicViewNoteProviderTempo();

        this.scoreView = (ScoreActiveView) findViewById(R.id.score_view);
        this.musicView = (MusicView) findViewById(R.id.music_view);
        this.musicView.setViewProvider(this.noteProvider);
        this.pianoView = (PianoView) findViewById(R.id.pianoView);
        this.tempoSpinner = (Spinner) findViewById(R.id.tempo_spinner);
        this.floatingPauseButton = (FloatingActionButton) findViewById(R.id.floatingPauseButton);
        this.floatingStopButton = (FloatingActionButton) findViewById(R.id.floatingStopButton);
        this.floatingTempoButton = (FloatingActionButton) findViewById(R.id.floatingTempoButton);
        this.tempoIncreaseIcon = findViewById(R.id.tempo_increase_image);
        this.tempoDecreaseIcon = findViewById(R.id.tempo_decrease_image);
        this.gameOverDisplay = findViewById(R.id.game_over_text);
        this.tempoChangeTime = (TextView) findViewById(R.id.tempo_change_time);

        this.mControlsView = findViewById(R.id.fullscreen_content_controls);
        this.microphonePermissionText = (TextView) findViewById(R.id.text_microphone_permission);
        this.microphonePermissionButton = (Button) findViewById(R.id.button_mic_permission);
        this.showNoteNamesSwitch = (Switch) findViewById(R.id.help_switch);
        this.soundButton = (Button) findViewById(R.id.button_sound);
        // level buttons
        this.levelButtons[0] = (Button) findViewById(R.id.button_easy);
        this.levelButtons[1] = (Button) findViewById(R.id.button_medium);
        this.levelButtons[2] = (Button) findViewById(R.id.button_hard);
        this.settingsLayout = findViewById(R.id.settings_layout);
        this.levelButtonsLayout = findViewById(R.id.level_buttons_layout);

        // setup the seek bar controls
        setupTempoSeekBar();
        // setup the sounds
        SoundPlayer.initialise(this);

        // any controls that make android show the title/back we need to add the delay listener
        tempoSpinner.setOnTouchListener(mDelayHideTouchListener);

        // listen for input from the piano
        this.pianoView.addListener(this);

        // get the notes we want to play from on this level
        this.level = NoteInvaders.getAppContext().getGameSelected();
        this.levelPlayer = this.level.getGamePlayer();

        // listen for pause button clicks
        this.floatingPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start playing
                if (NoteInvaders.getAppContext().getCurrentActiveScore().isGameOver()) {
                    // reset the old game
                    resetGame();
                    // update all our controls
                    updateControlsFromState();
                }
                // set the score settings and resume
                toggle();
            }
        });
        // and stop and tempo button clicks
        this.floatingStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        this.floatingTempoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseTempo(false);
                // tempo changed, clear the notes
                noteProvider.clearNotes();
            }
        });
        // hide the buttons
        this.tempoIncreaseIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dissapear));
        this.tempoDecreaseIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dissapear));
        this.gameOverDisplay.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dissapear));

        // create the fabs for this view
        this.fabsHandler = new PlayFabsHandler(this, new NoteInvaders.InputChangeListener() {
            @Override
            public void onInputTypeChanged(NoteInvaders.InputType type) {
                PlayActivity.this.onInputTypeChanged(type);
            }
        });
        // listen for permission requests to turn on the microphone
        this.microphonePermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
            }
        });
        // listen for the user clicking the sound button
        this.soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // toggle the sound
                NoteInvaders.getAppContext().setIsSoundOn(!NoteInvaders.getAppContext().isSoundOn());
                setSoundIcon();
            }
        });
        //setup our music view
        setupMusicView();

        // setup the display of this active score
        this.scoreView.setScore(NoteInvaders.getAppContext().getCurrentActiveScore());
        this.scoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showScoreCard();
            }
        });

        // setup the help switch
        this.showNoteNamesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PlayActivity.this.pianoView.setIsDrawNoteName(b);
                PlayActivity.this.musicView.setIsDrawNoteName(b);
            }
        });

        // setup MIDI
        this.inputMidi = new InputMidi(this);

        // setup the level buttons
        setupLevelButtons();

        // update from our game state
        updateControlsFromState();
    }

    private void updateControlsFromState() {
        // get the score and set our controls accordingly
        ActiveScore score = NoteInvaders.getAppContext().getCurrentActiveScore();
        // set the help switch in the proper state
        showNoteNamesSwitch.setChecked(score.isHelpOn());
        setBeats(score.getTopBpm());
        // set the sound icon from the state
        setSoundIcon();
        // show / hide the controls properly
        showHideControls();
    }

    private void setBeats(int beats) {
        if (beats == 0) {
            // set the default nicer than this
            beats = 60;
        }
        // the tempo too
        for (int i = 0; i < ActiveScore.K_AVAILABLE_TEMPOS.length; ++i) {
            if (ActiveScore.K_AVAILABLE_TEMPOS[i] == beats) {
                // set the selection on the spinner, this will update our views also
                this.tempoSpinner.setSelection(i);
                break;
            }
        }
    }

    private void setSoundIcon() {
        int iconId;
        if (NoteInvaders.getAppContext().isSoundOn()) {
            iconId = R.drawable.ic_baseline_volume_up_24px;
        }
        else {
            iconId = R.drawable.ic_baseline_volume_off_24px;
        }
        this.soundButton.setBackgroundResource(iconId);
    }

    private void setupLevelButtons() {
        this.levelButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayActivity.this.showNoteNamesSwitch.setChecked(true);
                PlayActivity.this.tempoSpinner.setSelection(1);
            }
        });

        this.levelButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayActivity.this.showNoteNamesSwitch.setChecked(false);
                PlayActivity.this.tempoSpinner.setSelection(3);
            }
        });

        this.levelButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayActivity.this.showNoteNamesSwitch.setChecked(false);
                PlayActivity.this.tempoSpinner.setSelection(5);
            }
        });
    }

    private void setupMusicView() {
        // setup the view for this level
        this.musicView.setIsDrawLaser(true);
        this.musicView.showTreble(this.level.isTreble());
        this.musicView.showBass(this.level.isBass());
        this.musicView.setIsDrawNoteName(this.showNoteNamesSwitch.isChecked());

        if (false == this.level.isTreble() || false == this.level.isBass()) {
            // we are only drawing one set of lines, shrink the height of the view
            this.musicView.post(new Runnable() {
                @Override
                public void run() {
                    ViewGroup.LayoutParams params = musicView.getLayoutParams();
                    params.height = (int) (musicView.getWidth() * 0.5f);
                    musicView.setLayoutParams(params);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (null != this.micPermissionsHandler) {
            micPermissionsHandler.handlePermissionsRequest(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void midiDeviceConnectivityChanged(MidiDeviceInfo deviceInfo, boolean isConnected) {
        // the USB connection state changed, update the colours of our buttons
        updateFabsStatus();
    }

    @Override
    public void midiDeviceConnectionChanged(final String deviceId, boolean isConnected) {
        // the MIDI connection state changed, update the status of the buttons to update the colours
        updateFabsStatus();
    }

    @Override
    public void midiBtScanStatusChange(boolean isScanning) {
        // BT scanning status changed
        updateFabsStatus();
    }

    @Override
    public void midiBtDeviceDiscovered(final BluetoothDevice device) {
        // discovered a BT device
        if (NoteInvaders.getAppContext().getSelectedInput() == NoteInvaders.InputType.bt) {
            // if we are not connected then connect to this
            String activeBtConnection = this.inputMidi.getActiveBtConnection();
            if (null == activeBtConnection || activeBtConnection.isEmpty()) {
                // there is no BT connection, connect to it real quick
                this.inputMidi.connectToDevice(device);
            }
            else {
                // so there is an active connection, is this a new discovery of the same device?
                // if it is then connect to this instead as newer is better
                String deviceId = InputMidi.GetMidiDeviceId(device);
                if (this.inputMidi.isConnectionActive() && activeBtConnection.equals(deviceId)) {
                    // this is a new instance of the existing connection
                    this.inputMidi.stopConnection();
                    // connect to the new instance of this device
                    this.inputMidi.connectToDevice(device);
                }
            }
        }
        // update the status of our input buttons
        updateFabsStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.pianoView.removeListener(this);
        this.fabsHandler.close();
        this.inputMidi.stopConnection();
        SoundPlayer.close();
        if (null != this.micPermissionsHandler) {
            this.micPermissionsHandler.close();
            this.micPermissionsHandler = null;
        }
    }

    @Override
    protected void toggle() {
        super.toggle();
        if (false == noteProvider.isPaused() || NoteInvaders.getAppContext().getCurrentActiveScore().isGameOver()) {
            // we are running or the game is over, pause the provider
            noteProvider.setPaused(true);
            updateControlsFromState();
        }
        else {
            // we want to start the game, ensure everything is set on the state before we start
            ActiveScore score = NoteInvaders.getAppContext().getCurrentActiveScore();
            boolean isHelp = this.showNoteNamesSwitch.isChecked();
            int tempo = ActiveScore.K_AVAILABLE_TEMPOS[this.tempoSpinner.getSelectedItemPosition()];

            if (score.isInProgress() &&
                    (isHelp != score.isHelpOn() || tempo != score.getTopBpm())) {
                // change in difficulty - need to reset the score
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_reset_title)
                        .setMessage(R.string.dialog_reset_contents)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.dialog_reset_start_new, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // reset the game
                                resetGame();
                                // set the score settings and resume
                                updateScoreFromControls();
                                // all the data is now accepted the new stuff, continue this new game
                                resumePlaying();
                            }})
                        .setNegativeButton(R.string.dialog_reset_continue_old, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // just resume playing, will reset to what we were doing
                                resumePlaying();
                            }}).show();
            }
            else {
                // no change, update the score (will not change but will update the views)
                updateScoreFromControls();
                // and resume playing
                resumePlaying();
            }
        }
    }

    private void resetGame() {
        // reset everything as this is a new game
        NoteInvaders.getAppContext().getCurrentActiveScore().reset();
        // clear the notes on the view
        this.noteProvider.clearNotes();
    }

    private void updateScoreFromControls() {
        ActiveScore score = NoteInvaders.getAppContext().getCurrentActiveScore();
        boolean isHelp = this.showNoteNamesSwitch.isChecked();
        int tempo = ActiveScore.K_AVAILABLE_TEMPOS[this.tempoSpinner.getSelectedItemPosition()];
        // set this data on the score
        score.setIsHelpOn(isHelp);
        if (score.setBpm(tempo)) {
            // this changes our score so update the views
            this.scoreView.invalidate();
            this.musicView.invalidate();
        }
    }

    private void resumePlaying() {
        // resume playing the game, restart the provider and update the controls
        if (false == NoteInvaders.getAppContext().getCurrentActiveScore().isGameOver()) {
            // game isn't over - start it up again
            gameOverDisplay.startAnimation(AnimationUtils.loadAnimation(PlayActivity.this, R.anim.dissapear));
            // and start running again
            noteProvider.setPaused(false);
            // hide the back and home buttons
            hide();
        }
        // this is us starting a game again, record this on the state
        NoteInvaders.getAppContext().startGame();
        // update the controls to show this
        updateControlsFromState();
        // this resets the time the danger zone was clean
        this.musicView.resetNoteFreeDangerZoneTime();
    }

    @Override
    public void pianoViewSizeChanged(int w, int h, int oldw, int oldh) {
        // if the config has changed, then so maybe has the size of the piano view
        if (null != this.pianoView) {
            setupPianoView();
        }
    }

    private void onInputTypeChanged(NoteInvaders.InputType type) {
        // change our display to represent the new input type
        switch(type) {
            case keyboard:
                // stop the microphone if it is running
                stopAudioMonitoring();
                // stop any BT scanning that might be running
                this.inputMidi.stopBluetoothScanning(null);
                // and close the MIDI connection
                this.inputMidi.closeOpenMidiConnection();
                break;
            case letters:
                // stop the microphone if it is running
                stopAudioMonitoring();
                // stop any BT scanning that might be running
                this.inputMidi.stopBluetoothScanning(null);
                // and close the MIDI connection
                this.inputMidi.closeOpenMidiConnection();
                break;
            case microphone:
                // stop any BT scanning that might be running
                this.inputMidi.stopBluetoothScanning(null);
                // and close the MIDI connection
                this.inputMidi.closeOpenMidiConnection();
                // if we want to use the microphone then we need to ask permission, this will start when granted
                if (null == this.micPermissionsHandler) {
                    this.micPermissionsHandler = new MicrophonePermissionHandler(this, this);
                    this.micPermissionsHandler.initialiseAudioPermissions(this);
                }
                break;
            case bt:
                // stop the microphone if it is running
                stopAudioMonitoring();
                // requested BT, try to connect to something we had before
                if (false == this.inputMidi.isConnectionActive()) {
                    // there is no connection
                    BluetoothDevice defaultBtDevice = this.inputMidi.getDefaultBtDevice();
                    if (null != defaultBtDevice) {
                        // try to connect to this old one
                        this.inputMidi.connectToDevice(defaultBtDevice);
                    }
                }
                // and start scanning in case we had nothing / want more
                this.inputMidi.scanForBluetoothDevices(this, true);
                break;
            case usb:
                // stop the microphone if it is running
                stopAudioMonitoring();
                // requested USB, scan for devices
                this.inputMidi.getConnectedUsbDevices();
                // if we are not connected to anything already then connect to the default
                String activeUsbConnection = this.inputMidi.getActiveUsbConnection();
                if (null == activeUsbConnection || activeUsbConnection.isEmpty()) {
                    // there is no USB connection, connect to one real quick
                    this.inputMidi.connectToDevice(this.inputMidi.getDefaultUsbDevice());
                }
                break;
        }
        // and update the view accordingly
        setupPianoView();
    }

    private void setupPianoView() {
        switch (NoteInvaders.getAppContext().getSelectedInput()) {
            case keyboard:
                // setup the piano to allow keyboard entry
                setupKeyboardEntry();
                // hide the audio permissions labels
                onAudioPermissionChange(false);
                break;
            case letters:
                // setup the piano to allow keyboard entry
                setupLettersEntry();
                // hide the audio permissions labels
                onAudioPermissionChange(false);
                break;
            case microphone:
                // show the piano view properly
                setFullPianoView();
                break;
            case usb:
                // show the piano view properly
                setFullPianoView();
                // hide the audio permissions labels
                onAudioPermissionChange(false);
                break;
            case bt:
                // show the piano view properly
                setFullPianoView();
                // hide the audio permissions labels
                onAudioPermissionChange(false);
                break;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // set the showing of letters etc
                pianoView.setIsDrawNoteName(showNoteNamesSwitch.isChecked());
                // and update the input type FABs for the new input type selected
                updateFabsStatus();
            }
        });
    }

    private void updateFabsStatus() {
        // update the FAB control colours
        this.fabsHandler.setInputAvailability(NoteInvaders.InputType.keyboard, true);
        this.fabsHandler.setInputAvailability(NoteInvaders.InputType.letters, true);
        this.fabsHandler.setInputAvailability(NoteInvaders.InputType.microphone, true);
        this.fabsHandler.setInputAvailability(NoteInvaders.InputType.usb, this.inputMidi.getNoUsbDevices() > 0);
        this.fabsHandler.setInputAvailability(NoteInvaders.InputType.bt, this.inputMidi.getNoBtDevices() > 0);

        String activeBt = this.inputMidi.getActiveBtConnection();
        if (this.inputMidi.isConnectionActive() && null != activeBt && false == activeBt.isEmpty()) {
            fabsHandler.setBtIcon(PlayFabsHandler.BtIcon.Connected);
        }
        else if (this.inputMidi.isBtScanning()) {
            fabsHandler.setBtIcon(PlayFabsHandler.BtIcon.Searching);
        }
        else {
            fabsHandler.setBtIcon(PlayFabsHandler.BtIcon.Normal);
        }
    }

    private void setupKeyboardEntry() {
        // just show a nice selection of notes
        this.pianoView.setNoteRange(this.level.getNoteRange(), false);
        this.pianoView.setIsPlayable(true);
        // and make it larger so they can press those keys
        setPianoViewHeight();
    }

    private void setupLettersEntry() {
        // just show a nice selection of notes, just C4 to B4 to limit to one scale
        Notes notes = NoteInvaders.getNotes();
        NoteRange basicRange = new NoteRange(notes.getNote("C4"), notes.getNote("B4"));
        // setup the piano view to show just a limited range
        this.pianoView.setNoteRange(basicRange, true);
        this.pianoView.setIsPlayable(true);
        // and make it larger so they can press those keys
        setPianoViewHeight();
    }

    private void setFullPianoView() {
        this.pianoView.setNoteRange(this.level.getNoteRange(), false);
        this.pianoView.setIsPlayable(false);
        // and make it larger so they can press those keys
        setPianoViewHeight();
    }

    private void setPianoViewHeight() {
        // make the piano view height nice and high so they user can press the keys
        this.pianoView.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = pianoView.getLayoutParams();
                int pianoHeight = (int) (pianoView.getWidth() * 0.4f);

                // where is this view on the screen
                int[] pianoPosition = new int[2];
                Point size = new Point();
                pianoView.getLocationOnScreen(pianoPosition);
                getWindowManager().getDefaultDisplay().getSize(size);
                // using the size of the screen, how much is left available, limit to this
                params.height = Math.min(pianoHeight, size.y - pianoPosition[1]);
                // and set this height
                pianoView.setLayoutParams(params);
                pianoView.invalidate();


            }
        });
    }

    private void showHideControls() {
        if (noteProvider.isPaused()) {
            // show the play icon
            floatingStopButton.animate().translationX(-getResources().getDimension(R.dimen.standard_63));
            floatingPauseButton.setImageResource(android.R.drawable.ic_media_play);
            // show the settings
            levelButtonsLayout.setVisibility(View.VISIBLE);
            settingsLayout.setVisibility(View.VISIBLE);
            // hide the countdown
            tempoChangeTime.setVisibility(View.INVISIBLE);
        }
        else {
            // show the pause icon
            floatingStopButton.animate().translationX(0);
            floatingPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            // hide the settings
            levelButtonsLayout.setVisibility(View.INVISIBLE);
            settingsLayout.setVisibility(View.INVISIBLE);
            // show the countdown timer
            tempoChangeTime.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void hide() {
        super.hide();

        mControlsView.setVisibility(View.GONE);
        mContentView.invalidate();
    }

    @Override
    protected void showAction() {
        super.showAction();

        mControlsView.setVisibility(View.VISIBLE);
        mContentView.invalidate();
    }

    private void setupTempoSeekBar() {
        List<String> spinnerOptions = new ArrayList<String>();
        String bpmString = getResources().getString(R.string.bpm);
        for (int value : ActiveScore.K_AVAILABLE_TEMPOS) {
            spinnerOptions.add(Integer.toString(value) + " " + bpmString);
        }
        // create the default adapter for these strings
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.tempoSpinner.setAdapter(adapter);
        this.tempoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                final int beats = ActiveScore.K_AVAILABLE_TEMPOS[position];
                // set this on the note provider to provide at the correct tempo
                PlayActivity.this.noteProvider.setBeats(beats);
                PlayActivity.this.musicView.setShowTempo(beats);
                PlayActivity.this.musicView.invalidate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void gameOver() {
        // show the game - over display
        Animation animation = AnimationUtils.loadAnimation(PlayActivity.this, R.anim.fade_in);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // fine, make sure we pause though
                toggle();
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                //more interesting, ended so show the score card now
                showScoreCard();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        gameOverDisplay.startAnimation(animation);
        if (NoteInvaders.getAppContext().isSoundOn()) {
            SoundPlayer.getINSTANCE().gameOver();
        }
    }

    private void increaseTempo(boolean isAllowWin) {
        int currentSelection = this.tempoSpinner.getSelectedItemPosition();
        if (++currentSelection <= ActiveScore.K_AVAILABLE_TEMPOS.length - 1) {
            int requiredBpm = ActiveScore.K_AVAILABLE_TEMPOS[currentSelection];
            // set this on the active score to immediately accept this new pace of notes
            if (NoteInvaders.getAppContext().getCurrentActiveScore().setBpm(requiredBpm)) {
                // animate this information
                this.tempoIncreaseIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade));
                // and update our controls to reflect this
                updateControlsFromState();
                // this changes the speed, reset the time the danger zone is clean of notes
                this.musicView.resetNoteFreeDangerZoneTime();
            }
        }
        else if (isAllowWin) {
            // we just completed the last of the tempo notes, this is won!
            NoteInvaders.getAppContext().getCurrentActiveScore().gameWon();
        }
    }

    private void decreaseTempo() {
        int currentSelection = this.tempoSpinner.getSelectedItemPosition();
        if (--currentSelection >= 0) {
            int requiredBpm = ActiveScore.K_AVAILABLE_TEMPOS[currentSelection];
            // set this on the active score to immediately accept this new pace of notes
            if (NoteInvaders.getAppContext().getCurrentActiveScore().setBpm(requiredBpm)) {
                // animate this information
                this.tempoDecreaseIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade));
                // and update our controls to reflect this
                updateControlsFromState();
                // this changes the speed, reset the time the danger zone is clean of notes
                this.musicView.resetNoteFreeDangerZoneTime();
                // clear the notes, too fast for the beginner
                this.noteProvider.clearNotes();
            }
        }
    }

    @Override
    public void onAudioPermissionChange(boolean isPermissionGranted) {
        if (isPermissionGranted || NoteInvaders.getAppContext().getSelectedInput() != NoteInvaders.InputType.microphone) {
            // hide the allow buttons
            this.microphonePermissionText.setVisibility(View.GONE);
            this.microphonePermissionButton.setVisibility(View.GONE);
            if (isPermissionGranted) {
                // start up the audio detection system
                startAudioMonitoring();
            }
        }
        else {
            // don't monitor for any audio
            stopAudioMonitoring();
            if (NoteInvaders.getAppContext().getSelectedInput() == NoteInvaders.InputType.microphone) {
                // we don't have permission but we want it
                this.microphonePermissionText.setVisibility(View.VISIBLE);
                this.microphonePermissionButton.setVisibility(View.VISIBLE);

            }
        }
    }

    @Override
    protected void onPause() {
        stopAudioMonitoring();
        this.musicView.removeListener(this);
        this.pianoView.removeListener(this);
        this.inputMidi.removeListener(this);
        this.inputMidi.removeMidiListener(this);
        this.inputMidi.stopConnection();
        this.noteProvider.setPaused(true);
        this.isRunNotes = false;
        synchronized (this.waitObject) {
            this.waitObject.notifyAll();
        }
        // close the music view too
        this.musicView.closeView();
        this.pianoView.closeView();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // get the game we are supposed to be playing
        this.level = NoteInvaders.getAppContext().getGameSelected();

        if (NoteInvaders.getAppContext().getCurrentActiveScore().isGameOver()) {
            // the game is over, coming back from the scorecard, close this too...
            finish();
        }
        else {
            // pause the player
            this.noteProvider.setPaused(true);
            // make sure the game over label is hidden
            gameOverDisplay.startAnimation(AnimationUtils.loadAnimation(PlayActivity.this, R.anim.dissapear));
            // reset all of our data
            this.noteProvider.clearNotes();
            this.isRunNotes = true;

            if (null != this.micPermissionsHandler) {
                this.micPermissionsHandler.initialiseAudioPermissions(this);
            }
            // setup the thread to move the notes
            this.noteThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunNotes) {
                        // while we want to run, process the notes
                        moveNotes();
                        synchronized (PlayActivity.this.waitObject) {
                            try {
                                PlayActivity.this.waitObject.wait(10);
                            } catch (InterruptedException e) {
                                // fine
                            }
                        }
                    }
                }
            });
            // listen for changes on the view
            this.musicView.addListener(this);
            this.pianoView.addListener(this);
            this.inputMidi.addListener(this);
            this.inputMidi.addMidiListener(this);
            // and start them again
            this.musicView.start(this);
            this.pianoView.start(this);
            this.inputMidi.startConnection();

            // update all the data on this view
            showHideControls();
            updateControlsFromState();

            // setup the initial data for that last played on this game
            NoteInvaders application = NoteInvaders.getAppContext();
            ActiveScore score = application.getCurrentActiveScore();
            // set the data on the controls
            Boolean helpState = application.getGameHelpState(application.getGameSelected());
            int topTempo = application.getGameTopTempo(application.getGameSelected());
            setBeats(topTempo);
            this.showNoteNamesSwitch.setChecked(helpState == null ? topTempo <= ActiveScore.K_TEMPO_TO_TURN_HELP_ON : helpState);

            // and start scrolling notes
            this.noteThread.start();

            // initialise MIDI
            this.inputMidi.initialiseMidi(this);

            // and set to the correct state to start the input etc
            onInputTypeChanged(application.getSelectedInput());
        }
    }

    private void stopAudioMonitoring() {
        if (null != this.micPermissionsHandler) {
            this.micPermissionsHandler.close();
            this.micPermissionsHandler = null;
        }
        if (null != this.inputMicrophone) {
            // and detecting the notes
            this.inputMicrophone.removeListener(this);
            this.inputMicrophone.stopConnection();
            this.inputMicrophone = null;
        }
    }

    private void startAudioMonitoring() {
        // also start detecting the notes
        inputMicrophone = new InputMicrophone(this);
        // add a listener
        inputMicrophone.addListener(this);
        if (false == inputMicrophone.startConnection()) {
            //TODO failed to start the note detector, do something about this

        }
    }

    @Override
    public void onNoteDetected(Playable note, boolean isDetection, float probability, int frequency) {
        if (isDetection) {
            // a note was detected, pretend this happened on the piano
            pianoView.depressNote(note);
        }
    }

    private void moveNotes() {
        this.musicView.updateViewProvider();
        // and invalidate the view
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // invalidate the changed view
                PlayActivity.this.musicView.invalidate();
                if (false == PlayActivity.this.noteProvider.isPaused()) {
                    if (NoteInvaders.getAppContext().getCurrentActiveScore().isGameOver()) {
                        // this game is over!
                        gameOver();
                    }
                    else if (PlayActivity.this.musicView.getNoteFreeDangerZoneTime() >= ActiveScore.K_SECBEFORESPEEDINCREASE) {
                        // this is good, increase our tempo as they are doing so well
                        NoteInvaders.getAppContext().getCurrentActiveScore().recordBpmCompleted(PlayActivity.this);
                        // and up the tempo
                        increaseTempo(true);
                    }
                    else if (PlayActivity.this.musicView.getNotesInDangerZone() >= ActiveScore.K_NONOTESINDANGERZONEISDEATH) {
                        // are close to death on the starting tempo, try to slow it down, score might just refuse this of course
                        decreaseTempo();
                    }
                    // countdown to the change in speed as the time elapses
                    float secsLeft = ActiveScore.K_SECBEFORESPEEDINCREASE - PlayActivity.this.musicView.getNoteFreeDangerZoneTime();
                    tempoChangeTime.setText(Integer.toString((int)(secsLeft + 0.5f)) + getResources().getString(R.string.second_postfix));
                }
            }
        });
        if (false == this.noteProvider.isPaused()) {
            this.levelPlayer.addNewNotes(this.musicView, this.level);
        }
    }

    @Override
    public void onNotePopped(Playable note) {
        // this is a miss
        NoteInvaders.getAppContext().getCurrentActiveScore().incMisses(note);
        showScore();
        if (NoteInvaders.getAppContext().isSoundOn()) {
            SoundPlayer.getINSTANCE().missed();
        }
    }

    @Override
    public void onNoteDestroyed(final Playable note) {
        // this is a hit
        NoteInvaders.getAppContext().getCurrentActiveScore().incHits(note);
        showScore();
        if (NoteInvaders.getAppContext().isSoundOn()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SoundPlayer.getINSTANCE().playSound(note);
                }
            });
        }
    }

    @Override
    public void onNoteMisfire(Playable note) {
        // this is a false shot
        NoteInvaders.getAppContext().getCurrentActiveScore().incFalseShots(note);
        showScore();
        if (NoteInvaders.getAppContext().isSoundOn()) {
            SoundPlayer.getINSTANCE().falseFire();
        }
    }

    private void showScoreCard() {
        if (false == this.noteProvider.isPaused()) {
            // not paused, quickly pause the playing
            toggle();
        }
        // show the activity that will display the score
        Intent newIntent = new Intent(this, ScoreCardActivity.class);
        this.startActivity(newIntent);
    }

    private void showScore() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update the current score
                PlayActivity.this.scoreView.invalidate();
            }
        });
    }

    @Override
    public void noteReleased(Playable note) {
        // inform the music view that a note was released
        if (false == this.noteProvider.isPaused()) {
            // inform the view that a note was released, if not part of a hit, will be a miss
            this.musicView.noteReleased(note);
        }
        // when the note is released from the piano it needs to be invalidated
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PlayActivity.this.pianoView.invalidate();
            }
        });
    }

    @Override
    public void noteDepressed(Playable note) {
        if (false == this.noteProvider.isPaused()) {
            // inform the music view of this to fire the laser at everything that is currently pressed down
            this.musicView.noteDepressed(note);
        }
        // update the piano view to show this
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // invalidate the view to display it okay
                pianoView.invalidate();
            }
        });
    }
}
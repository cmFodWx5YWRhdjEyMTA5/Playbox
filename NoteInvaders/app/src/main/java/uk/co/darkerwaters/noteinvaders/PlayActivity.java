package uk.co.darkerwaters.noteinvaders;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
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
import java.util.ResourceBundle;

import uk.co.darkerwaters.noteinvaders.games.GamePlayer;
import uk.co.darkerwaters.noteinvaders.sounds.NoteSounds;
import uk.co.darkerwaters.noteinvaders.sounds.SoundPlayer;
import uk.co.darkerwaters.noteinvaders.state.ActiveScore;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.State;
import uk.co.darkerwaters.noteinvaders.state.input.InputConnectionInterface;
import uk.co.darkerwaters.noteinvaders.state.input.InputMicrophone;
import uk.co.darkerwaters.noteinvaders.views.MusicView;
import uk.co.darkerwaters.noteinvaders.views.MusicViewNoteProviderTempo;
import uk.co.darkerwaters.noteinvaders.views.PianoView;
import uk.co.darkerwaters.noteinvaders.views.ScoreActiveView;

public class PlayActivity extends HidingFullscreenActivity implements MusicView.MusicViewListener, PianoView.IPianoViewListener, MicrophonePermissionHandler.MicrophonePermissionListener {

    private Thread noteThread = null;
    private volatile boolean isRunNotes = true;
    private final Object waitObject = new Object();

    private MusicView musicView;
    private PianoView pianoView;
    private ScoreActiveView scoreView;
    private Spinner tempoSpinner;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this is a new game
        State.getInstance().getCurrentActiveScore().reset();

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

        this.pianoView.addListener(this);

        // get the notes we want to play from on this level
        this.level = State.getInstance().getGameSelectedLast();
        this.levelPlayer = this.level.getGamePlayer();

        this.floatingPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start playing
                if (State.getInstance().getCurrentActiveScore().isGameOver()) {
                    // reset the old game
                    resetGame();
                    // update all our controls
                    updateControlsFromState();
                }
                // set the score settings and resume
                toggle();
            }
        });
        this.floatingStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        this.floatingTempoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (increaseTempo(false)) {
                    // tempo changed, clear the notes
                    noteProvider.clearNotes();
                }
            }
        });
        this.tempoIncreaseIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dissapear));
        this.tempoDecreaseIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dissapear));
        this.gameOverDisplay.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dissapear));

        // create the fabs for this view
        this.fabsHandler = new PlayFabsHandler(this, new State.InputChangeListener() {
            @Override
            public void onInputTypeChanged(State.InputType type) {
                // change our display to represent the new input type
                PlayActivity.this.setupPianoView();
            }
        });
        this.microphonePermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
            }
        });
        this.soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // toggle the sound
                State.getInstance().setIsSoundOn(PlayActivity.this, !State.getInstance().isSoundOn());
                setSoundIcon();
            }
        });

        //setup our music view
        setupMusicView();

        // setup the display of this active score
        this.scoreView.setScore(State.getInstance().getCurrentActiveScore());
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

        // setup the level buttons
        setupLevelButtons();

        // update from our game state
        updateControlsFromState();
    }

    private void updateControlsFromState() {
        // get the score and set our controls accordingly
        ActiveScore score = State.getInstance().getCurrentActiveScore();

        // set the help switch in the proper state
        showNoteNamesSwitch.setChecked(score.isHelpOn());
        int beats = score.getTopBpm();
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
        // set the sound icon from the state
        setSoundIcon();
        // show / hide the controls properly
        showHideControls();
    }

    private void setSoundIcon() {
        Drawable icon;
        int iconId;
        if (State.getInstance().isSoundOn()) {
            iconId = R.drawable.ic_baseline_volume_off_24px;
        }
        else {
            iconId = R.drawable.ic_baseline_volume_up_24px;
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
    protected void onDestroy() {
        super.onDestroy();
        this.pianoView.removeListener(this);
        this.fabsHandler.close();
        SoundPlayer.close();
        if (null != this.micPermissionsHandler) {
            this.micPermissionsHandler.close();
            this.micPermissionsHandler = null;
        }
        // this is killed, remove our selection from the state
        State.getInstance().deselectGame(this.level);
    }

    @Override
    protected void toggle() {
        super.toggle();
        if (false == noteProvider.isPaused() || State.getInstance().getCurrentActiveScore().isGameOver()) {
            // we are running or the game is over, pause the provider
            noteProvider.setPaused(true);
            updateControlsFromState();
        }
        else {
            // we want to start the game, ensure everything is set on the state before we start
            ActiveScore score = State.getInstance().getCurrentActiveScore();
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
        State.getInstance().getCurrentActiveScore().reset();
        // clear the notes on the view
        this.noteProvider.clearNotes();
    }

    private void updateScoreFromControls() {
        ActiveScore score = State.getInstance().getCurrentActiveScore();
        boolean isHelp = this.showNoteNamesSwitch.isChecked();
        int tempo = ActiveScore.K_AVAILABLE_TEMPOS[this.tempoSpinner.getSelectedItemPosition()];
        // set this data on the score
        score.setIsHelpOn(isHelp);
        score.setBpm(tempo);
        // this changes our score so update the views
        this.scoreView.invalidate();
        this.musicView.invalidate();
    }

    private void resumePlaying() {
        // resume playing the game, restart the provider and update the controls
        if (false == State.getInstance().getCurrentActiveScore().isGameOver()) {
            // game isn't over - start it up again
            gameOverDisplay.startAnimation(AnimationUtils.loadAnimation(PlayActivity.this, R.anim.dissapear));
            // and start running again
            noteProvider.setPaused(false);
            // hide the back and home buttons
            hide();
        }
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

    private void setupPianoView() {
        switch (State.getInstance().getSelectedInput()) {
            case keyboard:
                if (null != this.micPermissionsHandler) {
                    this.micPermissionsHandler.close();
                    this.micPermissionsHandler = null;
                }
                setupKeyboardEntry();
                // hide the audio permissions labels
                onAudioPermissionChange(false);
                break;
            case microphone:
                // if we want to use the microphone then we need to ask permission
                if (null == this.micPermissionsHandler) {
                    this.micPermissionsHandler = new MicrophonePermissionHandler(this, this);
                    this.micPermissionsHandler.initialiseAudioPermissions(this);
                }
                setFullPianoView();
                break;
            case usb:
            case bt:
                if (null != this.micPermissionsHandler) {
                    this.micPermissionsHandler.close();
                    this.micPermissionsHandler = null;
                }
                setFullPianoView();
                // hide the audio permissions labels
                onAudioPermissionChange(false);
                break;
        }
        // set the showing of letters etc
        this.pianoView.setIsDrawNoteName(this.showNoteNamesSwitch.isChecked());
    }


    private void setupKeyboardEntry() {
        // just show a nice selection of notes
        Notes notes = Notes.instance();
        this.pianoView.setNoteRange(this.level.getNoteRange());
        this.pianoView.setIsPlayable(true);
        // and make it larger so they can press those keys
        this.pianoView.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = pianoView.getLayoutParams();
                params.height = (int) (pianoView.getWidth() * 0.4f);
                pianoView.setLayoutParams(params);
            }
        });
        this.pianoView.invalidate();
    }

    private void setFullPianoView() {
        Notes notes = Notes.instance();
        this.pianoView.setNoteRange(notes.getFullRange());
        this.pianoView.setIsPlayable(false);
        // and make it larger so they can press those keys
        this.pianoView.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = pianoView.getLayoutParams();
                params.height = (int) (pianoView.getWidth() * 0.15f);
                pianoView.setLayoutParams(params);
            }
        });
        this.pianoView.invalidate();
    }

    private void showHideControls() {
        if (noteProvider.isPaused()) {
            // show the play icon
            floatingStopButton.animate().translationX(-getResources().getDimension(R.dimen.standard_63));
            floatingPauseButton.setImageResource(android.R.drawable.ic_media_play);
            // show the settings
            levelButtonsLayout.setVisibility(View.VISIBLE);
            settingsLayout.setVisibility(View.VISIBLE);
        }
        else {
            // show the pause icon
            floatingStopButton.animate().translationX(0);
            floatingPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            // hide the settings
            levelButtonsLayout.setVisibility(View.INVISIBLE);
            settingsLayout.setVisibility(View.INVISIBLE);
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
        if (State.getInstance().isSoundOn()) {
            SoundPlayer.getINSTANCE().gameOver();
        }
    }

    private boolean increaseTempo(boolean isAllowWin) {
        int currentSelection = this.tempoSpinner.getSelectedItemPosition();
        boolean isIncreased = false;
        if (++currentSelection <= ActiveScore.K_AVAILABLE_TEMPOS.length - 1) {
            int newBpm = ActiveScore.K_AVAILABLE_TEMPOS[currentSelection];
            // set this on the active score to immediately accept this new pace of notes
            if (newBpm == State.getInstance().getCurrentActiveScore().setBpm(newBpm)) {
                // the score accepted this change, animate this information
                this.tempoIncreaseIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade));
                // and update our controls to reflect this
                updateControlsFromState();
                // this changes the speed, reset the time the danger zone is clean of notes
                this.musicView.resetNoteFreeDangerZoneTime();
                // this is a change, return this
                isIncreased = true;
            }
        }
        else if (isAllowWin) {
            // we just completed the last of the tempo notes, this is won!
            State.getInstance().getCurrentActiveScore().gameWon();
        }
        return isIncreased;
    }

    private boolean decreaseTempo() {
        int currentSelection = this.tempoSpinner.getSelectedItemPosition();
        boolean isDecreased = false;
        if (--currentSelection >= 0) {
            int newBpm = ActiveScore.K_AVAILABLE_TEMPOS[currentSelection];
            // set this on the active score to immediately accept this new pace of notes
            if (newBpm == State.getInstance().getCurrentActiveScore().setBpm(newBpm)) {
                // the score accepted this change, animate this information
                this.tempoDecreaseIcon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade));
                // and update our controls to reflect this
                updateControlsFromState();
                // this changes the speed, reset the time the danger zone is clean of notes
                this.musicView.resetNoteFreeDangerZoneTime();
                // this is a change, return this
                isDecreased = true;
                // clear the notes, too fast for the beginner
                this.noteProvider.clearNotes();
            }
        }
        return isDecreased;
    }

    @Override
    public void onAudioPermissionChange(boolean isPermissionGranted) {
        if (isPermissionGranted || State.getInstance().getSelectedInput() != State.InputType.microphone) {
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
            if (State.getInstance().getSelectedInput() == State.InputType.microphone) {
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
        this.noteProvider.setPaused(true);
        this.isRunNotes = false;
        synchronized (this.waitObject) {
            this.waitObject.notifyAll();
        }
        // close the music view too
        this.musicView.closeView();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // pause the player
        this.noteProvider.setPaused(true);

        // reset all of our data
        this.musicView.closeView();
        this.noteProvider.clearNotes();
        this.isRunNotes = true;

        if (null != this.micPermissionsHandler) {
            this.micPermissionsHandler.initialiseAudioPermissions(this);
        }

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

        // update all the data on this view
        showHideControls();
        updateControlsFromState();

        // and start scrolling notes
        this.noteThread.start();
    }

    private void stopAudioMonitoring() {
        if (null != this.inputMicrophone) {
            // and detecting the notes
            this.inputMicrophone.stopConnection();
            this.inputMicrophone = null;
        }
    }

    private void startAudioMonitoring() {
        // also start detecting the notes
        inputMicrophone = new InputMicrophone(this);
        inputMicrophone.initialiseConnection();
        // add a listener
        inputMicrophone.addListener(new InputConnectionInterface() {
            @Override
            public void onNoteDetected(final Note note, final float probability, int frequency, boolean isPitched) {
                if (probability > InputMicrophone.K_NOTE_DETECTION_PROBABIILITY_THRESHOLD && frequency > InputMicrophone.K_NOTE_DETECTION_FREQUENCY_THRESHOLD) {
                    // exceeded thresholds for detection, inform the music view we detected this
                    noteDepressed(note);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // invalidate the view to display it okay
                        pianoView.invalidate();
                    }
                });
            }
        });
        if (false == inputMicrophone.startConnection()) {
            //TODO failed to start the note detector, do something about this

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
                    if (State.getInstance().getCurrentActiveScore().isGameOver()) {
                        // this game is over!
                        gameOver();
                    }
                    else if (PlayActivity.this.musicView.getNoteFreeDangerZoneTime() >= ActiveScore.K_SECBEFORESPEEDINCREASE) {
                        // this is good, increase our tempo as they are doing so well
                        State.getInstance().getCurrentActiveScore().recordBpmCompleted(PlayActivity.this);
                        // and up the tempo
                        increaseTempo(true);
                    }
                    else if (PlayActivity.this.musicView.getNotesInDangerZone() >= ActiveScore.K_NONOTESINDANGERZONEISDEATH) {
                        // are close to death on the starting tempo, try to slow it down, score might just refuse this of course
                        decreaseTempo();
                    }
                }
            }
        });
        if (false == this.noteProvider.isPaused()) {
            this.levelPlayer.addNewNotes(this.musicView, this.level);
        }
    }

    @Override
    public void onNotePopped(Note note) {
        // this is a miss
        State.getInstance().getCurrentActiveScore().incMisses(note);
        showScore();
        if (State.getInstance().isSoundOn()) {
            SoundPlayer.getINSTANCE().missed();
        }
    }

    @Override
    public void onNoteDestroyed(final Note note) {
        // this is a hit
        State.getInstance().getCurrentActiveScore().incHits(note);
        showScore();
        if (State.getInstance().isSoundOn()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SoundPlayer.getINSTANCE().playSound(note);
                }
            });
        }
    }

    @Override
    public void onNoteMisfire(Note note) {
        // this is a false shot
        State.getInstance().getCurrentActiveScore().incFalseShots(note);
        showScore();
        if (State.getInstance().isSoundOn()) {
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
    public void noteReleased(Note note) {
        // when the note is released from the piano it needs to be invalidated
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PlayActivity.this.pianoView.invalidate();
            }
        });
    }

    @Override
    public void noteDepressed(Note note) {
        if (false == this.noteProvider.isPaused()) {
            // when the note is pressed, fire the laser
            this.musicView.fireLaser(note);
        }
    }
}
package uk.co.darkerwaters.noteinvaders;

import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.darkerwaters.noteinvaders.games.GamePlayer;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.State;
import uk.co.darkerwaters.noteinvaders.views.MusicView;
import uk.co.darkerwaters.noteinvaders.views.MusicViewNoteProviderTempo;
import uk.co.darkerwaters.noteinvaders.views.PianoView;

public class PlayActivity extends HidingFullscreenActivity implements MusicView.MusicViewListener, PianoView.IPianoViewListener {

    private Thread noteThread = null;
    private volatile boolean isRunNotes = true;
    private final Object waitObject = new Object();

    private MusicView musicView;
    private PianoView pianoView;
    private TextView totalMissedCount;
    private Spinner tempoSpinner;
    private FloatingActionButton floatingPauseButton;
    private FloatingActionButton floatingStopButton;
    private View mControlsView;

    private Game level;
    private GamePlayer levelPlayer;
    private Map<Note, Integer> notesMissed = new HashMap<Note, Integer>();
    private volatile int totalNotesMissed = 0;

    private FloatingActionButton inputFab;
    private FloatingActionButton manualFab;
    private FloatingActionButton micFab;
    private FloatingActionButton usbFab;
    private FloatingActionButton btFab;

    private boolean isFabsShown = false;

    private MusicViewNoteProviderTempo noteProvider;

    private final int[] availableTempos = new int[] {
            20,40,50,60,80,100,120,150,180
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.noteProvider = new MusicViewNoteProviderTempo();

        this.musicView = (MusicView) findViewById(R.id.music_view);
        this.musicView.setViewProvider(this.noteProvider);
        this.pianoView = (PianoView) findViewById(R.id.pianoView);
        this.totalMissedCount = (TextView) findViewById(R.id.total_missed_count);
        this.tempoSpinner = (Spinner) findViewById(R.id.tempo_spinner);
        this.floatingPauseButton = (FloatingActionButton) findViewById(R.id.floatingPauseButton);
        this.floatingStopButton = (FloatingActionButton) findViewById(R.id.floatingStopButton);
        this.mControlsView = findViewById(R.id.fullscreen_content_controls);
        // setup the seek bar controls
        setupTempoSeekBar();

        // any controls that make android show the title/back we need to add the delay listener
        tempoSpinner.setOnTouchListener(mDelayHideTouchListener);

        this.pianoView.addListener(this);

        // get the notes we want to play from on this level
        this.level = State.getInstance().getGameSelectedLast();
        this.levelPlayer = this.level.getGamePlayer();

        // setup the view for this level
        this.musicView.setIsDrawLaser(true);
        this.musicView.showTreble(this.level.isTreble());
        this.musicView.showBass(this.level.isBass());

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

        this.floatingPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
            }
        });

        this.floatingStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        updateControls();

        setupFabs();
    }

    @Override
    public void pianoViewSizeChanged(int w, int h, int oldw, int oldh) {
        // if the config has changed, then so maybe has the size of the piano view
        if (null != this.pianoView) {
            setInputIcon();
        }
    }

    private void setupFabs() {
        this.inputFab = (FloatingActionButton) findViewById(R.id.input_action_button);
        this.manualFab = (FloatingActionButton) findViewById(R.id.input_action_1);
        this.micFab = (FloatingActionButton) findViewById(R.id.input_action_2);
        this.usbFab = (FloatingActionButton) findViewById(R.id.input_action_3);
        this.btFab = (FloatingActionButton) findViewById(R.id.input_action_4);

        // set the correct current icon
        setInputIcon();

        // set the icons on these buttons
        this.manualFab.setImageResource(R.drawable.ic_baseline_keyboard_24px);
        this.micFab.setImageResource(R.drawable.ic_baseline_mic_24px);
        this.usbFab.setImageResource(R.drawable.ic_baseline_usb_24px);
        this.btFab.setImageResource(R.drawable.ic_baseline_bluetooth_audio_24px);

        this.inputFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clicked the input FAB, expand or hide the selection
                toggleInputFabs();
            }
        });

        this.manualFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputType(State.InputType.keyboard);
            }
        });
        this.micFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputType(State.InputType.microphone);
            }
        });
        this.usbFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputType(State.InputType.usb);
            }
        });
        this.btFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeInputType(State.InputType.bt);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.pianoView.removeListener(this);
        // this is killed, remove our selection from the state
        State.getInstance().deselectGame(this.level);
    }

    @Override
    protected void toggle() {
        super.toggle();
        if (false == noteProvider.isPaused()) {
            // we are running, pause the provider
            noteProvider.setPaused(true);
        }
        else {
            // controls are hidden, we are playing again
            noteProvider.setPaused(false);
        }
        updateControls();
    }

    private void changeInputType(State.InputType input) {
        // set the input to manual
        State.getInstance().setSelectedInput(input);
        // and shrink the selection
        toggleInputFabs();
        // and update the icon
        setInputIcon();
    }

    private void setupKeyboardEntry() {
        // just show a nice selection of notes
        Notes notes = Notes.instance();
        this.pianoView.setNoteRange(notes.getNote("C4"), notes.getNote("E5"));
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
        this.pianoView.setNoteRange(notes.getNote(0), notes.getNote(notes.getNoteCount() - 1));
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

    private void setupMicrophoneEntry() {
        // show the keyboard full range to show what the microphone detects
        setFullPianoView();
    }

    private void setupUsbEntry() {
        // show the keyboard
        setFullPianoView();
    }

    private void setupBtEntry() {
        // show the keyboard
        setFullPianoView();
    }

    private void setInputIcon() {
        if (null != this.inputFab) {
            switch (State.getInstance().getSelectedInput()) {
                case keyboard:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_keyboard_24px);
                    setupKeyboardEntry();
                    break;
                case microphone:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_mic_24px);
                    setupMicrophoneEntry();
                    break;
                case usb:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_usb_24px);
                    setupUsbEntry();
                    break;
                case bt:
                    this.inputFab.setImageResource(R.drawable.ic_baseline_bluetooth_audio_24px);
                    setupBtEntry();
                    break;
            }
        }
    }

    private void toggleInputFabs() {
        if (this.isFabsShown) {
            manualFab.animate().translationX(0);
            micFab.animate().translationX(0);
            usbFab.animate().translationX(0);
            btFab.animate().translationX(0);
            this.isFabsShown = false;
        }
        else {
            manualFab.animate().translationX(getResources().getDimension(R.dimen.standard_55));
            micFab.animate().translationX(getResources().getDimension(R.dimen.standard_105));
            usbFab.animate().translationX(getResources().getDimension(R.dimen.standard_155));
            btFab.animate().translationX(getResources().getDimension(R.dimen.standard_205));
            this.isFabsShown = true;
        }
    }

    private void updateControls() {
        if (noteProvider.isPaused()) {
            // show the play icon
            floatingStopButton.animate().translationX(-getResources().getDimension(R.dimen.standard_63));
            floatingPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
        else {
            // show the pause icon
            floatingStopButton.animate().translationX(0);
            floatingPauseButton.setImageResource(android.R.drawable.ic_media_pause);
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
        for (int value : availableTempos) {
            spinnerOptions.add(Integer.toString(value) + " " + bpmString);
        }
        // create the default adapter for these strings
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.tempoSpinner.setAdapter(adapter);
        this.tempoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                final int beats = availableTempos[position];
                PlayActivity.this.noteProvider.setBeats(beats);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // set the correct tempo
        for (int i = 0; i < availableTempos.length; ++i) {
            if (availableTempos[i] == this.noteProvider.getBeats()) {
                this.tempoSpinner.setSelection(i);
                break;
            }
        }
    }

    @Override
    protected void onPause() {
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

        // reset all of our data
        this.musicView.closeView();
        this.noteProvider.clearNotes();
        this.totalNotesMissed = 0;
        this.isRunNotes = true;
        this.notesMissed.clear();

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
        // and start scrolling notes
        this.noteThread.start();
        // pause the player ready for the user to start
        this.noteProvider.setPaused(false);
        updateControls();
    }

    private void moveNotes() {
        this.musicView.updateViewProvider();
        // and invalidate the view
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // invalidate the changed view
                PlayActivity.this.musicView.invalidate();
            }
        });
        this.levelPlayer.addNewNotes(this.musicView, this.level);
    }

    @Override
    public void onNotePopped(Note note) {
        // add to the notes that we missed
        Integer value = this.notesMissed.get(note);
        if (value != null) {
            value = value + 1;
        }
        else {
            value = 1;
        }
        ++totalNotesMissed;
        this.notesMissed.put(note, value);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update the current score
                totalMissedCount.setText(Integer.toString(totalNotesMissed));
            }
        });
    }

    @Override
    public void onNoteDestroyed(Note note) {
        //TODO add to the notes that we hit

    }

    @Override
    public void onNoteMisfire(Note note) {
        //TODO add to the notes that we shot at but were not there

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
        // when the note is pressed, fire the laser
        this.musicView.fireTrebleLaser(note);
        this.musicView.fireBassLaser(note);
    }
}

package uk.co.darkerwaters.noteinvaders;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import uk.co.darkerwaters.noteinvaders.games.GamePlayer;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.State;
import uk.co.darkerwaters.noteinvaders.views.MusicView;
import uk.co.darkerwaters.noteinvaders.views.MusicViewNoteProviderTempo;

public class PlayActivity extends HidingFullscreenActivity implements MusicView.MusicViewListener {

    private Thread noteThread = null;
    private volatile boolean isRunNotes = true;
    private final Object waitObject = new Object();

    private MusicView musicView;
    private TextView totalMissedCount;
    private TextView textTempo;
    private SeekBar seekBarTempo;
    private FloatingActionButton floatingPauseButton;
    private FloatingActionButton floatingStopButton;
    private View mControlsView;

    private Game level;
    private GamePlayer levelPlayer;
    private Map<Note, Integer> notesMissed = new HashMap<Note, Integer>();
    private volatile int totalNotesMissed = 0;

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
        this.totalMissedCount = (TextView) findViewById(R.id.total_missed_count);
        this.textTempo = (TextView) findViewById(R.id.text_tempo);
        this.seekBarTempo = (SeekBar) findViewById(R.id.seek_bar_tempo);
        this.floatingPauseButton = (FloatingActionButton) findViewById(R.id.floatingPauseButton);
        this.floatingStopButton = (FloatingActionButton) findViewById(R.id.floatingStopButton);
        this.mControlsView = findViewById(R.id.fullscreen_content_controls);
        // setup the seek bar controls
        setupTempoSeekBar();

        // get the notes we want to play from on this level
        this.level = State.getInstance().getGameSelectedLast();
        this.levelPlayer = this.level.getGamePlayer();

        // setup the view for this level
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // this is killed, remove our selection from the state
        State.getInstance().deselectGame(this.level);
    }

    @Override
    protected void toggle() {
        super.toggle();
        if (mVisible) {
            // controls are visible, we are paused, pause the music view
            noteProvider.setPaused(true);
        }
        else {
            // controls are hidden, we are playing again
            noteProvider.setPaused(false);
        }
        updateControls();
    }

    private void updateControls() {
        if (noteProvider.isPaused()) {
            // show the play icon
            floatingPauseButton.setImageResource(android.R.drawable.ic_media_play);
            if (!mVisible) {
                // we are hidden, show things
                toggle();
            }
        }
        else {
            // show the pause icon
            floatingPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            if (mVisible) {
                // we are showing, hide things
                toggle();
            }
        }
    }

    @Override
    protected void hide() {
        super.hide();

        mControlsView.setVisibility(View.GONE);
    }

    @Override
    protected void showAction() {
        super.showAction();
        mControlsView.setVisibility(View.VISIBLE);
    }

    private void setupTempoSeekBar() {
        this.seekBarTempo.setMax(this.availableTempos.length - 1);
        this.seekBarTempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    final int beats = availableTempos[progress];
                    PlayActivity.this.noteProvider.setBeats(beats);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textTempo.setText(beats + " " + getResources().getString(R.string.bps));
                        }
                    });
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        for (int i = 0; i < availableTempos.length; ++i) {
            if (availableTempos[i] == this.noteProvider.getBeats()) {
                this.seekBarTempo.setProgress(i);
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
}

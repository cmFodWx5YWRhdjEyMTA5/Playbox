package uk.co.darkerwaters.noteinvaders;

import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.State;

public class PlayActivity extends HidingFullscreenActivity implements MusicView.MusicViewListener {

    private Thread noteThread = null;
    private volatile boolean isRunNotes = true;
    private final Object waitObject = new Object();

    private MusicView musicView;
    private TextView totalMissedCount;

    private Game.GameLevel level;
    private Map<Note, Integer> notesMissed = new HashMap<Note, Integer>();
    private volatile int totalNotesMissed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.musicView = (MusicView) findViewById(R.id.music_view);
        this.totalMissedCount = (TextView) findViewById(R.id.total_missed_count);

        // get the notes we want to play from on this level
        this.level = State.getInstance().getGameLevel();
    }

    @Override
    protected void onPause() {
        this.musicView.removeListener(this);
        this.isRunNotes = false;
        synchronized (this.waitObject) {
            this.waitObject.notifyAll();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    }

    private void moveNotes() {
        this.musicView.shiftNotesLeft(1);
        // and invalidate the view
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // invalidate the changed view
                PlayActivity.this.musicView.invalidate();
            }
        });
        Random random = new Random(System.currentTimeMillis());
        while (this.musicView.getNoteCount() < 20) {
            // add another note
            Note note = this.level.notesApplicable[random.nextInt(this.level.notesApplicable.length)];
            if (null != note) {
                // add to the view
                this.musicView.pushNote(note);
            }
        }
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

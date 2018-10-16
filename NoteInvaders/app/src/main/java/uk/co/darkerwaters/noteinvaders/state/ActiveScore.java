package uk.co.darkerwaters.noteinvaders.state;



import android.app.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActiveScore {

    public static final int K_PERMITTED_MISS_COUNT = 10;
    public static final int K_PERMITTED_FALSE_SHOT_COUNT = 10;
    public static final int K_PERMITTED_ERRORS = K_PERMITTED_MISS_COUNT + K_PERMITTED_FALSE_SHOT_COUNT;

    public static final float K_SECBEFORESPEEDINCREASE = 45f;

    public static final int[] K_AVAILABLE_TEMPOS = new int[] {
            20,40,50,60,80,100,120,150,180,200
    };

    public static final int K_MAX_TEMPO_WITH_HELP = 100;
    public static final int K_TEMPO_TO_TURN_HELP_ON = 60;
    public static final int K_NONOTESINDANGERZONEISDEATH = 3;   // the number of notes in the danger zone that means that we are close to death

    private int hits;
    private int misses;
    private int falseShots;

    private int topBpm;

    private int topBpmCompleted;

    private boolean isHelpOn;

    private boolean isStartingTempo;

    private boolean isLevelCompleted;

    // a map of the notes they missed for review
    private final Map<Note, Integer> notesMissed;
    private final Map<Note, Integer> notesFalselyShot;

    public ActiveScore() {
        this.isHelpOn = false;
        // keep a map of our misses
        this.notesMissed = new HashMap<Note, Integer>();
        this.notesFalselyShot = new HashMap<Note, Integer>();
        // reset the score to start nice
        reset();
    }

    public void reset() {
        this.hits = 0;
        this.misses = 0;
        this.falseShots = 0;
        this.topBpm = 0;
        this.topBpmCompleted = 0;
        this.isStartingTempo = true;
        this.isLevelCompleted = false;
        synchronized (this.notesMissed) {
            this.notesMissed.clear();
        }
        synchronized (this.notesFalselyShot) {
            this.notesFalselyShot.clear();
        }
    }

    public boolean isGameOver() {
        // return if we died (used all our permitted hits)
        return this.isLevelCompleted || this.falseShots + this.misses >= K_PERMITTED_ERRORS;
    }

    public void gameWon() {
        // we won!
        this.isLevelCompleted = true;
    }

    public boolean isLevelCompleted() {
        return isLevelCompleted;
    }

    public boolean isHelpOn() {
        return isHelpOn;
    }

    public void setIsHelpOn(boolean isHelpOn) {
        this.isHelpOn = isHelpOn;
    }

    public int setBpm(int newBpm) {
        if (isStartingTempo) {
            // this is the starting tempo, just accept this
            if (this.topBpm > 0 && newBpm > this.topBpm) {
                // this is an increase, this is no longer the start
                this.isStartingTempo = false;
            }
            else if (newBpm < this.topBpm && newBpm <= K_TEMPO_TO_TURN_HELP_ON) {
                // this is very low, help them out some
                setIsHelpOn(true);
            }
            // and set the new BPM
            this.topBpm = newBpm;
        }
        else if (newBpm > this.topBpm) {
            // this is an increase, accept this
            this.topBpm = newBpm;
            if (this.topBpm > K_MAX_TEMPO_WITH_HELP) {
                // turn off help for this
                setIsHelpOn(false);
            }
        }
        else if (newBpm <= K_TEMPO_TO_TURN_HELP_ON) {
            // this is very low, help them out some
            setIsHelpOn(true);
        }
        return this.topBpm;
    }

    public int getTopBpm() {
        return this.topBpm;
    }

    public int getTopBpmCompleted() {
        return this.topBpmCompleted;
    }

    public static int GetTempoAsPercent(int tempo) {
        float factor = tempo / (float)K_AVAILABLE_TEMPOS[K_AVAILABLE_TEMPOS.length - 1];
        return (int) (factor * 100f);
    }

    public int getScorePercent() {
        return ActiveScore.GetTempoAsPercent(this.topBpmCompleted);
    }

    public void recordBpmCompleted(Activity context) {
        this.topBpmCompleted = Math.max(this.topBpm, this.topBpmCompleted);
        State.getInstance().storeScore(context, this);
    }

    public int getMisses() {
        return this.misses;
    }

    public int getFalseShots() {
        return this.falseShots;
    }

    public int getHits() {
        return this.hits;
    }

    public int incHits(Note note) {
        synchronized (this.notesMissed) {
            Integer value = this.notesMissed.get(note);
            if (value == null) {
                // put a zero in here so it is the complete list
                this.notesMissed.put(note, 0);
            }
        }
        // increment the counter and return it
        return ++this.hits;
    }

    public int incMisses(Note note) {
        synchronized (this.notesMissed) {
            Integer value = this.notesMissed.get(note);
            if (value == null) {
                // this is the first
                value = new Integer(1);
            }
            else {
                // increment the counter
                ++value;
            }
            // put back the value
            this.notesMissed.put(note, value);
        }
        // increment the total counter of notes missed and return it
        return ++this.misses;
    }

    public int incFalseShots(Note note) {
        synchronized (this.notesFalselyShot) {
            Integer value = this.notesFalselyShot.get(note);
            if (value == null) {
                // this is the first
                value = new Integer(1);
            }
            else {
                // increment the counter
                ++value;
            }
            // put back the value
            this.notesFalselyShot.put(note, value);
        }
        synchronized (this.notesMissed) {
            Integer value = this.notesMissed.get(note);
            if (value == null) {
                // put a zero in here so it is the complete list
                this.notesMissed.put(note, 0);
            }
        }
        // increment the counter and return it
        return ++this.falseShots;
    }

    public boolean isInProgress() {
        return this.misses > 0 ||
                this.falseShots > 0 ||
                this.hits > 0;
    }

    public Note[] getNotesMissed() {
        synchronized (this.notesMissed) {
            // sort the set to note order (uses frequency)
            List<Note> sortedNotes = new ArrayList<Note>(this.notesMissed.keySet());
            Collections.sort(sortedNotes);
            return sortedNotes.toArray(new Note[sortedNotes.size()]);
        }
    }

    public Integer getNoteMissedFrequency(Note note) {
        synchronized (this.notesMissed) {
            Integer value = this.notesMissed.get(note);
            return value == null ? 0 : value.intValue();
        }
    }

    public Note[] getNotesFalselyShot() {
        synchronized (this.notesFalselyShot) {
            Set<Note> notes = this.notesFalselyShot.keySet();
            return notes.toArray(new Note[notes.size()]);
        }
    }

    public Integer getNoteFalselyShotFrequency(Note note) {
        synchronized (this.notesFalselyShot) {
            Integer value = this.notesFalselyShot.get(note);
            return value == null ? 0 : value.intValue();
        }
    }
}

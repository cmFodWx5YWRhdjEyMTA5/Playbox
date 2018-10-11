package uk.co.darkerwaters.noteinvaders.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveScore {

    public static final int K_PERMITTED_MISS_COUNT = 10;
    public static final int K_PERMITTED_FALSE_SHOT_COUNT = 10;
    public static final int K_PERMITTED_ERRORS = K_PERMITTED_MISS_COUNT + K_PERMITTED_FALSE_SHOT_COUNT;

    private int hits;
    private int misses;
    private int falseShots;

    private int topBpm;

    private boolean isHelpOn;

    // a map of the notes they missed for review
    private final Map<Note, Integer> notesMissed;

    public ActiveScore() {
        reset();
        this.isHelpOn = false;
        // keep a map of our misses
        this.notesMissed = new HashMap<Note, Integer>();
    }

    public void reset() {
        this.hits = 0;
        this.misses = 0;
        this.falseShots = 0;
        this.topBpm = 0;
    }

    public boolean isGameOver() {
        // return if we died (used all our permitted hits)
        return this.falseShots + this.misses >= K_PERMITTED_ERRORS;
    }

    public boolean isHelpOn() {
        return isHelpOn;
    }

    public void setIsHelpOn(boolean isHelpOn) {
        this.isHelpOn = isHelpOn;
    }

    public int setBpm(int newBpm) {
        this.topBpm = Math.max(this.topBpm, newBpm);
        return this.topBpm;
    }

    public int getTopBpm() {
        return this.topBpm;
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
        // increment the counter and return it
        return ++this.falseShots;
    }

    public boolean isInProgress() {
        return this.misses > 0 ||
                this.falseShots > 0 ||
                this.hits > 0;
    }
}

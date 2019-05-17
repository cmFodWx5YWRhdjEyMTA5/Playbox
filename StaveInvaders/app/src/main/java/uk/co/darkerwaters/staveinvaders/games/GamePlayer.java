package uk.co.darkerwaters.staveinvaders.games;


import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.views.MusicView;

public abstract class GamePlayer {

    public static final int K_DEFAULT_BPM = 60;

    protected final Application application;
    protected final Game game;

    private int beatsPerMinute = K_DEFAULT_BPM;
    private boolean isHelpOn = true;

    private MusicView.Clefs activeClef;
    private boolean isClefChangePermitted = false;

    private long lastChangeTime = 0l;

    private final List<GameNote> activeNotes = new ArrayList<GameNote>();

    public GamePlayer(Application application, Game game) {
        this.application = application;
        this.game = game;

        // get the active clef from the entries on the game, just use the first
        if (null != this.game && null != this.game.entries && this.game.entries.length > 0) {
            // there are entries in the list, find the active clef and see if it changes
            this.activeClef = this.game.entries[0].clef;
            // check all the entries in the list to see if the same as this
            for (int i = 1; i < this.game.entries.length; ++i) {
                // for each subsequent entry - is the clef different?
                if (this.game.entries[i].clef != this.activeClef) {
                    // this is different in some way, a change is permitted
                    this.isClefChangePermitted = true;
                    break;
                }
            }
        }
        else {
            // just go with treble for now
            this.activeClef = MusicView.Clefs.treble;
        }
    }

    public void setBeatsPerMinute(int beatsPerMinute) {
        this.beatsPerMinute = beatsPerMinute;
    }

    public int getBeatsPerMinute() {
        return this.beatsPerMinute;
    }

    public float getBeatsPerSecond() {
        return this.beatsPerMinute / 60f;
    }

    public void setIsHelpOn(boolean isHelpOn) {
        this.isHelpOn = isHelpOn;
    }

    public boolean getIsHelpOn() {
        return this.isHelpOn;
    }

    public void setActiveClef(MusicView.Clefs clef) {
        this.activeClef = clef;
    }

    public MusicView.Clefs getActiveClef() {
        return this.activeClef;
    }

    public void updateNotes(float secondsElapsed, float durationSeconds) {
        // update our list of notes
        List<GameNote> toRemove = new ArrayList<GameNote>();
        for (GameNote note : this.activeNotes) {
            // remove the time from the note
            if (note.adjustTime(-secondsElapsed) < 0f) {
                // this has dropped below zero
                toRemove.add(note);
            }
        }
        // remove all the notes timed out
        this.activeNotes.removeAll(toRemove);
        if (this.isClefChangePermitted) {
            //TODO handle changing clefs (wait for the notes to clear and swap occasionally)
            this.lastChangeTime += secondsElapsed * 1000f;
            if (this.lastChangeTime > 5000l) {
                if (getActiveClef() == MusicView.Clefs.treble) {
                    setActiveClef(MusicView.Clefs.bass);
                } else {
                    setActiveClef(MusicView.Clefs.treble);
                }
                this.lastChangeTime = 0l;
            }
        }

        // do we need any more notes in our list we will return now we have changed times?
        float lastNoteSeconds = getLastNoteSeconds(durationSeconds);
        while (lastNoteSeconds < durationSeconds) {
            // we need another at the correct interval from the last, add one here
            // 60 BPM == 1 bps so we want a note a second in this example, 1 over this to
            // return the seconds to the next note then please 1 / 2 == 0.5
            float seconds = lastNoteSeconds + (1 / getBeatsPerSecond());
            // get the next note to draw
            this.activeNotes.add(new GameNote(getNextNote(this.activeClef, seconds), seconds));
            // this is the last one now
            lastNoteSeconds = seconds;
        }
    }

    public GameNote[] getNotesToDraw(float durationSeconds) {
        if (getIsHelpOn()) {
            // just return all the notes divided by the interval over which we can draw
            if (this.game.entries.length == 0) {
                // none, log this for the developer
                Log.error("There are no entries in the game " + this.game.getFullName());
            }
            GameNote[] notes = new GameNote[this.game.entries.length];
            float interval = notes.length == 0 ? 1 : durationSeconds / notes.length;
            for (int i = 0; i < notes.length; ++i) {
                // create the note at the next time interval
                notes[i] = new GameNote(this.game.entries[i], i * interval + (interval * 0.5f));
            }
            // and return these notes
            return notes;
        }
        else {
            // just return our active list of notes
            return this.activeNotes.toArray(new GameNote[0]);
        }
    }

    protected abstract Game.GameEntry getNextNote(MusicView.Clefs activeClef, float seconds);

    private float getLastNoteSeconds(float durationSeconds) {
        if (this.activeNotes.isEmpty()) {
            // there are none
            return 0f;
        }
        else {
            return this.activeNotes.get(this.activeNotes.size() - 1).getSeconds();
        }
    }


}

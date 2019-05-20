package uk.co.darkerwaters.staveinvaders.games;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.views.MusicView;

public abstract class GamePlayer {

    public static final int K_DEFAULT_BPM = 60;
    public static final long K_HELP_CHANGE_TIME = 5000l;
    public static final long K_MIN_CHANGE_TIME = 10000l;
    public static final int K_CHANGE_TIME_SEC_ADD = 5;
    public static final float K_CLEF_CHANGE_FREEBEE_SEC = 2.5f;

    protected final Application application;
    protected final Game game;

    private int beatsPerMinute = K_DEFAULT_BPM;
    private boolean isHelpOn = true;

    private MusicView.Clefs activeClef;
    private HashSet<MusicView.Clefs> permittedClefs = new HashSet<MusicView.Clefs>(2);
    private HashSet<MusicView.Clefs> availableClefs = new HashSet<MusicView.Clefs>(2);

    private long lastChangeTime = 0l;
    private long nextSheduledChangeTime = 0l;

    protected final Random random = new Random();

    private final List<GameNote> activeNotes = new ArrayList<GameNote>();
    private boolean insertTimeGap = true;

    public GamePlayer(Application application, Game game) {
        this.application = application;
        this.game = game;

        // get the active clef from the entries on the game, just use the first
        if (null != this.game && null != this.game.entries && this.game.entries.length > 0) {
            // there are entries in the list, start off with the first one as the default
            this.activeClef = this.game.entries[0].clef;
            // check all the entries in the list to see what clefs are available
            for (int i = 0; i < this.game.entries.length; ++i) {
                // for each subsequent entry - add the clef to get them all added if available
                availableClefs.add(this.game.entries[i].clef);
            }
        }
        else {
            // just go with treble for now
            this.activeClef = MusicView.Clefs.treble;
        }
        // by default the permitted are all those available
        for (MusicView.Clefs clef : this.availableClefs) {
            setPermittedClef(clef, true);
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
        if (clef != this.activeClef) {
            // this is a change, change it
            this.activeClef = clef;
            // when there is a change we want to give the player a change to see it before
            // it arrives, insert a little gap here then
            this.insertTimeGap = true;
        }
    }

    public boolean setPermittedClef(MusicView.Clefs clef, boolean isPermitted) {
        boolean result;
        if (isPermitted) {
            result = this.permittedClefs.add(clef);
        }
        else {
            result = this.permittedClefs.remove(clef);
        }
        // reset the change time to be now
        this.nextSheduledChangeTime = System.currentTimeMillis();
        // and return the result
        return result;
    }

    public MusicView.Clefs getActiveClef() {
        return this.activeClef;
    }

    public MusicView.Clefs getCurrentClef() {
        // there might be notes from the old one
        MusicView.Clefs currentClef = this.activeClef;
        for (GameNote note : this.activeNotes) {
            // in the list of notes, the current clef is the one on the first we come across
            currentClef = note.getChord().clef;
            break;
        }
        return currentClef;
    }

    private void offsetNotes(float secondsDelta) {
        // update our list of notes
        List<GameNote> toRemove = new ArrayList<GameNote>();
        for (GameNote note : this.activeNotes) {
            // remove the time from the note
            if (note.adjustTime(secondsDelta) < 0f) {
                // this has dropped below zero
                toRemove.add(note);
            }
        }
        // remove all the notes timed out
        this.activeNotes.removeAll(toRemove);
    }

    public void updateNotes(float secondsElapsed, float durationSeconds) {
        // offset the notes the correct time
        offsetNotes(-secondsElapsed);

        // check the clef is correct
        if (false == permittedClefs.contains(this.activeClef) && false == permittedClefs.isEmpty()) {
            // the active clef is not permitted, change this to whatever is in the list
            setActiveClef(permittedClefs.iterator().next());
        }
        if (System.currentTimeMillis() > this.nextSheduledChangeTime) {
            // we are due a change on the permitted clefs
            boolean isActiveFound = false;
            MusicView.Clefs newClef = null;
            for (MusicView.Clefs clef : this.permittedClefs) {
                if (isActiveFound == false ) {
                    // not found the active one yet, is this it?
                    if (clef == this.activeClef) {
                        isActiveFound = true;
                    }
                }
                else {
                    // this is the one after the active one
                    newClef = clef;
                    break;
                }
            }
            if (null == newClef && this.permittedClefs.isEmpty() == false) {
                // looped around, select the first one
                newClef = this.permittedClefs.iterator().next();
            }
            if (null != newClef) {
                // set the new active clef
                setActiveClef(newClef);
                // and the new change time
                if (isHelpOn) {
                    // clear the list of active notes to immediately refresh them
                    this.activeNotes.clear();
                    // and schedule a new change
                    this.nextSheduledChangeTime = System.currentTimeMillis() + K_HELP_CHANGE_TIME;
                } else {
                    // schedule a new change, allowing for the current ones to clear from the list
                    this.nextSheduledChangeTime = System.currentTimeMillis() + K_MIN_CHANGE_TIME + (this.random.nextInt(K_CHANGE_TIME_SEC_ADD) * 1000l);
                }
            }
        }

        // do we need any more notes in our list we will return now we have changed times?
        float lastNoteSeconds = getLastNoteSeconds(durationSeconds);
        while (lastNoteSeconds < durationSeconds) {
            // we need another at the correct interval from the last, add one here
            // 60 BPM == 1 bps so we want a note a second in this example, 1 over this to
            // return the seconds to the next note then please 1 / 2 == 0.5
            float seconds = lastNoteSeconds + (1 / getBeatsPerSecond());
            if (this.insertTimeGap) {
                // add a gap in here
                seconds += K_CLEF_CHANGE_FREEBEE_SEC;
                this.insertTimeGap = false;
            }
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
            Game.GameEntry[] clefEntries = this.game.getClefEntries(this.activeClef);
            GameNote[] notes = new GameNote[clefEntries.length];
            float interval = notes.length == 0 ? 1 : durationSeconds / notes.length;
            for (int i = 0; i < notes.length; ++i) {
                // create the note at the next time interval
                notes[i] = new GameNote(clefEntries[i], i * interval + (interval * 0.5f));
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

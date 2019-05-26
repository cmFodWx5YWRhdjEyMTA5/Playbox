package uk.co.darkerwaters.staveinvaders.games;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.games.GameProgress.GameProgressListener;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.views.MusicView;

public abstract class GamePlayer implements GameProgressListener {

    public static final long K_HELP_CHANGE_TIME = 5000l;
    public static final long K_MIN_CHANGE_TIME = 10000l;
    public static final int K_CHANGE_TIME_BEATS_ADD = 5;
    public static final float K_CLEF_CHANGE_FREEBEE_BEATS = 5f;

    public static final int K_LEVEL_POINTS_GOAL = 250;

    public interface GamePlayerListener {
        void onGameClefChanged(Clef clef);
    };

    protected final Application application;
    protected final Game game;
    protected final GameScore score;
    protected final GameProgress progresser;

    private final List<GamePlayerListener> listeners;

    private boolean isPaused = true;
    protected boolean isInDemoMode = true;

    private Clef activeClef;
    private Clef broadcastClef = null;
    protected HashSet<Clef> permittedClefs = new HashSet<Clef>(2);
    protected HashSet<Clef> availableClefs = new HashSet<Clef>(2);

    protected final Random random = new Random();

    private final List<GameNote> activeNotes = new ArrayList<GameNote>();
    private float insertTimeGap = 0.0f;

    public GamePlayer(Application application, Game game) {
        this.application = application;
        this.game = game;
        this.listeners = new ArrayList<GamePlayerListener>();
        this.isPaused = true;

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
            this.activeClef = Clef.treble;
        }
        // by default the permitted are all those available
        for (Clef clef : this.availableClefs) {
            setPermittedClef(clef, true);
        }
        // setup our score and progress class
        this.score = new GameScore(this.game);
        this.progresser = new GameProgress();
        this.progresser.addListener(this);
    }

    public void setActiveClef(Clef clef) {
        if (clef != this.activeClef) {
            // this is a change, change it
            this.activeClef = clef;
            // when there is a change we want to give the player a change to see it before
            // it arrives, insert a little gap here then
            insertTimeGap(K_CLEF_CHANGE_FREEBEE_BEATS);
        }
    }

    public boolean addListener(GamePlayerListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(GamePlayerListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public boolean isPaused() {
        return this.isPaused;
    }

    public void insertTimeGap(float timeGapBeats) {
        this.insertTimeGap += timeGapBeats;
    }

    public boolean setPermittedClef(Clef clef, boolean isPermitted) {
        boolean result;
        if (isPermitted) {
            result = this.permittedClefs.add(clef);
        }
        else {
            result = this.permittedClefs.remove(clef);
        }
        // and return the result
        return result;
    }

    public Clef getActiveClef() {
        return this.activeClef;
    }

    public Clef getCurrentClef() {
        // there might be notes from the old one
        Clef currentClef = this.activeClef;
        GameNote[] notes;
        synchronized (this.activeNotes) {
            notes = this.activeNotes.toArray(new GameNote[0]);
        }
        for (GameNote note : notes) {
            // in the list of notes, the current clef is the one on the first we come across
            if (null != note && null != note.getChord()) {
                currentClef = note.getChord().clef;
                break;
            }
        }
        return currentClef;
    }

    public float getBeatsPerSecond() {
        return this.progresser.getBeatsPerSecond();
    }

    public int getPoints() { return this.progresser.getPoints(); }

    public int getMaxTempo() { return this.progresser.getMaxTempo(); }

    private void offsetNotes(float beatsDelta) {
        // update our list of notes
        GameNote[] notes;
        synchronized (this.activeNotes) {
            notes = this.activeNotes.toArray(new GameNote[0]);
        }
        for (GameNote note : notes) {
            // remove the time from the note
            if (note.adjustTime(beatsDelta) < 0f) {
                // this has dropped below zero handle this failure to hit this note
                registerMiss(note.getChord());
            }
        }
    }

    protected abstract boolean isPerformClefChange();

    public void updateNotes(float beatsElapsed) {
        if (false == this.isPaused || this.isInDemoMode) {
            // we are not pausd, or we are in demo mode - deal with the clef changes here
            if (false == permittedClefs.contains(this.activeClef) && false == permittedClefs.isEmpty()) {
                // the active clef is not permitted, change this to whatever is in the list
                setActiveClef(permittedClefs.iterator().next());
            }
            if (isPerformClefChange()) {
                // we are due a change on the permitted clefs
                performClefChange();
            }
        }
        // only do all the other updates when we are playing (not paused)
        if (false == this.isPaused) {
            // offset the notes the correct time
            offsetNotes(-beatsElapsed);
            // do we need any more notes in our list we will return now we have changed times?
            float lastNoteBeats = getLastNoteBeats();
            while (lastNoteBeats <= MusicView.K_BEATS_ON_VIEW + 1) {
                // we need another at the correct interval from the last, add one here
                float beats = lastNoteBeats + 1;
                if (this.insertTimeGap > 0f) {
                    // add a gap in here
                    beats += this.insertTimeGap;
                    this.insertTimeGap = 0f;
                }
                // get the next note to draw
                synchronized (this.activeNotes) {
                    this.activeNotes.add(new GameNote(getNextNote(this.activeClef, beats), beats));
                }
                // this is the last one now
                lastNoteBeats = beats;
            }
        }
        // this might have changed the current clef
        if (getCurrentClef() != this.broadcastClef) {
            broadcastClef = getCurrentClef();
            // inform any listeners of this change
            synchronized (this.listeners) {
                for (GamePlayerListener listener : this.listeners) {
                    listener.onGameClefChanged(broadcastClef);
                }
            }
        }
    }

    protected void performClefChange() {
        boolean isActiveFound = false;
        Clef newClef = null;
        for (Clef clef : this.permittedClefs) {
            if (isActiveFound == false) {
                // not found the active one yet, is this it?
                if (clef == this.activeClef) {
                    isActiveFound = true;
                }
            } else {
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
            if (isInDemoMode) {
                // clear the list of active notes to immediately refresh them
                synchronized (this.activeNotes) {
                    this.activeNotes.clear();
                }
            }
        }
    }

    public boolean isGameActive() {
        return this.progresser.isGameActive();
    }

    public boolean isHelpLettersShowing() {
        return this.progresser.getIsHelpOn();
    }

    public void setIsHelpLettersShowing(boolean isShowing) { this.progresser.setIsHelpOn(isShowing); }

    public void setTempo(int tempo) { this.progresser.setTempo(tempo); }

    public int getTempo() { return this.progresser.getTempo(); }

    public GameNote[] getNotesToDraw() {
        if (this.isInDemoMode) {
            // just return all the notes divided by the interval over which we can draw
            if (this.game.entries.length == 0) {
                // none, log this for the developer
                Log.error("There are no entries in the game " + this.game.getFullName());
            }
            Game.GameEntry[] clefEntries = this.game.getClefEntries(this.activeClef);
            GameNote[] notes = new GameNote[clefEntries.length];
            float interval = notes.length == 0 ? 1 : MusicView.K_BEATS_ON_VIEW / (float)notes.length;
            for (int i = 0; i < notes.length; ++i) {
                // create the note at the next time interval
                notes[i] = new GameNote(clefEntries[i], i * interval + (interval * 0.5f));
            }
            // and return these notes
            return notes;
        }
        else {
            // just return our active list of notes
            synchronized (this.activeNotes) {
                return this.activeNotes.toArray(new GameNote[0]);
            }
        }
    }

    public float destroyNote(Game.GameEntry entry) {
        // destroy the specified entry, hit!
        GameNote toRemove = null;
        GameNote[] notes;
        synchronized (this.activeNotes) {
            notes = this.activeNotes.toArray(new GameNote[0]);
        }
        for (GameNote note : notes) {
            if (note.getChord() == entry) {
                // this is the one to destroy
                toRemove = note;
                break;
            }
        }
        float hitBeatsOffset = -1f;
        if (null != toRemove) {
            hitBeatsOffset = toRemove.getOffsetBeats();
            synchronized (this.activeNotes) {
                this.activeNotes.remove(toRemove);
            }
        }
        // log the success of this note destruction
        registerHit(entry, hitBeatsOffset);
        return hitBeatsOffset;
    }

    protected int getPointsForHit(Game.GameEntry entry, float offsetBeats) {
        // by default the points are just the offset in seconds
        return (int)(offsetBeats + 1);
    }

    public int getPointLevelGoal() {
        return K_LEVEL_POINTS_GOAL;
    }

    protected void registerHit(Game.GameEntry entry, float offsetBeats) {
        // called as a not is successfully hit on the view
        if (isGameActive()) {
            this.score.recordHit(entry.clef, this.progresser.getTempo(), entry.chord);
            int points = getPointsForHit(entry, offsetBeats);
            this.progresser.recordHit(entry.clef, entry.chord, points);
        }
    }

    protected void registerMiss(Game.GameEntry entry) {
        // called as a note's time goes below zero (failed to hit it)
        if (isGameActive()) {
            this.score.recordMiss(entry.clef, this.progresser.getTempo(), entry.chord);
            this.progresser.recordMiss(entry.clef, entry.chord);
        }
    }

    public void registerMisfire(Game.GameEntry target, Chord actual) {
        // record this on the score
        if (isGameActive()) {
            this.score.recordMissfire(target.clef, this.progresser.getTempo(), target.chord, actual);
            this.progresser.recordMissire(target.clef,  target.chord, actual);
        }
    }

    protected abstract Game.GameEntry getNextNote(Clef activeClef, float seconds);

    private float getLastNoteBeats() {
        synchronized (this.activeNotes) {
            if (this.activeNotes.isEmpty()) {
                // there are none
                return MusicView.K_BEATS_ON_VIEW;
            } else {
                return this.activeNotes.get(this.activeNotes.size() - 1).getOffsetBeats();
            }
        }
    }

    @Override
    public void onGameProgressChanged(GameProgress source, GameProgress.Type type, Object data) {
        // called as the progress of our current game changes,
        switch (type) {
            case gameOver:
            case gameStarted:
            case shotLost:
            case targetHit:
                break;
            case lifeLost:
            case tempoIncrease:
            case lettersDisabled:
                // when we lose a life or the level changes, give them a chance to recover
                offsetNotes(K_CLEF_CHANGE_FREEBEE_BEATS);
                break;
        }
    }

    public boolean addListener(GameProgressListener listener) {
        return this.progresser.addListener(listener);
    }

    public boolean removeListener(GameProgressListener listener) {
        return this.progresser.removeListener(listener);
    }

    public void resetGame() {
        this.isInDemoMode = true;
    }

    public void startNewGame(int tempo, boolean isHelpOn) {
        // start a new game, stop help
        this.isInDemoMode = false;
        this.progresser.startNewGame(tempo, isHelpOn, getPointLevelGoal());
        // clear the notes
        synchronized (this.activeNotes) {
            this.activeNotes.clear();
        }
    }
}

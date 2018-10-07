package uk.co.darkerwaters.noteinvaders.views;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.State;

public class MusicViewNoteProviderTempo extends MusicViewNoteProvider {

    //public final static float K_SECONDSINVIEW = 10f;
    public final static int K_BEATSINVIEW = 10;

    private final float K_MAXSECONDSTOREPRESENT = 65535f; /*the number of secs before we reset our timer*/

    private int notesOnView = K_BEATSINVIEW;
    private float secondsInView = 10f;
    private long lastTimeDrawn = 0l;
    private float startSeconds = 0f;

    private float beatsPerSec;
    private volatile boolean isPaused = false;

    private class TimedNote extends MusicViewNote {
        float timeOffset;
        float xPosition;
        TimedNote(float timeOffset, float xPosition, Note note, String noteName) {
            super(note, noteName);
            this.timeOffset = timeOffset;
            this.xPosition = xPosition;
        }
        @Override
        public float getXPosition() {
            return xPosition;
        }
    }

    private final List<TimedNote> notesToDrawTreble = new ArrayList<TimedNote>();
    private final List<TimedNote> notesToDrawBass = new ArrayList<TimedNote>();

    public MusicViewNoteProviderTempo() {
        // initialise the view
        setBeats(60);
    }

    @Override
    public void clearNotes() {
        synchronized (this.notesToDrawTreble) {
            this.notesToDrawTreble.clear();
        }
        synchronized (this.notesToDrawBass) {
            this.notesToDrawBass.clear();
        }
    }

    public void setBeats(int bpm) {
        // set the beats per sec between notes
        this.beatsPerSec = bpm / 60f;
        // update all the notes on the view to this new beat
        // set the number of notes this will fit on the view
        this.notesOnView = K_BEATSINVIEW;// old was was seconds in view(int)(this.beatsPerSec * K_SECONDSINVIEW);
        // seconds on teh view is the notes * the time they represent in seconds
        this.secondsInView = this.notesOnView / this.beatsPerSec;
        // doing this while paused means that the offsets will be recalculated from the pixels
        // so we don't have to do anything with the notes already in the view
    }

    public int getBeats() {
        return (int)(this.beatsPerSec * 60f);
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public boolean isPaused() {
        return this.isPaused;
    }

    private float getXStart(MusicView musicView) {
        return musicView.getNoteStartX();
    }

    private float getXEnd(MusicView musicView) {
        return Math.max(100, musicView.getWidth() - musicView.getCanvasPadding().right);
    }

    private float getXSeconds(float xStart, float xEnd) {
        float width = xEnd - xStart;
        // calculate the number of pixels that we want to repesent a second of time
        return width / this.secondsInView;// old way was via the seconds on view K_SECONDSINVIEW;
    }

    @Override
    public void updateNotes(MusicView musicView) {
        // update all our notes on this view for this time
        if (this.lastTimeDrawn == 0) {
            this.lastTimeDrawn = System.currentTimeMillis();
        }
        // calculate the elapsed time since we last drew the data
        long elapsedTime = System.currentTimeMillis() - this.lastTimeDrawn;
        if (elapsedTime < 0) {
            // the elapsed time since 1970 just overran, ignore this, first start the time again
            this.lastTimeDrawn = System.currentTimeMillis();
            return;
        }
        // if we are here then some time has elapsed, need to move all the notes to their correct
        // locations on the view for the times the are to represent
        this.startSeconds += elapsedTime / 1000f;
        // update the positions of all our notes on this view for this time
        float xStart = getXStart(musicView);
        float xEnd = getXEnd(musicView);
        // calculate the number of pixels that we want to repesent a second of time
        float xSeconds = getXSeconds(xStart, xEnd);
        if (false == this.isPaused && this.startSeconds < K_MAXSECONDSTOREPRESENT) {
            // if we are here then some time has elapsed, need to move all the notes to their correct
            // locations on the view for the times the are to represent
            float secondsTillPlay;
            synchronized (this.notesToDrawTreble) {
                for (TimedNote note : this.notesToDrawTreble) {
                    // get the time this is from the start
                    secondsTillPlay = note.timeOffset - this.startSeconds;
                    // update the position
                    note.xPosition = xStart + (xSeconds * secondsTillPlay);
                }
            }
            synchronized (this.notesToDrawBass) {
                for (TimedNote note : this.notesToDrawBass) {
                    // get the time this is from the start
                    secondsTillPlay = note.timeOffset - this.startSeconds;
                    // update the position
                    note.xPosition = xStart + (xSeconds * secondsTillPlay);
                }
            }
        }
        else {
            // we are paused, but time continues to march on, adjust the start time and the times
            // of all our notes in the list accordingly
            this.startSeconds = 0f;
            synchronized (this.notesToDrawTreble) {
                for (TimedNote note : this.notesToDrawTreble) {
                    // the time to play is the xPosition * the seconds per pixel they represent
                    note.timeOffset = (note.xPosition - xStart) / xSeconds;
                }
            }
            synchronized (this.notesToDrawBass) {
                for (TimedNote note : this.notesToDrawBass) {
                    // the time to play is the xPosition * the seconds per pixel they represent
                    note.timeOffset = (note.xPosition - xStart) / xSeconds;
                }
            }
        }

        // reset the time calc so we can move the notes along the next time
        this.lastTimeDrawn = System.currentTimeMillis();
    }

    @Override
    public int getNotesFitOnView() {
        return (int)(this.beatsPerSec * this.secondsInView); // old way was seconds K_SECONDSINVIEW);
    }

    @Override
    public int getNoteCountTreble() {
        synchronized (this.notesToDrawTreble) {
            return this.notesToDrawTreble.size();
        }
    }

    @Override
    public int getNoteCountBass() {
        synchronized (this.notesToDrawBass) {
            return this.notesToDrawBass.size();
        }
    }

    @Override
    public MusicViewNote[] getNotesToDrawTreble() {
        MusicViewNote[] toDraw;
        synchronized (this.notesToDrawTreble) {
            toDraw = this.notesToDrawTreble.toArray(new MusicViewNote[this.notesToDrawTreble.size()]);
        }
        return toDraw;
    }

    @Override
    public MusicViewNote[] getNotesToDrawBass() {
        MusicViewNote[] toDraw;
        synchronized (this.notesToDrawBass) {
            toDraw = this.notesToDrawBass.toArray(new MusicViewNote[this.notesToDrawBass.size()]);
        }
        return toDraw;
    }

    @Override
    public boolean removeNoteTreble(MusicViewNote note) {
        boolean isRemoved;
        synchronized (this.notesToDrawTreble) {
            isRemoved = this.notesToDrawTreble.remove(note);
        }
        return isRemoved;
    }

    @Override
    public boolean removeNoteBass(MusicViewNote note) {
        boolean isRemoved;
        synchronized (this.notesToDrawBass) {
            isRemoved = this.notesToDrawBass.remove(note);
        }
        return isRemoved;
    }

    private boolean isValid(TimedNote note) {
        return null != note && note.xPosition > 0 && note.timeOffset > this.startSeconds;
    }

    @Override
    public boolean pushNoteBassToEnd(Note note, String noteName, MusicView musicView) {
        // get the last note we have in our lists, will be the last time we added
        float lastTimeX = this.secondsInView + this.startSeconds;// old was was defined K_SECONDSINVIEW;
        // but we might have later notes on the view already (waiting to appear)
        TimedNote lastNote = getLastNote();
        if (null != lastNote) {
            // there is a last note, the last time is this plus the current seconds between notes
            lastTimeX = lastNote.timeOffset + (1f / this.beatsPerSec);
        }
        // create a note for this time
        boolean isAdded = false;
        synchronized (this.notesToDrawBass) {
            TimedNote newNote = new TimedNote(lastTimeX, calculateXPosition(musicView, lastTimeX), note, noteName);
            if (null != newNote && isValid(newNote)) {
                isAdded =  this.notesToDrawBass.add(newNote);
            }
        }
        return isAdded;
    }

    @Override
    public boolean pushNoteTrebleToEnd(Note note, String noteName, MusicView musicView) {
        // get the last note we have in our lists, will be the last time we added
        float lastTimeX = this.secondsInView + this.startSeconds;// old was was defined K_SECONDSINVIEW;
        // but we might have later notes on the view already (waiting to appear)
        TimedNote lastNote = getLastNote();
        if (null != lastNote) {
            // there is a last note, the last time is this plus the current seconds between notes
            lastTimeX = lastNote.timeOffset + (1f / this.beatsPerSec);
        }
        // create a note for this time
        boolean isAdded = false;
        synchronized (this.notesToDrawTreble) {
            TimedNote newNote = new TimedNote(lastTimeX, calculateXPosition(musicView, lastTimeX), note, noteName);
            if (null != newNote && isValid(newNote)) {
                isAdded = this.notesToDrawTreble.add(newNote);
            }
        }
        return isAdded;
    }

    private float calculateXPosition(MusicView musicView, float timeX) {
        float xStart = getXStart(musicView);
        float xEnd = getXEnd(musicView);
        // calculate the number of pixels that we want to repesent a second of time
        float xSeconds = getXSeconds(xStart, xEnd);
        // the xPosition is the start + the time * the seconds
        return xStart + (xSeconds * timeX);
    }

    private TimedNote getLastNote() {
        TimedNote treble = getLastTrebleNote();
        TimedNote bass = getLastBassNote();
        if (treble == null && bass != null) {
            // just the one
            return bass;
        }
        else if (treble != null && bass == null) {
            // just the other one
            return treble;
        }
        else if (treble == null && bass == null) {
            // none
            return null;
        }
        else {
            // both
            if (treble.timeOffset > bass.timeOffset) {
                return treble;
            }
            else {
                return bass;
            }
        }
    }

    private TimedNote getLastTrebleNote() {
        TimedNote lastNote = null;
        synchronized (this.notesToDrawTreble) {
            if (false == this.notesToDrawTreble.isEmpty()) {
                lastNote = this.notesToDrawTreble.get(this.notesToDrawTreble.size() - 1);
            }
        }
        return lastNote;
    }

    private TimedNote getLastBassNote() {
        TimedNote lastNote = null;
        synchronized (this.notesToDrawBass) {
            if (false == this.notesToDrawBass.isEmpty()) {
                lastNote = this.notesToDrawBass.get(this.notesToDrawBass.size() - 1);
            }
        }
        return lastNote;
    }
}

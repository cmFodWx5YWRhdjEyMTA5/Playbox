package uk.co.darkerwaters.noteinvaders.views;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.state.Note;

public class MusicViewNoteProviderTempo extends MusicViewNoteProvider {

    public final static float K_SECONDSINVIEW = 10f;

    private int notesOnView = 0;
    private long lastTimeDrawn = 0l;

    private float beatsPerSec;

    private class TimedNote extends MusicViewNote {
        float timeOffset;
        float xPosition;
        TimedNote(float timeOffset, float xPosition, Note note) {
            super(note);
            this.timeOffset = timeOffset;
            this.xPosition = xPosition;
        }
        @Override
        float getXPosition() {
            return xPosition;
        }
    }

    private final List<TimedNote> notesToDrawTreble = new ArrayList<TimedNote>();
    private final List<TimedNote> notesToDrawBass = new ArrayList<TimedNote>();

    public MusicViewNoteProviderTempo() {
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
        this.notesOnView = (int)(this.beatsPerSec * K_SECONDSINVIEW);
        //TODO go through all our notes in order now and adjust the spacing to the new beat selected
        //for now we can just clear all the old ones
        clearNotes();
    }

    public int getBeats() {
        return (int)(this.beatsPerSec * 60f);
    }

    private float getXStart(MusicView musicView) {
        return musicView.getNoteStartX();
    }

    private float getXEnd(MusicView musicView) {
        return musicView.getWidth() - musicView.getCanvasPadding().right;
    }

    private float getXSeconds(float xStart, float xEnd) {
        float width = xEnd - xStart;
        // calculate the number of pixels that we want to repesent a second of time
        return width / K_SECONDSINVIEW;
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
        float xStart = getXStart(musicView);
        float xEnd = getXEnd(musicView);
        // calculate the number of pixels that we want to repesent a second of time
        float xSeconds = getXSeconds(xStart, xEnd);

        // let's reduce al the time offsets by the according number of milliseconds
        float secondsElapsed = elapsedTime / 1000f;
        synchronized (this.notesToDrawTreble) {
            for (TimedNote note : this.notesToDrawTreble) {
                // offset the time
                note.timeOffset -= secondsElapsed;
                // update the position
                note.xPosition = xStart + (xSeconds * note.timeOffset);
            }
        }
        synchronized (this.notesToDrawBass) {
            for (TimedNote note : this.notesToDrawBass) {
                note.timeOffset -= secondsElapsed;
                // update the position
                note.xPosition = xStart + (xSeconds * note.timeOffset);
            }
        }

        // reset the time calc so we can move the notes along the next time
        this.lastTimeDrawn = System.currentTimeMillis();
    }

    @Override
    public int getNotesFitOnView() {
        return (int)(this.beatsPerSec * K_SECONDSINVIEW);
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

    @Override
    public boolean pushNoteBassToEnd(Note note, MusicView musicView) {
        // get the last note we have in our lists, will be the last time we added
        float lastTimeX = K_SECONDSINVIEW;
        // but we might have later notes on the view already (waiting to appear)
        TimedNote lastNote = getLastNote();
        if (null != lastNote) {
            // there is a last note, the last time is this plus the current seconds between notes
            lastTimeX = lastNote.timeOffset + (1f / this.beatsPerSec);
        }
        // create a note for this time
        synchronized (this.notesToDrawBass) {
            return this.notesToDrawBass.add(new TimedNote(lastTimeX, calculateXPosition(musicView, lastTimeX), note));
        }
    }

    @Override
    public boolean pushNoteTrebleToEnd(Note note, MusicView musicView) {
        // get the last note we have in our lists, will be the last time we added
        float lastTimeX = K_SECONDSINVIEW;
        // but we might have later notes on the view already (waiting to appear)
        TimedNote lastNote = getLastNote();
        if (null != lastNote) {
            // there is a last note, the last time is this plus the current seconds between notes
            lastTimeX = lastNote.timeOffset + (1f / this.beatsPerSec);
        }
        // create a note for this time
        synchronized (this.notesToDrawTreble) {
            return this.notesToDrawTreble.add(new TimedNote(lastTimeX, calculateXPosition(musicView, lastTimeX), note));
        }
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

package uk.co.darkerwaters.noteinvaders.views;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.NoteInvaders;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Playable;


public class MusicViewNoteProviderTempo extends MusicViewNoteProvider {

    private final float K_MAXSECONDSTOREPRESENT = 65535f; /*the number of secs before we reset our timer*/

    private long lastTimeDrawn = 0l;
    private float startSeconds = 0f;
    private float beatsPerSec = 1f;
    private float noteSpacing = 10f;
    private int notesOnView = 0;

    private class TimedPlayable extends MusicViewPlayable {
        float timeOffset;
        float xPosition;
        TimedPlayable(float timeOffset, float xPosition, Playable playable, String noteName, String annotation) {
            super(playable, noteName, annotation);
            this.timeOffset = timeOffset;
            this.xPosition = xPosition;
        }
        @Override
        public float getXPosition() {
            return xPosition;
        }
    }

    private final List<TimedPlayable> notesToDrawTreble = new ArrayList<TimedPlayable>();
    private final List<TimedPlayable> notesToDrawBass = new ArrayList<TimedPlayable>();

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
        float oldBps = this.beatsPerSec;
        // set the beats per sec between notes
        this.beatsPerSec = bpm / 60f;
        // doing this while paused means that the offsets will be recalculated from the pixels
        // so we don't have to do anything with the notes already in the view
        if (oldBps > this.beatsPerSec) {
            // there is a reduction in speed, clear the notes to give them a chance
            clearNotes();
        }
    }

    public int getBeats() {
        return (int)(this.beatsPerSec * 60f);
    }

    private float getXStart(MusicView musicView) {
        return musicView.getNoteStartX();
    }

    private float getXEnd(MusicView musicView) {
        return Math.max(100, musicView.getWidth() - musicView.getCanvasPadding().right);
    }

    @Override
    public void initialiseNotes(MusicView musicView) {
        // initialise the base class
        super.initialiseNotes(musicView);
        // initialise the spacing between notes and the speed of the scroll on this view
        float noteRadius = musicView.getNoteRadius();
        this.noteSpacing = noteRadius * 2f;
        // initially we can show the notes from the level in a nice friendly order
        Game level = NoteInvaders.getAppContext().getGameSelected();
        final float widthFactor = 0.95f;
        final float xStart = musicView.getNoteStartX();
        final float xWidth = musicView.getWidth() - xStart;
        if (null != level.treble_clef && level.treble_clef.length > 0) {
            // we can take a large section of the view (95%) and divide equally by the note length
            float xSep = xWidth * widthFactor / level.treble_clef.length;
            for (int i = 0; i < level.treble_clef.length; ++i) {
                String noteName = "";
                String annotation = level.getTrebleAnnotations(i);
                Playable note = level.treble_clef[i];
                if (null != level.treble_names && level.treble_names.length > i) {
                    noteName = level.treble_names[i];
                }
                float positionX = xSep * (i+1);
                float timeX = this.startSeconds + positionX;
                synchronized (this.notesToDrawTreble) {
                    TimedPlayable newNote = new TimedPlayable(timeX, positionX, note, noteName, annotation);
                    if (null != newNote && isValid(newNote)) {
                        this.notesToDrawTreble.add(newNote);
                    }
                }
            }
        }
        // do the bass clef too
        if (null != level.bass_clef && level.bass_clef.length > 0) {
            // we can take a large section of the view (95%) and divide equally by the note length
            float xSep = xWidth * widthFactor / level.bass_clef.length;
            for (int i = 0; i < level.bass_clef.length; ++i) {
                String noteName = "";
                String annotation = level.getBassAnnotations(i);
                Playable note = level.bass_clef[i];
                if (null != level.bass_names && level.bass_names.length > i) {
                    noteName = level.bass_names[i];
                }
                float positionX = xSep * (i+1);
                float timeX = this.startSeconds + positionX;
                synchronized (this.notesToDrawBass) {
                    TimedPlayable newNote = new TimedPlayable(timeX, positionX, note, noteName, annotation);
                    if (null != newNote && isValid(newNote)) {
                        this.notesToDrawBass.add(newNote);
                    }
                }
            }
        }
    }

    @Override
    public void setStarted(boolean isStarted) {
        if (isStarted) {
            // clear the helping notes
            clearNotes();
        }
        // and start
        super.setStarted(isStarted);
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
        // check / reset the note spacing
        this.noteSpacing = musicView.getNoteRadius() * 8f;
        this.notesOnView = (int)((xEnd - xStart) / this.noteSpacing);
        // need to calculate the number of pixels for every second that passes
        float xSeconds = this.noteSpacing * this.beatsPerSec;
        if (false == this.isPaused() && this.startSeconds < K_MAXSECONDSTOREPRESENT) {
            // if we are here then some time has elapsed, need to move all the notes to their correct
            // locations on the view for the times the are to represent
            float secondsTillPlay;
            synchronized (this.notesToDrawTreble) {
                for (TimedPlayable note : this.notesToDrawTreble) {
                    // get the time this is from the start
                    secondsTillPlay = note.timeOffset - this.startSeconds;
                    // update the position
                    note.xPosition = xStart + (xSeconds * secondsTillPlay);
                }
            }
            synchronized (this.notesToDrawBass) {
                for (TimedPlayable note : this.notesToDrawBass) {
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
                for (TimedPlayable note : this.notesToDrawTreble) {
                    // the time to play is the xPosition * the seconds per pixel they represent
                    note.timeOffset = (note.xPosition - xStart) / xSeconds;
                }
            }
            synchronized (this.notesToDrawBass) {
                for (TimedPlayable note : this.notesToDrawBass) {
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
        return this.notesOnView;
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
    public MusicViewPlayable[] getNotesToDrawTreble() {
        MusicViewPlayable[] toDraw;
        synchronized (this.notesToDrawTreble) {
            toDraw = this.notesToDrawTreble.toArray(new MusicViewPlayable[this.notesToDrawTreble.size()]);
        }
        return toDraw;
    }

    @Override
    public MusicViewPlayable[] getNotesToDrawBass() {
        MusicViewPlayable[] toDraw;
        synchronized (this.notesToDrawBass) {
            toDraw = this.notesToDrawBass.toArray(new MusicViewPlayable[this.notesToDrawBass.size()]);
        }
        return toDraw;
    }

    @Override
    public boolean removeNoteTreble(MusicViewPlayable note) {
        boolean isRemoved;
        synchronized (this.notesToDrawTreble) {
            isRemoved = this.notesToDrawTreble.remove(note);
        }
        return isRemoved;
    }

    @Override
    public boolean removeNoteBass(MusicViewPlayable note) {
        boolean isRemoved;
        synchronized (this.notesToDrawBass) {
            isRemoved = this.notesToDrawBass.remove(note);
        }
        return isRemoved;
    }

    private boolean isValid(TimedPlayable note) {
        return null != note && note.xPosition > 0 && note.timeOffset > this.startSeconds;
    }

    @Override
    public boolean pushNoteBassToEnd(Playable note, String noteName, String annotation, MusicView musicView) {
        return this.pushNoteBassToEnd(note, noteName, annotation, musicView, 1f / this.beatsPerSec);
    }

    @Override
    public boolean pushNoteBassToEnd(Playable note, String noteName, String annotation, MusicView musicView, float timeOffset) {
        // get the last note we have in our lists, will be the last time we added
        // the last time is the beats (notes) per sec
        float lastTimeX = (this.notesOnView / this.beatsPerSec) + this.startSeconds;
        // but we might have later notes on the view already (waiting to appear)
        TimedPlayable lastNote = getLastNote();
        if (null != lastNote) {
            // there is a last note, the last time is this plus the current seconds between notes
            lastTimeX = lastNote.timeOffset + timeOffset;
        }
        // create a note for this time
        boolean isAdded = false;
        synchronized (this.notesToDrawBass) {
            TimedPlayable newNote = new TimedPlayable(lastTimeX, calculateXPosition(musicView, lastTimeX), note, noteName, annotation);
            if (null != newNote && isValid(newNote)) {
                isAdded =  this.notesToDrawBass.add(newNote);
            }
        }
        return isAdded;
    }

    @Override
    public boolean pushNoteTrebleToEnd(Playable note, String noteName, String annotation, MusicView musicView) {
        return this.pushNoteTrebleToEnd(note, noteName, annotation, musicView, 1f / this.beatsPerSec);
    }

    @Override
    public boolean pushNoteTrebleToEnd(Playable note, String noteName, String annotation, MusicView musicView, float timeOffset) {
        // get the last note we have in our lists, will be the last time we added
        // the last time is the beats (notes) per sec
        float lastTimeX = (this.notesOnView / this.beatsPerSec) + this.startSeconds;
        // but we might have later notes on the view already (waiting to appear)
        TimedPlayable lastNote = getLastNote();
        if (null != lastNote) {
            // there is a last note, the last time is this plus the current seconds between notes
            lastTimeX = lastNote.timeOffset + (1f / this.beatsPerSec);
        }
        // create a note for this time
        boolean isAdded = false;
        synchronized (this.notesToDrawTreble) {
            TimedPlayable newNote = new TimedPlayable(lastTimeX, calculateXPosition(musicView, lastTimeX), note, noteName, annotation);
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
        float xSeconds = this.noteSpacing * this.beatsPerSec;
        // the xPosition is the start + the time * the seconds
        return xStart + (xSeconds * timeX);
    }

    @Override
    public float getLastNotePosition(float defaultX) {
        TimedPlayable lastNote = getLastNote();
        if (null == lastNote) {
            // return error
            return defaultX;
        }
        else {
            // return the right position
            return lastNote.getXPosition();
        }
    }

    public TimedPlayable getLastNote() {
        TimedPlayable treble = getLastTrebleNote();
        TimedPlayable bass = getLastBassNote();
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

    private TimedPlayable getLastTrebleNote() {
        TimedPlayable lastNote = null;
        synchronized (this.notesToDrawTreble) {
            if (false == this.notesToDrawTreble.isEmpty()) {
                lastNote = this.notesToDrawTreble.get(this.notesToDrawTreble.size() - 1);
            }
        }
        return lastNote;
    }

    private TimedPlayable getLastBassNote() {
        TimedPlayable lastNote = null;
        synchronized (this.notesToDrawBass) {
            if (false == this.notesToDrawBass.isEmpty()) {
                lastNote = this.notesToDrawBass.get(this.notesToDrawBass.size() - 1);
            }
        }
        return lastNote;
    }
}

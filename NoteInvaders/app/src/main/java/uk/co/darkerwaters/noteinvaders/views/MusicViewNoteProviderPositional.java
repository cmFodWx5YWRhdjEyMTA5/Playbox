package uk.co.darkerwaters.noteinvaders.views;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Playable;

public class MusicViewNoteProviderPositional extends MusicViewNoteProvider {


    private class PositionedNote extends MusicViewPlayable {
        float xPosition;
        PositionedNote(float xPosition, Playable note, String noteName) {
            super(note, noteName);
            this.xPosition = xPosition;
        }
        @Override
        public float getXPosition() {
            return this.xPosition;
        }
    }

    private final List<PositionedNote> notesToDrawTreble = new ArrayList<PositionedNote>();
    private final List<PositionedNote> notesToDrawBass = new ArrayList<PositionedNote>();

    private float noteSeparation = 80f;

    public MusicViewNoteProviderPositional(MusicView view) {
        // create this provider
        view.setViewProvider(this);
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

    @Override
    public void initialiseNotes(MusicView musicView) {
        super.initialiseNotes(musicView);
    }

    @Override
    public void updateNotes(MusicView musicView) {
        // update all our notes on this view
        shiftNotesLeft(1);
    }

    @Override
    public int getNotesFitOnView() {
        return 10;
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

    public boolean pushNoteTreble(Playable note, String noteName, float xPosition) {
        synchronized (this.notesToDrawTreble) {
            return this.notesToDrawTreble.add(new PositionedNote(xPosition, note, noteName));
        }
    }

    public int getNoteCountTreble() {
        synchronized (this.notesToDrawTreble) {
            return this.notesToDrawTreble.size();
        }
    }

    public boolean pushNoteBass(Playable note, String noteName, float xPosition) {
        synchronized (this.notesToDrawBass) {
            return this.notesToDrawBass.add(new PositionedNote(xPosition, note, noteName));
        }
    }

    public int getNoteCountBass() {
        synchronized (this.notesToDrawBass) {
            return this.notesToDrawBass.size();
        }
    }

    @Override
    public float getLastNotePosition(float defaultX) {
        return Math.max(getLastBassPosition(defaultX), getLastTreblePosition(defaultX));
    }

    public float getLastTreblePosition(float defaultX) {
        float startX = defaultX;
        synchronized (this.notesToDrawTreble) {
            if (this.notesToDrawTreble.size() > 0) {
                // get the last x used
                startX = this.notesToDrawTreble.get(this.notesToDrawTreble.size() - 1).xPosition;
            }
        }
        return startX;
    }

    @Override
    public boolean pushNoteTrebleToEnd(Playable note, String noteName, MusicView musicView) {
        // put this note on the end of the view
        return pushNoteTreble(note, noteName, getLastNotePosition(musicView.getWidth()) + noteSeparation);
    }

    public float getLastBassPosition(float defaultX) {
        float startX = defaultX;
        synchronized (this.notesToDrawBass) {
            if (this.notesToDrawBass.size() > 0) {
                // get the last x used
                startX = this.notesToDrawBass.get(this.notesToDrawBass.size() - 1).xPosition;
            }
        }
        return startX;
    }

    @Override
    public boolean pushNoteBassToEnd(Playable note, String noteName, MusicView musicView) {
        // put this note on the end of the view
        return pushNoteBass(note, noteName, getLastNotePosition(musicView.getWidth()) + noteSeparation);
    }

    public void shiftNotesLeft(int pixels) {
        synchronized (this.notesToDrawTreble) {
            for (PositionedNote note : this.notesToDrawTreble) {
                note.xPosition -= pixels;
            }
        }
        synchronized (this.notesToDrawBass) {
            for (PositionedNote note : this.notesToDrawBass) {
                note.xPosition -= pixels;
            }
        }
    }

    @Override
    public boolean removeNoteTreble(MusicViewPlayable note) {
        boolean isRemoved = false;
        synchronized (this.notesToDrawTreble) {
            isRemoved = this.notesToDrawTreble.remove(note);
        }
        return isRemoved;
    }

    @Override
    public boolean removeNoteBass(MusicViewPlayable note) {
        boolean isRemoved = false;
        synchronized (this.notesToDrawBass) {
            isRemoved = this.notesToDrawBass.remove(note);
        }
        return isRemoved;
    }
}

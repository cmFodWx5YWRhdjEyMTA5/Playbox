package uk.co.darkerwaters.noteinvaders.views;

import uk.co.darkerwaters.noteinvaders.state.Note;

public abstract class MusicViewNote {

    public final Note note;

    public final String noteName;

    public MusicViewNote(Note note, String noteName) {
        this.note = note;
        this.noteName = noteName;
    }

    @Override
    public boolean equals(Object compare) {
        if (compare == null || false == compare instanceof MusicViewNote) {
            // not the same
            return false;
        }
        else {
            return this.note.equals(((MusicViewNote)compare).note);
        }
    }

    public abstract float getXPosition();
}

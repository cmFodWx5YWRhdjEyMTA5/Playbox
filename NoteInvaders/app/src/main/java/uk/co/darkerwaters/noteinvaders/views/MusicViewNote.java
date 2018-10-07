package uk.co.darkerwaters.noteinvaders.views;

import uk.co.darkerwaters.noteinvaders.state.Note;

public abstract class MusicViewNote {

    final Note note;

    final String noteName;

    public MusicViewNote(Note note, String noteName) {
        this.note = note;
        this.noteName = noteName;
    }

    abstract float getXPosition();
}

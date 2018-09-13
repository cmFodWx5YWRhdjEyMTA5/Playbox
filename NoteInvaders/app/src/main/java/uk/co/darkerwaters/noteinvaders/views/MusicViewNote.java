package uk.co.darkerwaters.noteinvaders.views;

import uk.co.darkerwaters.noteinvaders.state.Note;

public abstract class MusicViewNote {

    final Note note;

    public MusicViewNote(Note note) {
        this.note = note;
    }

    abstract float getXPosition();
}

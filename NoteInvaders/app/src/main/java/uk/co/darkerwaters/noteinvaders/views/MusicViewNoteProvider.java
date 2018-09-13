package uk.co.darkerwaters.noteinvaders.views;

import uk.co.darkerwaters.noteinvaders.state.Note;

public abstract class MusicViewNoteProvider {
    public abstract void clearNotes();

    public abstract MusicViewNote[] getNotesToDrawTreble();
    public abstract MusicViewNote[] getNotesToDrawBass();

    public abstract int getNoteCountTreble();
    public abstract int getNoteCountBass();

    public abstract int getNotesFitOnView();

    public abstract void updateNotes(MusicView musicView);

    public abstract boolean pushNoteTrebleToEnd(Note note, MusicView musicView);

    public abstract boolean pushNoteBassToEnd(Note note, MusicView musicView);

    public abstract boolean removeNoteTreble(MusicViewNote note);
    public abstract boolean removeNoteBass(MusicViewNote note);
}

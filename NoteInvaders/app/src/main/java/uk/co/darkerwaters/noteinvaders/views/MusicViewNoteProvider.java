package uk.co.darkerwaters.noteinvaders.views;

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Playable;

public abstract class MusicViewNoteProvider {

    private boolean isStarted = false;
    private boolean isInitialised = false;
    private volatile boolean isPaused = false;

    public void initialiseNotes(MusicView musicView) {
        this.isInitialised = true;
    }
    public abstract void clearNotes();

    public abstract MusicViewPlayable[] getNotesToDrawTreble();
    public abstract MusicViewPlayable[] getNotesToDrawBass();

    public abstract int getNoteCountTreble();
    public abstract int getNoteCountBass();

    public abstract int getNotesFitOnView();

    public abstract float getLastNotePosition(float defaultX);

    public abstract void updateNotes(MusicView musicView);

    public abstract boolean pushNoteTrebleToEnd(Playable note, String noteName, String annotation, MusicView musicView);
    public abstract boolean pushNoteTrebleToEnd(Playable note, String noteName, String annotation, MusicView musicView, float timeOffset);

    public abstract boolean pushNoteBassToEnd(Playable note, String noteName, String annotation, MusicView musicView);
    public abstract boolean pushNoteBassToEnd(Playable note, String noteName, String annotation, MusicView musicView, float timeOffset);

    public abstract boolean removeNoteTreble(MusicViewPlayable note);
    public abstract boolean removeNoteBass(MusicViewPlayable note);

    public boolean isStarted() { return this.isStarted; }

    public boolean isInitialised() { return this.isInitialised; }

    public void setPaused(boolean isPaused) {
        // we also use this to start the game
        if (false == isPaused && false == this.isStarted) {
            // we will soon not be paused, but we also haven't started
            this.setStarted(true);
        }
        // pause / resume
        this.isPaused = isPaused;
    }

    public void setStarted(boolean isStarted) {
        this.isStarted = isStarted;
    }

    public boolean isPaused() {
        return this.isPaused;
    }
}

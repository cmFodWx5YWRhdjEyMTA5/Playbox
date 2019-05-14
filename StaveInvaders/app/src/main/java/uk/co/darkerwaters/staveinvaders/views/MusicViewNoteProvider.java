package uk.co.darkerwaters.staveinvaders.views;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.notes.Chords;

class MusicViewNoteProvider {

    private long lastChangeTime = 0l;
    private MusicView.Clefs activeClef = MusicView.Clefs.treble;

    private final Chords notesToPlay;

    MusicViewNoteProvider(Application application) {
        this.notesToPlay = application.getSingleChords();
    }

    public MusicView.Clefs getActiveClef() {
        // return the clef that the next note should be drawn on
        return this.activeClef;
    }

    public void updateNotes(MusicView view, long timeElapsed) {
        this.lastChangeTime += timeElapsed;
        if (this.lastChangeTime > 5000l) {
            if (this.activeClef == MusicView.Clefs.treble) {
                this.activeClef = MusicView.Clefs.bass;
            }
            else {
                this.activeClef = MusicView.Clefs.treble;
            }
            this.lastChangeTime = 0l;
        }
        // invalidate the view to draw this change in notes
        view.invalidate();
    }

    public MusicView.DrawnNote[] getNotesToDraw(MusicView view) {
        // return a list of notes to draw
        return new MusicView.DrawnNote[] {
                view.new DrawnNote(this.notesToPlay.getChord("A4"), 0.5f),
                view.new DrawnNote(this.notesToPlay.getChord("B4"), 0.6f)
        };
    }
}

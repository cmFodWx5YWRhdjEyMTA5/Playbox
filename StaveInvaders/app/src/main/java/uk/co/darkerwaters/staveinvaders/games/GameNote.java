package uk.co.darkerwaters.staveinvaders.games;

import uk.co.darkerwaters.staveinvaders.notes.Chord;

public class GameNote {

    private final Game.GameEntry entry;
    private float offsetBeats;

    public GameNote(Game.GameEntry entry, float offsetBeats) {
        this.entry = entry;
        this.offsetBeats = offsetBeats;
    }

    public float getOffsetBeats() {
        return this.offsetBeats;
    }

    public Game.GameEntry getChord() {
        return this.entry;
    }

    public float adjustTime(float delta) {
        this.offsetBeats += delta;
        return this.offsetBeats;
    }
}

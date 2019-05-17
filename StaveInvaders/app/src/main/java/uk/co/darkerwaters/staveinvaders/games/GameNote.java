package uk.co.darkerwaters.staveinvaders.games;

import uk.co.darkerwaters.staveinvaders.notes.Chord;

public class GameNote {

    private final Game.GameEntry entry;
    private float seconds;

    public GameNote(Game.GameEntry entry, float seconds) {
        this.entry = entry;
        this.seconds = seconds;
    }

    public float getSeconds() {
        return this.seconds;
    }

    public Game.GameEntry getChord() {
        return this.entry;
    }

    public float adjustTime(float delta) {
        this.seconds += delta;
        return this.seconds;
    }
}

package uk.co.darkerwaters.noteinvaders.views;

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Playable;

public abstract class MusicViewPlayable {

    public final Playable playable;

    public final String name;

    public MusicViewPlayable(Playable playable, String noteName) {
        this.playable = playable;
        this.name = noteName;
    }

    @Override
    public boolean equals(Object compare) {
        if (compare == null || false == compare instanceof MusicViewPlayable) {
            // not the same
            return false;
        }
        else {
            return this.playable.equals(((MusicViewPlayable)compare).playable);
        }
    }

    public abstract float getXPosition();
}

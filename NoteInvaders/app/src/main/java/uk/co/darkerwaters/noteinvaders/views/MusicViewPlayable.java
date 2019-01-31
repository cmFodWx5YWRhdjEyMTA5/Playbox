package uk.co.darkerwaters.noteinvaders.views;

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Playable;

public abstract class MusicViewPlayable {

    public final Playable playable;

    public final String name;

    public final String annotation;

    public MusicViewPlayable(Playable playable, String noteName, String annotation) {
        this.playable = playable;
        this.name = noteName;
        this.annotation = annotation;
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

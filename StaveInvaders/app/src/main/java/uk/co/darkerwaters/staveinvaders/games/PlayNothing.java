package uk.co.darkerwaters.staveinvaders.games;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.notes.Clef;

public class PlayNothing extends GamePlayer {

    private int iNote = 0;

    public PlayNothing(Application application, Game game) {
        super(application, game);
    }

    @Override
    protected Game.GameEntry getNextNote(Clef activeClef, float seconds) {
        // simply return the next note from the list
        if (this.game.entries == null || this.game.entries.length == 0) {
            return null;
        }
        else {
            Game.GameEntry toReturn = this.game.entries[this.iNote++];
            if (this.iNote > this.game.entries.length - 1) {
                // reset
                this.iNote = 0;
            }
            return toReturn;
        }
    }
}

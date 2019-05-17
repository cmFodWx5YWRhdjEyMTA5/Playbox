package uk.co.darkerwaters.staveinvaders.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.views.MusicView;

public class PlayRandomAttack extends GamePlayer {

    private final Random random = new Random();

    public PlayRandomAttack(Application application, Game game) {
        super(application, game);
    }

    protected  Game.GameEntry getNextNote(MusicView.Clefs activeClef, float seconds) {
        // find the next random node in the list
        if (this.game.entries.length == 0) {
            return null;
        }
        // else sit in an infinite loop to find the next note
        int tryIndex = 0;
        while (++tryIndex < 100) {
            // get one from the list at random
            int index = random.nextInt(this.game.entries.length);
            if (this.game.entries[index].clef == activeClef) {
                // this is a note in the correct clef, fine
                return this.game.entries[index];
            }
            // else keep trying
        }
        // should never get here because if we do something is wrong with randomised, still
        // hate putting in infinite loops
        return this.game.entries[0];
    }
}

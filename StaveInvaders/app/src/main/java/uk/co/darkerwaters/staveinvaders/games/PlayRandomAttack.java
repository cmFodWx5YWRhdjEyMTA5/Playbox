package uk.co.darkerwaters.staveinvaders.games;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.notes.Clef;

public class PlayRandomAttack extends GamePlayer {

    private static final int K_MIN_CHANGE_NOTE = 15;
    private static final int K_RND_CHANGE_NOTE = 5;

    private int noteCounter = 0;
    private int nextNoteClefChange = K_MIN_CHANGE_NOTE;

    public PlayRandomAttack(Application application, Game game) {
        super(application, game);
    }

    protected  Game.GameEntry getNextNote(Clef activeClef, float seconds) {
        // find the next random node in the list
        if (this.game.entries.length == 0) {
            return null;
        }
        // another note returned
        ++noteCounter;
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

    @Override
    protected boolean isPerformClefChange() {
        if (this.isInDemoMode) {
            // in demo mode we just flip at the specified time, let the base do this
            return super.isPerformClefChange();
        }
        else if (this.noteCounter >= this.nextNoteClefChange) {
            // reset the change counter
            this.nextNoteClefChange = this.noteCounter + K_MIN_CHANGE_NOTE + this.random.nextInt(K_RND_CHANGE_NOTE);
            // and change the clef
            return true;
        }
        else {
            // don't
            return false;
        }
    }
}

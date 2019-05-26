package uk.co.darkerwaters.staveinvaders.games;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.notes.Clef;

public class PlayRandomAttack extends GamePlayer {

    private long nextSheduledChangeTime = 0l;

    public PlayRandomAttack(Application application, Game game) {
        super(application, game);
    }

    protected  Game.GameEntry getNextNote(Clef activeClef, float seconds) {
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

    @Override
    public boolean setPermittedClef(Clef clef, boolean isPermitted) {
        // reset the time we will change this about
        this.nextSheduledChangeTime = System.currentTimeMillis();
        // and do the change
        return super.setPermittedClef(clef, isPermitted);
    }

    @Override
    protected boolean isPerformClefChange() {
        return System.currentTimeMillis() > this.nextSheduledChangeTime;
    }

    @Override
    protected void performClefChange() {
        // do the change
        super.performClefChange();
        if (this.isInDemoMode) {
            // and schedule a new change to be fairly quick to show the user the state of play
            this.nextSheduledChangeTime = System.currentTimeMillis() + K_HELP_CHANGE_TIME;
        } else {
            // schedule a new change, allowing for the current ones to clear from the list
            this.nextSheduledChangeTime = System.currentTimeMillis() + K_MIN_CHANGE_TIME + (this.random.nextInt(K_CHANGE_TIME_BEATS_ADD) * 1000l);
        }
    }
}

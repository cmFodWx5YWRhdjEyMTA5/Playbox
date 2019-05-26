package uk.co.darkerwaters.staveinvaders.games;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.notes.Clef;

public class PlayScales extends GamePlayer {

    private static final int K_NO_SCALES = 8;

    private int noteIndex = 0;
    private int incrementer = 1;

    private int scaleCounter = 0;

    private List<Game.GameEntry>[] scales = new List[Clef.values().length];
    private boolean isChangeClef = false;

    public PlayScales(Application application, Game game) {
        super(application, game);
        // we want to seperate the notes into their clefs here to do
        // each one on their own in totality

        // get the active clef from the entries on the game, just use the first
        if (null != this.game && null != this.game.entries && this.game.entries.length > 0) {
            // there are entries in the list, start off with the first one as the default
            for (Clef clef : Clef.values()) {
                // create a list of this clef
                scales[clef.val] = new ArrayList<Game.GameEntry>(this.game.entries.length / 2);
                // get all the entries for this clef
                for (Game.GameEntry entry : this.game.entries) {
                    if (entry.clef == clef) {
                        scales[clef.val].add(entry);
                    }
                }
            }
        }
    }

    protected  Game.GameEntry getNextNote(Clef activeClef, float seconds) {
        // find the next note node in the list
        if (this.game.entries.length == 0) {
            return null;
        }
        // get the next entry
        List<Game.GameEntry> scale = this.scales[activeClef.val];
        // get the entry to return for the active clef
        Game.GameEntry entry = scale.get(this.noteIndex);
        // move the index on for next time
        this.noteIndex += this.incrementer;
        int noteCount = scale.size();
        // handle the index so we don't overflow in either direction
        if (noteIndex > noteCount - 1) {
            // oops, over the end, put it back to the end
            this.noteIndex = noteCount - 1;
            // and change to go down the list next time around
            this.incrementer = -1;
        }
        else if (this.noteIndex < 0) {
            // under the end, put it back
            this.noteIndex = 0;
            // and go upwards next time
            this.incrementer = 1;
            // this is a scale complete, inc the counter
            if (++scaleCounter % 2 == 0) {
                // every 2 scales, change clef
                this.isChangeClef = true;
            }
        }
        // and return the note
        return entry;
    }

    @Override
    protected int getPointsForHit(Game.GameEntry entry, float offsetBeats) {
        // regardless of position, scales are one point per hit
        return 1;
    }

    @Override
    public int getPointLevelGoal() {
        // we want them to do the scales at least 5 times to progress
        return this.permittedClefs.size() * this.scales[getActiveClef().val].size() * K_NO_SCALES;
    }

    @Override
    protected boolean isPerformClefChange() {
        if (this.isInDemoMode) {
            // in demo mode we just flip at the specified time, let the base do this
            return super.isPerformClefChange();
        }
        else if (this.isChangeClef) {
            // we change clef when we end each scale played
            this.isChangeClef = false;
            return true;
        }
        else {
            // don't
            return false;
        }
    }
}

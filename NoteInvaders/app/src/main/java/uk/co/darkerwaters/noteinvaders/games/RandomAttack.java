package uk.co.darkerwaters.noteinvaders.games;

import java.util.Random;

import uk.co.darkerwaters.noteinvaders.views.MusicView;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.views.MusicViewNoteProvider;

public class RandomAttack extends GamePlayer {
    private final Random random;

    public RandomAttack() {
        this.random = new Random(System.currentTimeMillis());
    }

    @Override
    public void addNewNotes(MusicView musicView, Game game) {
        //ensure the view is always full of notes to show
        MusicViewNoteProvider provider = musicView.getNoteProvider();
        int iNoAttempts = 0;
        while (provider.getNoteCountTreble() + provider.getNoteCountBass() < provider.getNotesFitOnView() + 5) {
            // add another note
            Note note = null;
            // first decide treble or bass
            if (game.isTreble() && (!game.isBass() || random.nextBoolean())) {
                // we are doing treble and either no bass or treble's turn
                note = game.treble_clef[random.nextInt(game.treble_clef.length)];
                provider.pushNoteTrebleToEnd(note, musicView);
            }
            if (note == null && game.isBass()) {
                // didn't do treble, so do bass
                note = game.bass_clef[random.nextInt(game.bass_clef.length)];
                provider.pushNoteBassToEnd(note, musicView);
            }
            if (++iNoAttempts > provider.getNotesFitOnView() * 10) {
                // stop already
                break;
            }
        }
    }
}
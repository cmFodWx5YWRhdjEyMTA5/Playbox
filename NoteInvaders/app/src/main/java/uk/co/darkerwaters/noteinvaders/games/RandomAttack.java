package uk.co.darkerwaters.noteinvaders.games;

import java.util.Random;

import uk.co.darkerwaters.noteinvaders.views.MusicView;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.views.MusicViewNoteProvider;

public class RandomAttack extends GamePlayer {
    private final Random random;

    private static final int K_NUMBERTRIESWITHOUTSUCCESS = 10;

    public RandomAttack() {
        this.random = new Random(System.currentTimeMillis());
    }

    @Override
    public void addNewNotes(MusicView musicView, Game game) {
        //ensure the view is always full of notes to show
        MusicViewNoteProvider provider = musicView.getNoteProvider();
        int iNoAttempts = 0;
        int targetNotes = provider.getNotesFitOnView() + 5;
        int noteIndex = 0;
        while (provider.getNoteCountTreble() + provider.getNoteCountBass() < targetNotes) {
            // add another note
            Note note = null;
            String noteName = "";
            // first decide treble or bass
            if (game.isTreble() && (!game.isBass() || random.nextBoolean())) {
                // we are doing treble and either no bass or treble's turn
                noteIndex = random.nextInt(game.treble_clef.length);
                // find the note to push the the provider
                note = game.treble_clef[noteIndex];
                // get the name
                if (game.treble_names != null && noteIndex < game.treble_names.length) {
                    // there is a name, use this
                    noteName = game.treble_names[noteIndex];
                }
                else {
                    // use the name of the note
                    noteName = note.getName();
                }
                provider.pushNoteTrebleToEnd(note, noteName, musicView);
            }
            if (note == null && game.isBass()) {
                // didn't do treble, so do bass
                noteIndex = random.nextInt(game.bass_clef.length);
                note = game.bass_clef[noteIndex];
                // get the name
                if (game.bass_names != null && noteIndex < game.bass_names.length) {
                    // there is a name, use this
                    noteName = game.bass_names[noteIndex];
                }
                else {
                    // use the name of the note
                    noteName = note.getName();
                }
                // find the note to push the the provider
                provider.pushNoteBassToEnd(note, noteName, musicView);
            }
            if (++iNoAttempts > provider.getNotesFitOnView() + K_NUMBERTRIESWITHOUTSUCCESS) {
                // stop already
                break;
            }
        }
    }
}
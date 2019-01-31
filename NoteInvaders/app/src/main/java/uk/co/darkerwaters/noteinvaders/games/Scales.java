package uk.co.darkerwaters.noteinvaders.games;

import java.util.Random;

import uk.co.darkerwaters.noteinvaders.state.Playable;
import uk.co.darkerwaters.noteinvaders.views.MusicView;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.views.MusicViewNoteProvider;

public class Scales extends GamePlayer  {
    private int trebleIndex = 0;
    private int bassIndex = 0;
    private int direction = 1;

    private static final int K_NUMBERTRIESWITHOUTSUCCESS = 10;

    public Scales() {

    }

    @Override
    public void addNewNotes(MusicView musicView, Game game) {
        //ensure the view is always full of notes to show
        MusicViewNoteProvider provider = musicView.getNoteProvider();
        int maxX = musicView.getWidth();
        int iNoAttempts = 0;
        while (provider.getLastNotePosition(-1f) < maxX) {
            // add the next notes
            Playable note = null;
            String noteName = "";
            // first do treble
            if (game.isTreble() && trebleIndex >= 0 && trebleIndex < game.treble_clef.length) {
                // are in the range, get the next
                note = game.treble_clef[trebleIndex];
                // get the name
                if (game.treble_names != null && trebleIndex < game.treble_names.length) {
                    // there is a name, use this
                    noteName = game.treble_names[trebleIndex];
                }
                else {
                    // use the name of the note
                    noteName = note.getName();
                }
                provider.pushNoteTrebleToEnd(note, noteName, musicView);
            }
            // now do bass
            if (game.isBass() && bassIndex >= 0 && bassIndex < game.bass_clef.length) {
                // are in the range, get the next
                note = game.bass_clef[bassIndex];
                // get the name
                if (game.bass_names != null && bassIndex < game.bass_names.length) {
                    // there is a name, use this
                    noteName = game.treble_names[bassIndex];
                }
                else {
                    // use the name of the note
                    noteName = note.getName();
                }
                provider.pushNoteBassToEnd(note, noteName, musicView);
            }
            // increment the counters
            trebleIndex += direction;
            bassIndex += direction;
            if (direction > 0) {
                // are going up, check to see if we are completed
                if (trebleIndex >= game.treble_clef.length && ++bassIndex >= game.bass_clef.length) {
                    // both counters have played all the notes in their lists, reset them both
                    trebleIndex = game.treble_clef.length - 1;
                    bassIndex = game.bass_clef.length - 1;
                    // and change direction
                    direction = -1;
                }
            }
            else {
                // are going down, check to see if we are completed
                if (trebleIndex < 0 && bassIndex < 0) {
                    // both counters have dropped off the bottom, reset them both
                    trebleIndex = 0;
                    bassIndex = 0;
                    // and change direction
                    direction = 1;
                }
            }
            if (++iNoAttempts > provider.getNotesFitOnView() + K_NUMBERTRIESWITHOUTSUCCESS) {
                // stop already
                break;
            }
        }
    }
}

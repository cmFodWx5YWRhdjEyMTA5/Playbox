package uk.co.darkerwaters.noteinvaders.state;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.selectables.Instrument;

public class State {

    private static final State INSTANCE = new State();
    public static final String K_APPTAG = "NoteInvaders";

    private Instrument instrument = null;
    private List<Instrument> instrumentList = null;

    private List<Game> games = null;
    private List<Game> gameSelected = new ArrayList<Game>();

    public static State getInstance() {
        return INSTANCE;
    }

    public void initialise(Activity context) {
        // make sure the notes are all loaded
        Notes.CreateNotes(context);
        // initialise the members of this state
        this.instrumentList = new ArrayList<Instrument>();
        this.instrumentList.add(new Instrument(context, context.getString(R.string.piano_keyboard), R.drawable.piano));
        this.instrumentList.add(new Instrument(context, context.getString(R.string.violin), R.drawable.violin));
        // first default everything to something sensible
        this.instrument = instrumentList.get(0);

        // load in all the games available too
        this.games = Game.loadGamesFromAssets(context);
    }

    public Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public Instrument getAvailableInstrument(int index) {
        return this.instrumentList.get(index);
    }

    public int getAvailableInstrumentCount() {
        return this.instrumentList.size();
    }

    public boolean isMicAvailable() {
        return true;
    }

    public boolean isUsbAvailable() {
        return false;
    }

    public boolean isBtAvailable() {
        return false;
    }

    public int getGameSelectedCount() { return this.gameSelected.size(); }

    public Game getGameSelected(int index) {
        return this.gameSelected.get(index);
    }

    public boolean selectGame(Game game) {
        return this.gameSelected.add(game);
    }

    public Game deselectGame() {
        Game selected = null;
        if (this.gameSelected.size() > 0) {
            selected = this.gameSelected.remove(this.gameSelected.size() - 1);
        }
        return selected;
    }

    public Game getGameSelectedLast() {
        Game selected = null;
        if (this.gameSelected.size() > 0) {
            selected = this.gameSelected.get(this.gameSelected.size() - 1);
        }
        return selected;
    }

    public int getAvailableGameCount() { return this.games.size(); }

    public Game getAvailableGame(int index) { return this.games.get(index); }

}

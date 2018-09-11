package uk.co.darkerwaters.noteinvaders.state;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.selectables.GameCard;
import uk.co.darkerwaters.noteinvaders.selectables.Instrument;

public class State {

    private static final State INSTANCE = new State();
    public static final String K_APPTAG = "NoteInvaders";

    private Instrument instrument = null;
    private List<Instrument> instrumentList = null;

    private Game game = null;
    private List<Game> games = null;

    private Game.GameLevel gameLevel = null;

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

    public Game getGame() {
        return this.game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game.GameLevel getGameLevel() {
        return this.gameLevel;
    }

    public void setGameLevel(Game.GameLevel gameLevel) {
        this.gameLevel = gameLevel;
    }

    public int getAvailableGameCount() { return this.games.size(); }

    public Game getAvailableGame(int index) { return this.games.get(index); }

}

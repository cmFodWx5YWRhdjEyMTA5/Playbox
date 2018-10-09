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

    private ActiveScore currentActiveScore = null;

    public interface InputChangeListener {
        void onInputTypeChanged(InputType type);
    }

    private InputType selectedInput = InputType.keyboard;

    public enum InputType {
        keyboard,
        microphone,
        usb,
        bt
    }

    private List<InputChangeListener> inputChangeListeners;

    public static State getInstance() {
        return INSTANCE;
    }

    public void initialise(Activity context) {
        // make sure the notes are all loaded
        Notes.CreateNotes(context);
        // initialise the members of this state
        this.instrumentList = new ArrayList<Instrument>();
        this.instrumentList.add(new Instrument(context, context.getString(R.string.piano_keyboard), new NoteRange("A0", "C8"), R.drawable.piano));
        this.instrumentList.add(new Instrument(context, context.getString(R.string.violin), new NoteRange("G3", "E7"), R.drawable.violin));
        // first default everything to something sensible
        this.instrument = instrumentList.get(0);

        // load in all the games available too
        this.games = Game.loadGamesFromAssets(context);
        this.currentActiveScore = new ActiveScore();

        this.inputChangeListeners = new ArrayList<InputChangeListener>();
    }

    public boolean addListener(InputChangeListener listener) {
        synchronized (this.inputChangeListeners) {
            return this.inputChangeListeners.add(listener);
        }
    }

    public boolean removeListener(InputChangeListener listener) {
        synchronized (this.inputChangeListeners) {
            return this.inputChangeListeners.remove(listener);
        }
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

    public boolean isInputAvailable(InputType inputState) {
        switch (inputState) {
            case keyboard:
                return true;
            case microphone:
                return true;
            case usb:
                return false;
            case bt:
                return false;
            default:
                return false;
        }
    }

    public InputType getSelectedInput() { return this.selectedInput; }

    public void setSelectedInput(InputType type) {
        this.selectedInput = type;
        synchronized (this.inputChangeListeners) {
            for (InputChangeListener listener : this.inputChangeListeners) {
                listener.onInputTypeChanged(type);
            }
        }
    }

    public ActiveScore getCurrentActiveScore() {
        return this.currentActiveScore;
    }

    public int getGameSelectedCount() { return this.gameSelected.size(); }

    public Game getGameSelected(int index) {
        return this.gameSelected.get(index);
    }

    public boolean selectGame(Game game) {
        return this.gameSelected.add(game);
    }

    public int deselectGame(Game toDeselect) {
        int noRemoved = 0;
        while (this.gameSelected.size() > 0) {
            Game removed = this.gameSelected.remove(this.gameSelected.size() - 1);
            ++noRemoved;
            if (removed == toDeselect) {
                // this is the right one removed
                break;
            }
        }
        return noRemoved;
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

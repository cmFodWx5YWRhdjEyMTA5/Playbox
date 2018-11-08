package uk.co.darkerwaters.noteinvaders.state;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    private Map<String, Integer> gameTempoMap = null;
    private Map<String, Date> gamePlayedLastMap = null;
    private Map<String, Boolean> gameHelpMap = null;

    public static SimpleDateFormat K_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private boolean isSoundOn = true;
    private String midiDeviceId = "";

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

        // load in all the scores that were stored also
        this.gameTempoMap = new HashMap<String, Integer>();
        this.gamePlayedLastMap = new HashMap<String, Date>();
        this.gameHelpMap = new HashMap<String, Boolean>();
        loadState(context);

        this.inputChangeListeners = new ArrayList<InputChangeListener>();
    }

    private void loadState(Activity context) {
        // Store values between instances here
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        for (Game game : this.games) {
            loadGameScore(game, preferences);
        }
        this.isSoundOn = preferences.getBoolean("is_sound_on", this.isSoundOn);
        this.midiDeviceId = preferences.getString("midi_device_id", this.midiDeviceId);
    }

    private void loadGameScore(Game game, SharedPreferences preferences) {
        int gameTempo = preferences.getInt("top_tempo_" + game.id, -1);
        if (gameTempo > 0) {
            // put this top tempo from the last time into the map
            this.gameTempoMap.put(game.id, gameTempo);
        }
        int gameHelp = preferences.getInt("help_" + game.id, -1);
        if (gameHelp > 0) {
            // put this help state in the map to remember
            this.gameHelpMap.put(game.id, gameHelp != 0);
        }
        String gameDateString = preferences.getString("game_played_" + game.id, "");
        if (null != gameDateString && false == gameDateString.isEmpty()) {
            // there is a string, convert to a date and put in the map
            try {
                Date gameDate = K_DATEFORMAT.parse(gameDateString);
                this.gamePlayedLastMap.put(game.id, gameDate);
            }
            catch (Exception e) {
                // oops
                e.printStackTrace();
            }
        }
        // just recerse, should be fine - not that many games todo
        for (Game child : game.children) {
            loadGameScore(child, preferences);
        }
    }

    public Boolean getGameHelpState(Game game) {
        // return if the help was on for this game
        return this.gameHelpMap.get(game);
    }

    public int getGameTopTempo(Game game) {
        Integer topTempo = this.gameTempoMap.get(game.id);
        if (topTempo == null) {
            if (game.children.length > 0) {
                // there is no tempo for this, can we average the children?
                int avgTempo = 0;
                for (Game child : game.children) {
                    avgTempo += getGameTopTempo(child);
                }
                avgTempo /= game.children.length;
                // and return this
                return avgTempo;
            }
            else {
                // no children, no tempo
                return 0;
            }
        }
        else {
            // return this retrieved value
            return topTempo.intValue();
        }
    }

    public void storeScore(Activity context, ActiveScore score) {
        // store the top score for the current active level
        Game currentGame = getGameSelectedLast();
        if (null != currentGame && null != score) {
            // if this tempo is better, store it now
            int topTempo = getGameTopTempo(currentGame);
            if (score.getTopBpm() > topTempo) {
                // there isn't a score, or the current score is more, store this
                topTempo = score.getTopBpm();
                boolean isHelpOn = score.isHelpOn();
                this.gameTempoMap.put(currentGame.id, topTempo);
                this.gameHelpMap.put(currentGame.id, isHelpOn);
                // and put it in the preferences for later
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("top_tempo_" + currentGame.id, topTempo);
                editor.putInt("help_" + currentGame.id, isHelpOn ? 1 : 0);
                // and commit to storage this value
                editor.commit();
            }
        }
    }

    public Date getTimeGameLastPlayed(Game game) {
        return this.gamePlayedLastMap.get(game.id);
    }

    public String getTimeGameLastPlayedStr(Activity context, Game game) {
        Date lastPlayed = getTimeGameLastPlayed(game);
        if (null == lastPlayed) {
            return context.getResources().getString(R.string.never);
        }
        else {
            Date now = new Date();
            long seconds=TimeUnit.MILLISECONDS.toSeconds(now.getTime() - lastPlayed.getTime());
            long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - lastPlayed.getTime());
            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - lastPlayed.getTime());

            if (seconds < 60) {
                return Long.toString(seconds) + " " + context.getResources().getString(R.string.seconds_ago);
            }
            else if (minutes < 60) {
                return Long.toString(minutes) + " " + context.getResources().getString(R.string.minutes_ago);
            }
            else if (hours < 24) {
                return Long.toString(hours) + " " + context.getResources().getString(R.string.hours_ago);
            }
            else {
                long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - lastPlayed.getTime());
                return Long.toString(days) + " " + context.getResources().getString(R.string.days_ago);
            }
        }
    }

    public Game getGamePlayedLast() {
        Date latestDate = null;
        String latestGameId = null;
        for (Map.Entry<String, Date> entry : this.gamePlayedLastMap.entrySet()) {
            if (latestDate == null || entry.getValue().after(latestDate)) {
                // this is later
                latestDate = entry.getValue();
                latestGameId = entry.getKey();
            }
        }
        if (null != latestDate) {
            // have the latest game as an ID, find it as an actual game
            return findGame(latestGameId);
        }
        else {
             // none
            return null;
        }
    }

    public Game findGame(String gameId) {
        for (Game game : this.games) {
            if (game.id.equals(gameId)) {
                // this is it
                return game;
            }
            Game matchingGame = findGameInChildren(game, gameId);
            if (null != matchingGame) {
                return matchingGame;
            }
        }
        // if here then there is no match
        return null;
    }

    private Game findGameInChildren(Game parent, String gameId) {
        for (Game child : parent.children) {
            if (child.id.equals(gameId)) {
                // this is it
                return child;
            }
            else {
                // try the children
                Game matchingChild = findGameInChildren(child, gameId);
                if (null != matchingChild) {
                    return matchingChild;
                }
            }
        }
        // if here, no match
        return null;
    }

    public void startGame(Activity context) {
        // start the game currently selected
        Game currentGame = getGameSelectedLast();
        if (null != currentGame) {
            // nothing much to do except store that this
            // game was played now
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            // put the string of this data in our preferences
            Date datePlayed = new Date();
            editor.putString("game_played_" + currentGame.id, K_DATEFORMAT.format(datePlayed));
            // and commit to storage this value
            editor.commit();
            // update the map too
            this.gamePlayedLastMap.put(currentGame.id, datePlayed);
        }
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

    public boolean isSoundOn() {
        return this.isSoundOn;
    }

    public void setIsSoundOn(Activity context, boolean soundOn) {
        this.isSoundOn = soundOn;
        // store this
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_sound_on", this.isSoundOn);
        // and commit to storage this value
        editor.commit();
    }

    public String getMidiDeviceId() {
        return this.midiDeviceId;
    }

    public void setMidiDeviceId(Activity context, String midiDeviceId) {
        this.midiDeviceId = midiDeviceId;
        // store this
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("midi_device_id", this.midiDeviceId);
        // and commit to storage this value
        editor.commit();
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

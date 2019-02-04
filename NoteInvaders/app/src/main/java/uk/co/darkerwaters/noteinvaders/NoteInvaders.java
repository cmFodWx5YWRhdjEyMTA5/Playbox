package uk.co.darkerwaters.noteinvaders;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import uk.co.darkerwaters.noteinvaders.selectables.Instrument;
import uk.co.darkerwaters.noteinvaders.state.ActiveScore;
import uk.co.darkerwaters.noteinvaders.state.Game;
import uk.co.darkerwaters.noteinvaders.state.NoteRange;
import uk.co.darkerwaters.noteinvaders.state.Notes;

public class NoteInvaders extends Application {

    public static final String K_APPTAG = "NoteInvaders";

    private Instrument instrument = null;
    private List<Instrument> instrumentList = null;

    private List<Game> games = null;
    private Game gameSelected = null;

    private ActiveScore currentActiveScore = null;

    private Notes notes = null;

    private static NoteInvaders context;

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
        letters,
        microphone,
        usb,
        bt
    }

    private List<InputChangeListener> inputChangeListeners;

    public static Notes getNotes() {
        NoteInvaders app = getAppContext();
        if (app.notes == null) {
            app.notes = Notes.CreateNotes(app);
        }
        return app.notes;
    }

    public static NoteInvaders getAppContext() {
        return NoteInvaders.context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // remember ourselves
        NoteInvaders.context = (NoteInvaders) getApplicationContext();
        // make sure the notes are all loaded
        getNotes();
        // initialise the members of this state
        this.instrumentList = new ArrayList<Instrument>();
        this.instrumentList.add(new Instrument(this, getString(R.string.piano_keyboard), new NoteRange("A0", "C8"), R.drawable.piano));
        //this.instrumentList.add(new Instrument(this, getString(R.string.violin), new NoteRange("G3", "E7"), R.drawable.violin));
        // first default everything to something sensible
        this.instrument = instrumentList.get(0);

        // load in all the games available too
        this.games = Game.loadGamesFromAssets(this);
        this.currentActiveScore = new ActiveScore();

        // load in all the scores that were stored also
        this.gameTempoMap = new HashMap<String, Integer>();
        this.gamePlayedLastMap = new HashMap<String, Date>();
        this.gameHelpMap = new HashMap<String, Boolean>();

        // load the previous state that was stored
        loadState();

        this.inputChangeListeners = new ArrayList<InputChangeListener>();
    }

    private void loadState() {
        // Store values between instances here
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        for (Game game : this.games) {
            loadGameScore(game, preferences);
        }
        this.isSoundOn = preferences.getBoolean("is_sound_on", this.isSoundOn);
        this.midiDeviceId = preferences.getString("midi_device_id", this.midiDeviceId);
        this.selectedInput = InputType.valueOf(preferences.getString("input_type", this.selectedInput.toString()));

        // and the game list
        String gameId = preferences.getString("game_selected", "");
        if (null != gameId && false == gameId.isEmpty()) {
            this.gameSelected = findGame(gameId);
        }
        else {
            this.gameSelected = null;
        }
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

    public void storeScore(ActiveScore score) {
        // store the top score for the current active level
        Game currentGame = getGameSelected();
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
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
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

    public String getTimeGameLastPlayedStr(Game game) {
        Date lastPlayed = getTimeGameLastPlayed(game);
        if (null == lastPlayed) {
            return getResources().getString(R.string.never);
        }
        else {
            Date now = new Date();
            long seconds=TimeUnit.MILLISECONDS.toSeconds(now.getTime() - lastPlayed.getTime());
            long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - lastPlayed.getTime());
            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - lastPlayed.getTime());

            if (seconds < 60) {
                return Long.toString(seconds) + " " + getResources().getString(R.string.seconds_ago);
            }
            else if (minutes < 60) {
                return Long.toString(minutes) + " " + getResources().getString(R.string.minutes_ago);
            }
            else if (hours < 24) {
                return Long.toString(hours) + " " + getResources().getString(R.string.hours_ago);
            }
            else {
                long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - lastPlayed.getTime());
                return Long.toString(days) + " " + getResources().getString(R.string.days_ago);
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

    public Game getNextGame() {
        Game currentGame = getGameSelected();
        Game nextGame = null;
        if (null != currentGame && null != currentGame.parent) {
            // get the sibling of the current game
            Game[] siblings = currentGame.parent.children;
            if (null != siblings) {
                int foundIndex = -1;
                for (int i = 0; i < siblings.length; ++i) {
                    if (siblings[i] == currentGame) {
                        // this is the current game
                        foundIndex = i;
                        break;
                    }
                }
                if (foundIndex > -1 && foundIndex + 1 < siblings.length) {
                    // there is a later game
                    nextGame = siblings[foundIndex + 1];
                }
            }
        }
        // return the next game if there is one
        return nextGame;
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

    public void startGame() {
        // start the game currently selected
        Game currentGame = getGameSelected();
        if (null != currentGame) {
            // nothing much to do except store that this
            // game was played now
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
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

    public void setIsSoundOn(boolean soundOn) {
        this.isSoundOn = soundOn;
        // store this
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_sound_on", this.isSoundOn);
        // and commit to storage this value
        editor.commit();
    }

    public String getMidiDeviceId() {
        return this.midiDeviceId;
    }

    public void setMidiDeviceId(String midiDeviceId) {
        this.midiDeviceId = midiDeviceId;
        // store this
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
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
            case letters:
                return true;
            case microphone:
                return false;
            case usb:
                return true;
            case bt:
                return true;
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
        // store this
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("input_type", this.selectedInput.toString());
        // and commit to storage this value
        editor.commit();
    }

    public ActiveScore getCurrentActiveScore() {
        return this.currentActiveScore;
    }

    public void selectGame(Game game) {
        // set this game
        this.gameSelected = game;
        // and store this
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("game_selected", this.gameSelected == null ? "" : this.gameSelected.id);
        // and commit to storage this value
        editor.commit();
    }

    public Game getGameSelected() {
        return this.gameSelected;
    }

    public int getAvailableGameCount() { return this.games.size(); }

    public Game getAvailableGame(int index) { return this.games.get(index); }
}

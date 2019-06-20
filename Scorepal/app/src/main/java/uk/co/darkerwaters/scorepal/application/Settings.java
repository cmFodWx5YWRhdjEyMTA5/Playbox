package uk.co.darkerwaters.scorepal.application;

import android.content.SharedPreferences;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class Settings {
    private final SharedPreferences preferences;
    private final Application application;
    private final SharedPreferences.Editor editor;

    private final String K_ISLOGGING = "isLogging";
    private final String K_ISSHOWMENURIGHT = "isShowMenuRight";
    private final String K_ISMUTED = "isMuted";
    private final String K_ISASKFORCONTACTS = "isAskForContacts";
    private final String K_ISDOUBLES = "isDoubles";
    private final String K_SETS = "sets";
    private final String K_ISSPEAKING = "isSpeaking";
    private final String K_ISSPEAKINGMESSAGES = "isSpeakingMessages";
    private final String K_PLAYERNAME = "playerName";

    private final String K_ACTIVESPORT = "activeSport";

    private final String K_DEFAULTACTIVESPORT = "Tennis";

    private final String[] availableSports = new String[] {K_DEFAULTACTIVESPORT};

    // the settings - important for defaults
    private boolean isDoubles;
    private TennisSets sets;
    private boolean isLogging;
    private boolean isMuted;
    private boolean isAskForContacts;
    private boolean isSpeaking;
    private boolean isSpeakingMessages;
    private String activeSport;

    public Settings(Application app) {
        // get all the variables
        this.application = app;
        this.preferences = this.application.getSharedPreferences("MainPref", 0); // 0 - for private mode
        this.editor = this.preferences.edit();

        // set the defaults
        setDefaults();
        // initialise all the settings
        Log.debug("Settings initialised...");
    }

    private void setDefaults() {
        // set all our defaults here so we can clear some other time easily
        this.isLogging = true;
        this.isMuted = false;
        this.isAskForContacts = true;
        this.activeSport = K_DEFAULTACTIVESPORT;
        this.isDoubles = false;
        this.isSpeaking = true;
        this.isSpeakingMessages = true;
        this.sets = TennisSets.THREE;
    }

    public void wipeAllSettings() {
        this.editor.clear().commit();
        setDefaults();
    }

    public boolean isLogging() {
        this.isLogging = this.preferences.getBoolean(K_ISLOGGING, this.isLogging);
        return this.isLogging;
    }

    public Settings setIsLogging(boolean isLogging) {
        this.isLogging = isLogging;
        this.editor.putBoolean(K_ISLOGGING, this.isLogging);
        return this;
    }

    public boolean getShowMenuRight() {
        return this.preferences.getBoolean(K_ISSHOWMENURIGHT, false);
    }

    public boolean getIsMuted() {
        this.isMuted = this.preferences.getBoolean(K_ISMUTED, this.isMuted);
        return this.isMuted;
    }

    public boolean setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        this.editor.putBoolean(K_ISMUTED, this.isMuted);
        return this.editor.commit();
    }

    public boolean getIsSpeakingPoints() {
        this.isSpeaking = this.preferences.getBoolean(K_ISSPEAKING, this.isSpeaking);
        return this.isSpeaking;
    }

    public boolean setIsSpeakingPoints(boolean isSpeaking) {
        this.isSpeaking = isSpeaking;
        this.editor.putBoolean(K_ISSPEAKING, this.isSpeaking);
        return this.editor.commit();
    }

    public boolean getIsSpeakingMessages() {
        this.isSpeakingMessages = this.preferences.getBoolean(K_ISSPEAKINGMESSAGES, this.isSpeakingMessages);
        return this.isSpeakingMessages;
    }

    public boolean setIsSpeakingMessages(boolean isSpeakingMessages) {
        this.isSpeakingMessages = isSpeakingMessages;
        this.editor.putBoolean(K_ISSPEAKINGMESSAGES, this.isSpeakingMessages);
        return this.editor.commit();
    }

    public String getActiveSport() {
        this.activeSport = this.preferences.getString(K_ACTIVESPORT, this.activeSport);
        return this.activeSport;
    }

    public boolean setActiveSport(String activeSport) {
        this.activeSport = activeSport;
        this.editor.putString(K_ACTIVESPORT, this.activeSport);
        return this.editor.commit();
    }

    public boolean getIsRequestContactsPermission() {
        this.isAskForContacts = this.preferences.getBoolean(K_ISASKFORCONTACTS, this.isAskForContacts);
        return this.isAskForContacts;
    }

    public boolean setIsRequestContactsPermission(boolean isAskForContacts) {
        this.isAskForContacts = isAskForContacts;
        this.editor.putBoolean(K_ISASKFORCONTACTS, this.isAskForContacts);
        return this.editor.commit();
    }

    public TennisSets getTennisSets() {
        int setsValue = this.preferences.getInt(K_SETS, this.sets.val);
        // set the member from this value
        this.sets = TennisSets.fromValue(setsValue);
        return this.sets;
    }

    public boolean setTennisSets(TennisSets sets) {
        this.sets = sets;
        this.editor.putInt(K_SETS, this.sets.val);
        return this.editor.commit();
    }

    public boolean getIsDoubles() {
        this.isDoubles = this.preferences.getBoolean(K_ISDOUBLES, this.isDoubles);
        return this.isDoubles;
    }

    public boolean setIsDoubles(boolean isDoubles) {
        this.isDoubles = isDoubles;
        this.editor.putBoolean(K_ISDOUBLES, this.isDoubles);
        return this.editor.commit();
    }

    public String getPlayerName(int teamIndex, int playerIndex, String defaultName) {
        return this.preferences.getString(K_PLAYERNAME + "-" + teamIndex + "-" + playerIndex, defaultName);
    }

    public boolean setPlayerName(String name, int teamIndex, int playerIndex) {
        this.editor.putString(K_PLAYERNAME + "-" + teamIndex + "-" + playerIndex, name);
        return this.editor.commit();
    }
}

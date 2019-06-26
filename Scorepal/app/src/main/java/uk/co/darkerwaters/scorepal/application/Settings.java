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
    private final String K_ISASKFORFILEACCESS = "isAskForFileAccess";
    private final String K_ISDOUBLES = "isDoubles";
    private final String K_SETS = "sets";
    private final String K_ISSOUNDS = "isSounds";
    private final String K_ISSPEAKING = "isSpeaking";
    private final String K_ISSPEAKINGMESSAGES = "isSpeakingMessages";
    private final String K_PLAYERNAME = "playerName";
    private final String K_FINALSETTIETARGET = "finalSetTieTarget";
    private final String K_ISDECIDERONDEUCE = "deciderOnDeuce";

    private final String K_ISCONTROLTAP = "isControlTap";
    private final String K_ISCONTROLBT = "isControlBt";

    private final String K_ACTIVESPORT = "activeSport";

    private final String K_DEFAULTACTIVESPORT = "Tennis";

    private final String[] availableSports = new String[] {K_DEFAULTACTIVESPORT};

    // the settings - important for defaults
    private boolean isDoubles;
    private TennisSets sets;
    private boolean isLogging;
    private boolean isMuted;
    private boolean isAskForContacts;
    private boolean isAskForFileAccess;
    private boolean isSounds;
    private boolean isSpeaking;
    private boolean isSpeakingMessages;
    private boolean isControlTap;
    private boolean isControlBt;
    private int finalSetTieTarget;
    private boolean isDeciderOnDeuce;
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
        this.isAskForFileAccess = true;
        this.activeSport = K_DEFAULTACTIVESPORT;
        this.isDoubles = false;
        this.isSounds = true;
        this.isSpeaking = true;
        this.isControlBt = false;
        this.isControlTap = true;
        this.isSpeakingMessages = true;
        this.finalSetTieTarget = -1;
        this.isDeciderOnDeuce = false;
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

    public boolean getIsControlBt() {
        this.isControlBt = this.preferences.getBoolean(K_ISCONTROLBT, this.isControlBt);
        return this.isControlBt;
    }

    public boolean setIsControlBt(boolean isControlBt) {
        this.isControlBt = isControlBt;
        this.editor.putBoolean(K_ISCONTROLBT, this.isControlBt);
        return this.editor.commit();
    }

    public boolean getIsControlTap() {
        this.isControlTap = this.preferences.getBoolean(K_ISCONTROLTAP, this.isControlTap);
        return this.isControlTap;
    }

    public boolean setIsControlTap(boolean isControlTap) {
        this.isControlTap = isControlTap;
        this.editor.putBoolean(K_ISCONTROLTAP, this.isControlTap);
        return this.editor.commit();
    }

    public boolean getIsMakingSounds() {
        this.isSounds = this.preferences.getBoolean(K_ISSOUNDS, this.isSounds);
        return this.isSounds;
    }

    public boolean setIsMakingSounds(boolean isSounds) {
        this.isSounds = isSounds;
        this.editor.putBoolean(K_ISSOUNDS, this.isSounds);
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

    public boolean setIsDecidingPointOnDeuce(boolean isDeciderOnDeuce) {
        this.isDeciderOnDeuce = isDeciderOnDeuce;
        this.editor.putBoolean(K_ISDECIDERONDEUCE, this.isDeciderOnDeuce);
        return this.editor.commit();
    }

    public boolean getIsDecidingPointOnDeuce() {
        this.isDeciderOnDeuce = this.preferences.getBoolean(K_ISDECIDERONDEUCE, this.isDeciderOnDeuce);
        return this.isDeciderOnDeuce;
    }

    public boolean setFinalSetTieTarget(int finalSetTieTarget) {
        this.finalSetTieTarget = finalSetTieTarget;
        this.editor.putInt(K_FINALSETTIETARGET, this.finalSetTieTarget);
        return this.editor.commit();
    }

    public int getFinalSetTieTarget() {
        this.finalSetTieTarget = this.preferences.getInt(K_FINALSETTIETARGET, this.finalSetTieTarget);
        return this.finalSetTieTarget;
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

    public boolean getIsRequestFileAccessPermission() {
        this.isAskForFileAccess = this.preferences.getBoolean(K_ISASKFORFILEACCESS, this.isAskForFileAccess);
        return this.isAskForFileAccess;
    }

    public boolean setIsRequestFileAccessPermission(boolean isAskForFileAccess) {
        this.isAskForFileAccess = isAskForFileAccess;
        this.editor.putBoolean(K_ISASKFORFILEACCESS, this.isAskForFileAccess);
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

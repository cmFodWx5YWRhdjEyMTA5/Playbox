package uk.co.darkerwaters.scorepal.application;

import android.content.SharedPreferences;

import uk.co.darkerwaters.scorepal.Application;

public class Settings {
    private final SharedPreferences preferences;
    private final Application application;
    private final SharedPreferences.Editor editor;

    private final String K_ISLOGGING = "isLogging";
    private final String K_ISSHOWMENURIGHT = "isShowMenuRight";
    private final String K_ISMUTED = "isMuted";
    private final String K_ISASKFORCONTACTS = "isAskForContacts";

    private final String K_ACTIVESPORT = "activeSport";

    private final String K_DEFAULTACTIVESPORT = "Tennis";
    private final String[] availableSports = new String[] {K_DEFAULTACTIVESPORT};

    // the settings - important for defaults
    private boolean isLogging;
    private boolean isMuted;
    private boolean isAskForContacts;
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
}

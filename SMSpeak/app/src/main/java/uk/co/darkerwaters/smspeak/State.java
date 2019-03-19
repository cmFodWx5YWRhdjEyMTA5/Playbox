package uk.co.darkerwaters.smspeak;

import android.content.Context;
import android.content.SharedPreferences;

public class State {

    public static final String PREFS_NAME = "MyApp_Settings";
    private final SharedPreferences preferences;

    private boolean isTalkTime = true;
    private boolean isTalkIntro = true;
    private boolean isTalkContact = true;
    private boolean isTalkMessage = true;

    private String intro = null;
    private final String defaultIntro;

    private boolean isTalkEverything = false;
    private boolean isTalkHeadphones = false;
    private boolean isTalkHeadset = false;

    private static final Object MUTEX = new Object();
    private static State INSTANCE = null;

    public static State GetInstance(Context context) {
        synchronized (MUTEX) {
            if (null == INSTANCE) {
                INSTANCE = new State(context);
            }
            return INSTANCE;
        }
    }

    private State(Context context) {
        // private constructor to create singleton
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.defaultIntro = context.getResources().getString(R.string.intro_text_default);
        this.intro = this.defaultIntro;
    }

    public boolean isTalkTime() {
        synchronized (this.preferences) {
            return this.preferences.getBoolean("isTalkTime", this.isTalkTime);
        }
    }

    public boolean isTalkIntro() {
        synchronized (this.preferences) {
            return this.preferences.getBoolean("isTalkIntro", this.isTalkIntro);
        }
    }

    public boolean isTalkContact() {
        synchronized (this.preferences) {
            return this.preferences.getBoolean("isTalkContact", this.isTalkContact);
        }
    }

    public boolean isTalkMessage() {
        synchronized (this.preferences) {
            return this.preferences.getBoolean("isTalkMessage", this.isTalkMessage);
        }
    }

    public String getIntro() {
        synchronized (this.preferences) {
            return this.preferences.getString("intro", this.intro);
        }
    }

    public void setIsTalkTime(boolean isTalkTime) {
        synchronized (this.preferences) {
            this.isTalkTime = isTalkTime;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isTalkTime", this.isTalkTime);
            editor.commit();
        }
    }

    public void setIsTalkIntro(boolean isTalkIntro) {
        synchronized (this.preferences) {
            this.isTalkIntro = isTalkIntro;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isTalkIntro", this.isTalkIntro);
            editor.commit();
        }
    }

    public void setIsTalkContact(boolean isTalkContact) {
        synchronized (this.preferences) {
            this.isTalkContact = isTalkContact;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isTalkContact", this.isTalkContact);
            editor.commit();
        }
    }

    public void setIsTalkMessage(boolean isTalkMessage) {
        synchronized (this.preferences) {
            this.isTalkMessage = isTalkMessage;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isTalkMessage", this.isTalkMessage);
            editor.commit();
        }
    }

    public void setIntro(String intro) {
        if (intro == null || intro.isEmpty()) {
            // don't allow this
            intro = this.defaultIntro;
        }
        synchronized (this.preferences) {
            this.intro = intro;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("intro", this.intro);
            editor.commit();
        }
    }

}

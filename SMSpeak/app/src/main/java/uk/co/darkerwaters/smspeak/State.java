package uk.co.darkerwaters.smspeak;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;

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
    private boolean isTalkHeadphones = true;
    private boolean isTalkHeadset = true;

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

    public boolean isTalkActive(Context context) {
        // are we supposed to talk
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        // without registering a filter we can determine the state one would have discovered
        Intent iStatus = context.registerReceiver(null, iFilter);
        // from this we can get our state of headphones
        int headSetState = iStatus.getIntExtra("state", 0);      //get the headset state property
        int hasMicrophone = iStatus.getIntExtra("microphone", 0);//get the headset microphone property
        // check our current settings
        boolean isTalkActive = false;
        if (this.isTalkHeadphones && headSetState == 1 && hasMicrophone == 0) {
            // this is headphones connected
            isTalkActive = true;
        }
        else if (this.isTalkHeadset && headSetState == 1 && hasMicrophone == 1) {
            // this is headset connected (have microphone)
            isTalkActive = true;
        }
        return isTalkActive;
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

    public boolean isTalkHeadphones() {
        synchronized (this.preferences) {
            return this.preferences.getBoolean("isTalkHeadphones", this.isTalkHeadphones);
        }
    }

    public boolean isTalkHeadset() {
        synchronized (this.preferences) {
            return this.preferences.getBoolean("isTalkHeadset", this.isTalkHeadset);
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

    public void setIsTalkHeadphones(boolean isTalkHeadphones) {
        synchronized (this.preferences) {
            this.isTalkHeadphones = isTalkHeadphones;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isTalkHeadphones", this.isTalkHeadphones);
            editor.commit();
        }
    }

    public void setIsTalkHeadset(boolean isTalkHeadset) {
        synchronized (this.preferences) {
            this.isTalkHeadset = isTalkHeadset;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isTalkHeadset", this.isTalkHeadset);
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

package uk.co.darkerwaters.staveinvaders.application;

import android.content.SharedPreferences;

import uk.co.darkerwaters.staveinvaders.Application;

public class Settings {
    private final SharedPreferences preferences;
    private final Application application;
    private final SharedPreferences.Editor editor;

    private final String K_ISLOGGING = "isLogging";

    private final String K_INPUTKEYS = "input_keyboard";
    private final String K_INPUTMIC = "input_mic";
    private final String K_INPUTBT = "input_bt";
    private final String K_INPUTUSB = "input_usb";

    private final String K_ACTIVEINPUT = "input_active";

    // the settings - important for defaults
    private boolean isLogging = true;

    private boolean isInputKeys = true;
    private boolean isInputMic = true;
    private boolean isInputBt = true;
    private boolean isInputUsb = true;

    private String activeInput = K_INPUTKEYS;

    public enum InputType {
        keys,
        mic,
        bt,
        usb
    };

    public Settings(Application app) {
        // get all the variables
        this.application = app;
        this.preferences = this.application.getSharedPreferences("MainPref", 0); // 0 - for private mode
        this.editor = this.preferences.edit();

        // initialise all the settings
        Log.debug("Settings initialised...");
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

    public InputType getActiveInput() {
        InputType typeToReturn = InputType.keys;
        this.activeInput = this.preferences.getString(K_ACTIVEINPUT, this.activeInput);
        switch (this.activeInput) {
            case K_INPUTMIC:
                typeToReturn = InputType.mic;
                break;
            case K_INPUTBT:
                typeToReturn = InputType.bt;
                break;
            case K_INPUTUSB:
                typeToReturn = InputType.usb;
                break;
            case K_INPUTKEYS:
            default:
                typeToReturn = InputType.keys;
                break;

        }
        return typeToReturn;
    }

    public Settings setActiveInput(InputType activeInput) {
        switch (activeInput) {
            case mic:
                this.activeInput = K_INPUTMIC;
                break;
            case bt:
                this.activeInput = K_INPUTBT;
                break;
            case usb:
                this.activeInput = K_INPUTUSB;
            case keys:
            default:
                this.activeInput = K_INPUTKEYS;
        }
        this.editor.putString(K_ACTIVEINPUT, this.activeInput);
        return this;
    }

    public boolean getIsInputKeys() {
        this.isInputKeys = this.preferences.getBoolean(K_INPUTKEYS, this.isInputKeys);
        return this.isInputKeys;
    }

    public boolean getIsInputMic() {
        this.isInputMic = this.preferences.getBoolean(K_INPUTMIC, this.isInputMic);
        return this.isInputMic;
    }

    public boolean getIsInputBt() {
        this.isInputBt = this.preferences.getBoolean(K_INPUTBT, this.isInputBt);
        return this.isInputBt;
    }

    public boolean getIsInputUsb() {
        this.isInputUsb = this.preferences.getBoolean(K_INPUTUSB, this.isInputUsb);
        return this.isInputUsb;
    }

    public void commitChanges() {
        // commit the changes that were set
        this.editor.commit();
    }
}

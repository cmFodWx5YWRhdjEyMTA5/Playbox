package uk.co.darkerwaters.staveinvaders.application;

import android.content.SharedPreferences;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.notes.Clef;

public class Settings {
    private final SharedPreferences preferences;
    private final Application application;
    private final SharedPreferences.Editor editor;

    private final String K_ISLOGGING = "isLogging";
    private final String K_ISSHOWMENURIGHT = "isShowMenuRight";

    private final String K_INPUTKEYS = "input_keyboard";
    private final String K_INPUTMIC = "input_mic";
    private final String K_INPUTBT = "input_bt";
    private final String K_INPUTUSB = "input_usb";

    private final String K_ACTIVEINPUT = "input_active";

    private final String K_LASTCONNECTEDUSBDEVICE = "last_connected_device_usb";
    private final String K_LASTCONNECTEDBTDEVICE = "last_connected_device_bt";

    private final String K_ISHIDEBASS = "isHideBass";
    private final String K_ISHIDETREBLE = "isHideTreble";
    private final String K_SELECTEDCLEF = "selectedClef";
    private final String K_ISMUTED = "isMuted";
    private final String K_ISKEYINPUTPIANO = "isKeyInputPiano";
    private final String K_ISSHOWPIANOLETTERS = "isShowPianoLetters";

    // the settings - important for defaults
    private boolean isLogging;

    private String activeInput;

    private String lastConnectedUsbDevice;
    private String lastConnectedBtDevice;

    private boolean isHideTreble;
    private boolean isHideBass;
    private boolean isKeyInputPiano;
    private boolean isShowPianoLetters;
    private boolean isMuted;

    private String selectedClefs = Clef.treble.name();

    public enum InputType {
        keys,
        mic,
        bt,
        usb
    }

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
        this.isHideTreble = false;
        this.isHideBass = false;
        this.isKeyInputPiano = true;
        this.isShowPianoLetters = true;
        this.isMuted = false;

        this.isLogging = true;

        this.activeInput = K_INPUTKEYS;
        this.lastConnectedUsbDevice = "";
        this.lastConnectedBtDevice = "";
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

    public InputType getActiveInput() {
        InputType typeToReturn;
        this.activeInput = this.preferences.getString(K_ACTIVEINPUT, this.activeInput);
        if (null != this.activeInput) {
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
        }
        else {
            Log.error("Failed to find the active input type");
            typeToReturn = InputType.keys;
        }
        return typeToReturn;
    }

    public boolean getIsKeyInputPiano() {
        this.isKeyInputPiano = this.preferences.getBoolean(K_ISKEYINPUTPIANO, this.isKeyInputPiano);
        return this.isKeyInputPiano;
    }

    public Settings setIsKeyInputPiano(boolean isPiano) {
        this.isKeyInputPiano = isPiano;
        this.editor.putBoolean(K_ISKEYINPUTPIANO, this.isKeyInputPiano);
        return this;
    }

    public boolean getIsShowPianoLetters() {
        this.isShowPianoLetters = this.preferences.getBoolean(K_ISSHOWPIANOLETTERS, this.isShowPianoLetters);
        return this.isShowPianoLetters;
    }

    public Settings setIsShowPianoLetters(boolean isShowPianoLetters) {
        this.isShowPianoLetters = isShowPianoLetters;
        this.editor.putBoolean(K_ISSHOWPIANOLETTERS, this.isShowPianoLetters);
        return this;
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
                break;
            case keys:
            default:
                this.activeInput = K_INPUTKEYS;
                break;
        }
        this.editor.putString(K_ACTIVEINPUT, this.activeInput);
        return this;
    }

    public Clef[] getSelectedClefs() {
        Clef[] toReturn = null;
        this.selectedClefs = this.preferences.getString(K_SELECTEDCLEF, this.selectedClefs);
        if (null != selectedClefs) {
            String[] clefStrings = this.selectedClefs.split(",");
            toReturn = new Clef[clefStrings.length];
            for (int i = 0; i < clefStrings.length; ++i) {
                // get the enum from the string representation
                toReturn[i] = Clef.valueOf(Clef.class, clefStrings[i]);
            }
        }
        return toReturn;
    }

    public Settings setSelectedClefs(Clef[] clefs) {
        StringBuilder builder = new StringBuilder();
        for (Clef clef : clefs) {
            builder.append(clef.name());
            builder.append(",");
        }
        // remove the trailing comma
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        this.selectedClefs = builder.toString();
        this.editor.putString(K_SELECTEDCLEF, this.selectedClefs);
        return this;
    }

    public String getLastConnectedUsbDevice() {
        this.lastConnectedUsbDevice = this.preferences.getString(K_LASTCONNECTEDUSBDEVICE, this.lastConnectedUsbDevice);
        return this.lastConnectedUsbDevice;
    }

    public Settings setLastConnectedUsbDevice(String device) {
        this.lastConnectedUsbDevice = device;
        this.editor.putString(K_LASTCONNECTEDUSBDEVICE, this.lastConnectedUsbDevice);
        return this;
    }

    public String getLastConnectedBtDevice() {
        this.lastConnectedBtDevice = this.preferences.getString(K_LASTCONNECTEDBTDEVICE, this.lastConnectedBtDevice);
        return this.lastConnectedBtDevice;
    }

    public Settings setLastConnectedBtDevice(String device) {
        this.lastConnectedBtDevice = device;
        this.editor.putString(K_LASTCONNECTEDBTDEVICE, this.lastConnectedBtDevice);
        return this;
    }

    public boolean getIsMuted() {
        this.isMuted = this.preferences.getBoolean(K_ISMUTED, this.isMuted);
        return this.isMuted;
    }

    public Settings setIsMuted(boolean isMuted) {
        this.isMuted = isMuted;
        this.editor.putBoolean(K_ISMUTED, this.isMuted);
        return this;
    }

    public boolean getIsHideClef(Clef clef) {
        boolean toReturn = false;
        switch (clef) {
            case treble: {
                this.isHideTreble = this.preferences.getBoolean(K_ISHIDETREBLE, this.isHideTreble);
                toReturn = this.isHideTreble;
                break;
            }
            case bass: {
                this.isHideBass = this.preferences.getBoolean(K_ISHIDEBASS, this.isHideBass);
                toReturn = this.isHideBass;
                break;
            }
        }
        return toReturn;
    }

    public Settings setIsHideClef(Clef clef, boolean isHideClef) {
        switch (clef) {
            case treble: {
                this.isHideTreble = isHideClef;
                this.editor.putBoolean(K_ISHIDETREBLE, this.isHideTreble);
                break;
            }
            case bass: {
                this.isHideBass = isHideClef;
                this.editor.putBoolean(K_ISHIDEBASS, this.isHideBass);
                break;
            }
        }
        return this;
    }

    public void commitChanges() {
        // commit the changes that were set
        this.editor.commit();
    }
}

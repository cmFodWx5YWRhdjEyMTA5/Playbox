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

    // the settings - important for defaults
    private boolean isLogging = true;

    private boolean isInputKeys = true;
    private boolean isInputMic = true;
    private boolean isInputBt = true;
    private boolean isInputUsb = true;

    private String activeInput = K_INPUTKEYS;

    private String lastConnectedUsbDevice = "";
    private String lastConnectedBtDevice = "";

    private boolean isHideTreble = false;
    private boolean isHideBass = false;

    private String selectedClefs = Clef.treble.name();

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

    public boolean getShowMenuRight() {
        return this.preferences.getBoolean(K_ISSHOWMENURIGHT, false);
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
        this.selectedClefs = this.preferences.getString(K_SELECTEDCLEF, this.selectedClefs);
        String[] clefStrings = this.selectedClefs.split(",");
        Clef[] toReturn = new Clef[clefStrings.length];
        for (int i = 0; i < clefStrings.length; ++i) {
            // get the enum from the string representation
            toReturn[i] = Clef.valueOf(Clef.class, clefStrings[i]);
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

package uk.co.darkerwaters.smspeak;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class State {

    public static final String PREFS_NAME = "MyApp_Settings";
    public static final String LOGTAG = "SMSpeak";
    private final SharedPreferences preferences;

    private boolean isTalkTime = true;
    private boolean isTalkIntro = true;
    private boolean isTalkContact = true;
    private boolean isTalkMessage = true;

    private String intro = null;
    private final String defaultIntro;

    private boolean isTalkAlways = false;
    private boolean isTalkHeadphones = true;
    private boolean isTalkHeadset = true;
    private boolean isTalkBluetooth = false;

    private final HashSet<String> bluetoothDevices = new HashSet<String>();
    private String connectedBtDevice = "";

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
        boolean isTalkActive = this.isTalkAlways;
        if (false == isTalkActive && null != iStatus) {
            // from this we can get our state of headphones
            int headSetState = iStatus.getIntExtra("state", 0);      //get the headset state property
            int hasMicrophone = iStatus.getIntExtra("microphone", 0);//get the headset microphone property
            // check our current settings
            if (this.isTalkHeadphones && headSetState == 1 && hasMicrophone == 0) {
                // this is headphones connected
                isTalkActive = true;
            } else if (this.isTalkHeadset && headSetState == 1 && hasMicrophone == 1) {
                // this is headset connected (have microphone)
                isTalkActive = true;
            }
        }
        if (false == isTalkActive) {
            // need to check the connection status of BT devices then
            String connectedDevice = getConnectedBtDevice(context);
            if (this.isTalkBluetooth) {
                // talk is active if we are connected to some BT device
                isTalkActive = null != connectedDevice && false == connectedDevice.isEmpty();
            }
            else {
                // check the list for a connected BT device
                if (null != connectedDevice && false == connectedDevice.isEmpty()) {
                    // there is a connected device, should we talk (is it in the list?)
                    isTalkActive = isTalkBtDevice(connectedDevice);
                }
            }
        }
        // return if we should talk
        return isTalkActive;
    }

    public String getConnectedBtDevice(Context context) {
        synchronized (this.preferences) {
            return this.preferences.getString("connectedBtDevice", this.connectedBtDevice);
        }
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

    public boolean isTalkAlways() {
        synchronized (this.preferences) {
            return this.preferences.getBoolean("isTalkAlways", this.isTalkAlways);
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

    public boolean isTalkBluetooth() {
        synchronized (this.preferences) {
            return this.preferences.getBoolean("isTalkBluetooth", this.isTalkBluetooth);
        }
    }

    public boolean isTalkBtDevice(String deviceName) {
        boolean returnValue = false;
        // first load the list of devices into our member list
        synchronized (this.preferences) {
            this.bluetoothDevices.clear();
            int noDevices = this.preferences.getInt("BTDeviceCount", 0);
            for (int i = 1; i <= noDevices; ++i) {
                String device = this.preferences.getString("BTDevice" +  i, "");
                if (device != null && false == device.isEmpty()) {
                    // this is ok, add to the list
                    this.bluetoothDevices.add(device);
                }
            }
            for (String device : this.bluetoothDevices) {
                // check each device
                if (device.equals(deviceName)) {
                    // this is the one
                    returnValue = true;
                    break;
                }
            }
        }
        return returnValue;
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

    public void setIsTalkAlways(boolean isTalkAlways) {
        synchronized (this.preferences) {
            this.isTalkAlways = isTalkAlways;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isTalkAlways", this.isTalkAlways);
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

    public void setIsTalkBluetooth(boolean isTalkBluetooth) {
        synchronized (this.preferences) {
            this.isTalkBluetooth = isTalkBluetooth;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isTalkBluetooth", this.isTalkBluetooth);
            editor.commit();
        }
    }

    public void setIsTalkBtDevice(String deviceName, boolean isTalk) {
        synchronized (this.preferences) {
            if (isTalk) {
                // add this device
                this.bluetoothDevices.add(deviceName);
            }
            else {
                // remove this device
                this.bluetoothDevices.remove(deviceName);
            }
            // and put into the preferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("BTDeviceCount", this.bluetoothDevices.size());
            int i = 1;
            for (String device : this.bluetoothDevices) {
                editor.putString("BTDevice" +  i, device);
                ++i;
            }
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

    public void setConnectedBtDevice(String device, boolean isConnected) {
        if (device == null || false == isConnected) {
            // no connection
            device = "";
        }
        synchronized (this.preferences) {
            this.connectedBtDevice = device;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("connectedBtDevice", this.connectedBtDevice);
            editor.commit();
        }
    }

}

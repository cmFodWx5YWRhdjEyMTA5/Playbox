package uk.co.darkerwaters.staveinvaders.Input;

import android.bluetooth.BluetoothDevice;
import android.os.Build;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputBluetooth extends InputMidi {

    public InputBluetooth(Application application) {
        super(application);
        // constructor for the input type, set everything up here
        Log.debug("input type bluetooth initialised");
    }

    @Override
    public void shutdown() {
        Log.debug("input type bluetooth shutdown");

    }

    public static String GetMidiDeviceId(BluetoothDevice device) {
        String deviceId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != device) {
            deviceId = device.getName();
        }
        return deviceId == null ? "" : deviceId;
    }
}

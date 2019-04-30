package uk.co.darkerwaters.staveinvaders.Input;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

class BluetoothDeviceList {
    private final List<BluetoothDevice> devices;
    private BluetoothDevice defaultDevice;

    BluetoothDeviceList() {
        devices = new ArrayList<BluetoothDevice>();
        defaultDevice = null;
    }

    void add(BluetoothDevice device) {
        synchronized (this.devices) {
            // remove any that match this device
            String deviceId = InputBluetooth.GetMidiDeviceId(device);
            for (BluetoothDevice old : this.devices) {
                if (InputBluetooth.GetMidiDeviceId(old).equals(deviceId)) {
                    // this is a match - remove
                    this.devices.remove(old);
                    break;
                }
            }
            // add the new one
            this.devices.add(device);
            if (null == defaultDevice || InputBluetooth.GetMidiDeviceId(defaultDevice).equals(deviceId)) {
                // there is no default, or this replaces it
                defaultDevice = device;
            }
        }
    }

    BluetoothDevice getDefaultDevice() {
        return this.defaultDevice;
    }

    int size() {
        synchronized (this.devices) {
            return this.devices.size();
        }
    }

    BluetoothDevice[] getAll() {
        synchronized (this.devices) {
            return this.devices.toArray(new BluetoothDevice[this.devices.size()]);
        }
    }
}

package uk.co.darkerwaters.staveinvaders.input;

import android.bluetooth.BluetoothDevice;
import android.media.midi.MidiDeviceInfo;
import android.os.Build;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputBluetooth extends InputMidi {

    public interface BluetoothListener {
        void midiBtScanStatusChange(boolean isScanning);
        void midiBtDeviceDiscovered(BluetoothDevice device);
    }

    public InputBluetooth(Application application) {
        super(application);
        // constructor for the input type, set everything up here
        Log.debug("input type bluetooth initialised");
    }

    @Override
    public void initialiseConnection() {
        super.initialiseConnection();
        // not implemented yet
        setStatus(InputSelector.Status.error);
    }

    @Override
    public int getStatusDrawable(InputSelector.Status status) {
        switch (status) {
            case connecting:
                return R.drawable.ic_baseline_bluetooth_searching_24px;
            case connected:
                return R.drawable.ic_baseline_bluetooth_connected_24px;
            case disconnected:
            case unknown:
                return R.drawable.ic_baseline_bluetooth_24px;
        }
        // if here then let the base class decide
        return super.getStatusDrawable(status);
    }

    @Override
    public void shutdown() {
        Log.debug("input type bluetooth shutdown");

    }

    @Override
    protected void onDeviceAdded(MidiDeviceInfo device) {

    }

    @Override
    protected void onDeviceRemoved(MidiDeviceInfo device) {

    }

    public static String GetMidiDeviceId(BluetoothDevice device) {
        String deviceId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != device) {
            deviceId = device.getName();
        }
        return deviceId == null ? "" : deviceId;
    }
}

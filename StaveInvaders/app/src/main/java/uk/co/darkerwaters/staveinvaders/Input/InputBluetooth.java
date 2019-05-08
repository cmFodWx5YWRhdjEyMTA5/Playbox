package uk.co.darkerwaters.staveinvaders.input;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputBluetooth extends InputMidi {

    public static final int REQUEST_ENABLE_BT = 123;
    public static final int REQUEST_ENABLE_PERMISSIONS = 122;
    public static final int K_BT_SCAN_PERIOD = 10000;

    private static final String BT_OVER_LE_UUID = "03B80E5A-EDE8-4B33-A751-6CE34EC4C700";

    private BluetoothManager bluetoothManager = null;
    private ScanCallback btScanCallback = null;
    private volatile boolean isBtScanning = false;
    private BluetoothLeScanner btLeScanner = null;

    private String activeConnectionId = "";
    private String activeMidiConnectionId = "";

    private static final BluetoothDeviceList btDevices = new BluetoothDeviceList();

    public interface BluetoothListener {
        void midiBtScanStatusChange(boolean isScanning);
        void midiBtDeviceDiscovered(BluetoothDevice device);
    }

    private final List<BluetoothListener> bluetoothListeners = new ArrayList<BluetoothListener>();

    public InputBluetooth(Application application) {
        super(application);
        // constructor for the input type, set everything up here
        Log.debug("input type bluetooth initialised");
    }

    @Override
    public void initialiseConnection() {
        super.initialiseConnection();
        // start up the BT stuff
        if (initialiseBluetooth(this.application.getMainActivity())) {
            // we are initialised, scan too
            scanForBluetoothDevices(this.application.getMainActivity(), true);
        }
    }

    public boolean addListener(BluetoothListener listener) {
        synchronized (bluetoothListeners) {
            return bluetoothListeners.add(listener);
        }
    }

    public boolean removeListener(BluetoothListener listener) {
        synchronized (bluetoothListeners) {
            return bluetoothListeners.remove(listener);
        }
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

    public boolean isBtAvailable(Context context) {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private boolean initialiseBluetooth(final Activity context) {
        if (null == context) {
            // no main activity, cannot do this here then
            Log.error("Failed to initialise BT as no main activity when requested to do so");
            setStatus(InputSelector.Status.error);
            return false;
        }
        BluetoothAdapter adapter = null;
        // check we have the permissions to perform a search here
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // no permission, show the dialog to ask for it
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Show user dialog to grant permissions
                    ActivityCompat.requestPermissions(context,
                            new String[]{
                                    android.Manifest.permission.READ_CONTACTS,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            REQUEST_ENABLE_PERMISSIONS);
                }
            });
            setStatus(InputSelector.Status.error);
            return false;
        }
        // create the manager and get the adapter to check it
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            adapter = bluetoothManager.getAdapter();
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (adapter == null || false == adapter.isEnabled()) {
                // not enabled
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        boolean isConnected = false;
        if (null != this.bluetoothManager) {
            // there is a manager
            if (null == adapter && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                adapter = this.bluetoothManager.getAdapter();
            }
            isConnected = null != adapter && adapter.isEnabled();
        }
        return isConnected;
    }

    public boolean stopBluetoothScanning() {
        // stop scanning
        if (null != this.btLeScanner && null != btScanCallback) {
            this.btLeScanner.stopScan(btScanCallback);
            this.btScanCallback = null;
        }
        this.isBtScanning = false;
        // inform the listeners of this change
        synchronized (bluetoothListeners) {
            for (BluetoothListener listener : bluetoothListeners) {
                listener.midiBtScanStatusChange(this.isBtScanning);
            }
        }
        return isBtScanning == false;
    }

    public boolean scanForBluetoothDevices(final Activity context, boolean isIncludePrevious) {
        // initialise it all
        if (!initialiseBluetooth(context)) {
            return false;
        }
        if (null != this.bluetoothManager && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // there is a manager, get the adapter and do the scan
            BluetoothAdapter adapter = this.bluetoothManager.getAdapter();
            if (null != adapter && adapter.isEnabled()) {
                // finally we can start the scan on this adapter, just looking for BLE MIDI devices
                // if we are already scanning, stop already
                if (this.isBtScanning()) {
                    // stop the old scanning
                    stopBluetoothScanning();
                }
                // get the active scanner we want to use
                this.btLeScanner = adapter.getBluetoothLeScanner();
                // create the callback we need for being informed of devices found
                btScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        processDevice(result.getDevice());
                    }
                    private void processDevice(BluetoothDevice device) {
                        // add to the list
                        btDevices.add(device);
                        // inform the listeners of this new device available
                        onBtDeviceDiscovered(device);
                    }
                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        super.onBatchScanResults(results);
                        for (ScanResult result : results) {
                            processDevice(result.getDevice());
                        }
                    }
                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        Log.error("BTLE Scanning error code encountered \"" + errorCode + "\".");
                    }
                };
                // start scanning and stop after a pre-defined scan period.
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // stop without passing the adapter, will get it again - more current
                        stopBluetoothScanning();
                    }
                }, K_BT_SCAN_PERIOD);
                // do we want to inform of existing found devices
                if (isIncludePrevious) {
                    // send a message for each device we already have
                    for (BluetoothDevice device : btDevices.getAll()) {
                        // inform the listeners of this previously available device
                        onBtDeviceDiscovered(device);
                    }
                }
                // and start scanning
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
                filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(BT_OVER_LE_UUID)).build());
                // scan with these generic settings
                this.btLeScanner.startScan(filters, settings, btScanCallback);
                this.isBtScanning = true;
            }
        }
        // inform the listeners of this change in status
        synchronized (bluetoothListeners) {
            for (BluetoothListener listener : bluetoothListeners) {
                listener.midiBtScanStatusChange(this.isBtScanning);
            }
        }
        // return if we are scanning now
        return this.isBtScanning;
    }

    private void onBtDeviceDiscovered(BluetoothDevice device) {
        // are we connected to any device yet?
        if (this.activeConnectionId == null || this.activeConnectionId.isEmpty()) {
            // there is no active connection, is this one to connect to?
            String deviceId = GetMidiDeviceId(device);
            String lastDevice = this.application.getSettings().getLastConnectedBtDevice();
            if (lastDevice == null || lastDevice.isEmpty() || lastDevice.equals(deviceId)) {
                // either there was no last device, or this is the last device, connect to this
                connectToDevice(device);
            }
        }
        else {
            // so there is an active connection, is this a new discovery of the same device?
            // if it is then connect to this instead as newer is better
            String deviceId = InputBluetooth.GetMidiDeviceId(device);
            if (false == isConnectionActive() && this.activeConnectionId.equals(deviceId)) {
                // this is a new instance of the existing connection
                // connect to the new instance of this device
                connectToDevice(device);
            }
        }
        // inform the listeners we discovered a BT device
        synchronized (bluetoothListeners) {
            for (BluetoothListener listener : bluetoothListeners) {
                listener.midiBtDeviceDiscovered(device);
            }
        }
    }

    public boolean isBtScanning() {
        return this.isBtScanning;
    }

    public boolean connectToDevice(final BluetoothDevice item) {
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != item) {
            // check our existing connection
            if (isConnectionActive() && this.activeConnectionId.equals(GetMidiDeviceId(item))) {
                // this is connected already
                isConnected = true;
                // set our status correctly
                setStatus(InputSelector.Status.connected);
            }
            else {
                // things can go badly wrong in the depths of MIDI and BT so try/catch it
                try {
                    // store what we are trying here
                    this.activeConnectionId = GetMidiDeviceId(item);
                    // remember this was the last connected device
                    application.getSettings().setLastConnectedBtDevice(GetMidiDeviceId(item));
                    // set our status correctly
                    setStatus(InputSelector.Status.connecting);
                    // and open the device
                    this.midiManager.openBluetoothDevice(item, new MidiManager.OnDeviceOpenedListener() {
                        @Override
                        public void onDeviceOpened(MidiDevice midiDevice) {
                            // opened a device, find the output port for this and connect to it
                            // also checking that it is from our attempt to connect a BT device
                            if (connectToDevice(midiDevice)) {
                                // we connected, remember this ID
                                activeMidiConnectionId = GetMidiDeviceId(item);
                                // set our status correctly
                                setStatus(InputSelector.Status.connected);
                            }
                        }
                    }, new Handler(Looper.getMainLooper()));
                    // if here then it didn't throw - we are connected
                    isConnected = true;
                } catch (Exception e) {
                    // inform the dev but just carry on and return out false
                    Log.error("Failed to open BT", e);
                }
            }
        }
        return isConnected;
    }

    @Override
    public void shutdown() {
        // shutdown the base
        super.shutdown();
        Log.debug("input type bluetooth shutdown");
        // stop any BT scanning that might be running
        stopBluetoothScanning();
    }

    public String getActiveMidiConnection() {
        return this.activeMidiConnectionId;
    }

    public String getActiveConnection() {
        return this.activeConnectionId;
    }

    @Override
    protected void onDeviceAdded(MidiDeviceInfo device) {
        //  found a device, will connect to this if the default
    }

    @Override
    protected void onDeviceRemoved(MidiDeviceInfo device) {
        // MIDI device was removed, if this was what we are connected to then
        // we want to reconnect straight away if we can
        if (this.activeConnectionId.equals(GetMidiDeviceId(device))) {
            // just removed the BT device that we are using, stop using it
            closeOpenMidiConnection();
            // try to reconnect as it might just be a little glitch
            scanForBluetoothDevices(this.application.getMainActivity(),true);
        }
    }

    /*
    need to do this special as scanning twice can miss previously discovered devices
    so we will have to keep and refresh a nice static list of BT devices
     */
    private static class BluetoothDeviceList {
        private final List<BluetoothDevice> devices;
        private BluetoothDevice defaultDevice;
        BluetoothDeviceList() {
            devices = new ArrayList<BluetoothDevice>();
            defaultDevice = null;
        }
        void add(BluetoothDevice device) {
            synchronized (this.devices) {
                // remove any that match this device
                String deviceId = GetMidiDeviceId(device);
                for (BluetoothDevice old : this.devices) {
                    if (GetMidiDeviceId(old).equals(deviceId)) {
                        // this is a match - remove
                        this.devices.remove(old);
                        break;
                    }
                }
                // add the new one
                this.devices.add(device);
                if (null == defaultDevice || GetMidiDeviceId(defaultDevice).equals(deviceId)) {
                    // there is no default, or this replaces is
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

    public static String GetMidiDeviceId(BluetoothDevice device) {
        String deviceId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != device) {
            deviceId = device.getName();
        }
        return deviceId == null ? "" : deviceId;
    }
}

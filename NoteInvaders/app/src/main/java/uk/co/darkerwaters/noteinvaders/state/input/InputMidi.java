package uk.co.darkerwaters.noteinvaders.state.input;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.renderscript.ScriptGroup;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.co.darkerwaters.noteinvaders.NoteInvaders;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;


/*
Android MIDI Reference https://developer.android.com/reference/android/media/midi/package-summary
Run ADB over WIFI https://developer.android.com/studio/command-line/adb
Where is ADB? https://stackoverflow.com/questions/5526470/trying-to-add-adb-to-path-variable-osx/19764254
Spec of MIDI protocol http://www.music-software-development.com/midi-tutorial.html
Desc of MIDI receiver https://developer.android.com/reference/android/media/midi/MidiReceiver?authuser=3&hl=it
BLE guide https://developer.android.com/guide/topics/connectivity/bluetooth-le
 */
public class InputMidi extends InputConnection {

    public static final int REQUEST_ENABLE_BT = 123;
    public static final int REQUEST_ENABLE_PERMISSIONS = 122;
    private static final String BT_OVER_LE_UUID = "03B80E5A-EDE8-4B33-A751-6CE34EC4C700";

    private static final int K_BT = 0;
    private static final int K_USB = 1;
    private static final int K_MIDI = 2;
    public static final int K_BT_SCAN_PERIOD = 10000;

    public interface MidiListener {
        void midiDeviceConnectivityChanged(MidiDeviceInfo deviceInfo, boolean isConnected);
        void midiDeviceConnectionChanged(String deviceId, boolean isConnected);
        void midiBtScanStatusChange(boolean isScanning);
        void midiBtDeviceDiscovered(BluetoothDevice device);
    }

    private final MidiManager midiManager;
    private BluetoothManager bluetoothManager = null;
    private BluetoothAdapter.LeScanCallback btScanCallback = null;
    private volatile boolean isBtScanning = false;
    private MidiManager.DeviceCallback callback = null;

    private final List<MidiListener> listeners;

    public static String GetMidiDeviceId(MidiDeviceInfo info) {
        String deviceId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != info) {
            Bundle properties = info.getProperties();
            deviceId = properties.getString(MidiDeviceInfo.PROPERTY_SERIAL_NUMBER);
            if (null == deviceId || deviceId.isEmpty()) {
                deviceId = properties.getString(MidiDeviceInfo.PROPERTY_NAME);
            }
        }
        return deviceId == null ? "" : deviceId;
    }

    public static String GetMidiDeviceId(BluetoothDevice device) {
        String deviceId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != device) {
            deviceId = device.getName();
        }
        return deviceId == null ? "" : deviceId;
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

    private int lastConnectionType = -1;
    private MidiDeviceInfo defaultUsbDevice = null;
    private static final BluetoothDeviceList btDevices = new BluetoothDeviceList();
    private List<MidiDeviceInfo> usbDevices = new ArrayList<MidiDeviceInfo>();
    private MidiOutputPort openMidiOutputPort = null;
    private String[] activeConnectionId = new String[3];
    private final Note[] midiNotes;

    //MIDI COMMAND MASKS / DATA
    private static final byte COMMAND_BYTE_MASK = (byte) 0x80;
    private static final byte STATUS_COMMAND_MASK = (byte) 0xF0;
    private static final byte STATUS_CHANNEL_MASK = (byte) 0x0F;
    private static final byte STATUS_NOTE_OFF = (byte) 0x80;
    private static final byte STATUS_NOTE_ON = (byte) 0x90;

    private enum MidiCommand {
        None,
        NoteOn,
        NoteOff,
    }
    private MidiCommand runningState = MidiCommand.None;
    private int midiChannel = 0;
    private Note runningNote = null;

    public InputMidi(Activity context) {
        super(context);

        this.listeners = new ArrayList<InputMidi.MidiListener>();
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI) &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // do MIDI stuff
            this.midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
            initialiseMidi(context);
        }
        else {
            this.midiManager = null;
        }

        // map the notes to their MIDI index (middle C being 60)
        Notes notes = NoteInvaders.getNotes();
        // notes in MIDI are indexed from 0 to 127, create a nice lookup array for this purpose.
        this.midiNotes = new Note[128];
        // find middle C
        this.midiNotes[60] =  notes.getNote("C4");
        int middleC = notes.getNoteIndex(this.midiNotes[60].getFrequency());
        // ok then, from here we can do down to zero
        int offset = 1;
        for (int i = 59; i >= 0 && offset <= middleC; --i) {
            this.midiNotes[i] = notes.getNote(middleC - offset++);
        }
        // also we can go up to 127
        offset = 1;
        for (int i = 61; i < 128 && middleC + offset < notes.getNoteCount(); ++i) {
            this.midiNotes[i] = notes.getNote(middleC + offset++);
        }
    }

    public void initialiseMidi(final Activity context) {
        // setup MIDI on this activity
        // and listen for hot-plugins
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                null != this.midiManager &&
                null == this.callback) {
            this.callback = new MidiManager.DeviceCallback() {
                @Override
                public void onDeviceAdded(MidiDeviceInfo device) {
                    super.onDeviceAdded(device);
                    // inform listeners of this discovery
                    synchronized (listeners) {
                        for (MidiListener listener : listeners) {
                            listener.midiDeviceConnectivityChanged(device, true);
                        }
                    }
                }
                @Override
                public void onDeviceRemoved(MidiDeviceInfo device) {
                    super.onDeviceRemoved(device);
                    // change the selected device to not be the thing just unplugged
                    if (lastConnectionType == K_USB && activeConnectionId[K_USB].equals(GetMidiDeviceId(device))) {
                        // just removed the USB device that we are using, stop using it
                        closeOpenMidiConnection();
                        // set the state to be keyboard now we disconnected
                        NoteInvaders.getAppContext().setSelectedInput(NoteInvaders.InputType.keyboard);
                        // try to reconnect
                        switch (lastConnectionType) {
                            case K_BT:
                                scanForBluetoothDevices(context, true);
                                break;
                            case K_USB:
                                getConnectedUsbDevices();
                                break;
                        }
                    }
                }
            };
            this.midiManager.registerDeviceCallback(this.callback, new Handler(Looper.getMainLooper()));
        }
    }

    @Override
    public boolean stopConnection() {
        // stop listening
        if (null != this.callback &&
                null != this.midiManager &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.midiManager.unregisterDeviceCallback(this.callback);
            this.callback = null;
        }
        // stop any BT scanning that might be running
        stopBluetoothScanning(null);
        // stop it all
        closeOpenMidiConnection();
        // return our success
        return true;
    }

    public void closeOpenMidiConnection() {
        if (null != openMidiOutputPort) {
            try {
                openMidiOutputPort.close();
                openMidiOutputPort = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            // remember the ID we just disconnected
            String deviceDisconnected = activeConnectionId[lastConnectionType];
            for (int i = 0; i < 3; ++i) {
                // clear the connection IDs all to nothing
                activeConnectionId[i] = new String();
            }
            // inform the listners of this disconnection
            synchronized (this.listeners) {
                for (MidiListener listener : this.listeners) {
                    listener.midiDeviceConnectionChanged(deviceDisconnected, false);
                }
            }
        }
    }

    @Override
    public boolean startConnection() {
        // start everything up
        return true;
    }

    private boolean initialiseBluetooth(final Activity context) {
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

    public boolean stopBluetoothScanning(BluetoothAdapter adapter) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (null == adapter) {
                // no adapter, get it
                if (null != this.bluetoothManager) {
                    // there is a manager, get the adapter and do the scan
                    adapter = this.bluetoothManager.getAdapter();
                }
            }
            if (null != adapter && adapter.isEnabled()) {
                adapter.stopLeScan(btScanCallback);
                isBtScanning = false;
            }
        }
        // inform the listeners of this change
        synchronized (listeners) {
            for (MidiListener listener : listeners) {
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
                // if we are already scanning, stop already
                if (this.isBtScanning()) {
                    // stop the old scanning
                    stopBluetoothScanning(adapter);
                }
                // create the callback we need for being informed of devices found
                btScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                        // add this to the list of BT devices
                        btDevices.add(device);
                        // inform the listeners of this new device available
                        synchronized (listeners) {
                            for (MidiListener listener : listeners) {
                                listener.midiBtDeviceDiscovered(device);
                            }
                        }
                    }
                };
                // Stops scanning after a pre-defined scan period.
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // stop without passing the adapter, will get it again - more current
                        stopBluetoothScanning(null);
                    }
                }, K_BT_SCAN_PERIOD);

                if (isIncludePrevious) {
                    // send a message for each device we already have
                    for (BluetoothDevice device : btDevices.getAll()) {
                        // inform the listeners of this previously available device
                        synchronized (listeners) {
                            for (MidiListener listener : listeners) {
                                listener.midiBtDeviceDiscovered(device);
                            }
                        }
                    }
                }

                // finally we can start the scan on this adapter, just looking for BLE MIDI devices
                if (adapter.startLeScan(new UUID[] {UUID.fromString(BT_OVER_LE_UUID)}, btScanCallback)) {
                    // this is scanning, remember this
                    this.isBtScanning = true;
                }
            }
        }
        // inform the listeners of this change in status
        synchronized (listeners) {
            for (MidiListener listener : listeners) {
                listener.midiBtScanStatusChange(this.isBtScanning);
            }
        }
        // return if we are scanning now
        return this.isBtScanning;
    }

    public boolean isBtScanning() {
        return this.isBtScanning;
    }

    public boolean addMidiListener(MidiListener listener) {
        synchronized (listeners) {
            return listeners.add(listener);
        }
    }

    public boolean removeMidiListener(MidiListener listener) {
        synchronized (listeners) {
            return listeners.remove(listener);
        }
    }

    public boolean isMidiAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI) &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
    }

    public boolean isBtAvailable(Context context) {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public List<MidiDeviceInfo> getConnectedUsbDevices() {
        this.defaultUsbDevice = null;
        synchronized (this.usbDevices) {
            this.usbDevices.clear();
            if (null != this.midiManager && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                MidiDeviceInfo[] infos = this.midiManager.getDevices();
                for (MidiDeviceInfo info : infos) {
                    // get the ID for this, everything should have one.
                    String deviceId = InputMidi.GetMidiDeviceId(info);
                    if (deviceId != null && deviceId.isEmpty() == false && info.getOutputPortCount() > 0) {
                        // this is valid
                        usbDevices.add(info);
                        if (null == this.defaultUsbDevice || deviceId.equals(NoteInvaders.getAppContext().getMidiDeviceId())) {
                            // this is the correct default USB device to use
                            this.defaultUsbDevice = info;
                        }
                    }
                }
            }
            // return all the found USB devices (own copy to stop them messing with our list)
            return new ArrayList<MidiDeviceInfo>(this.usbDevices);
        }
    }

    public MidiDeviceInfo getDefaultUsbDevice() {
        return this.defaultUsbDevice;
    }

    public int getNoUsbDevices() {
        synchronized (this.usbDevices) {
            return this.usbDevices.size();
        }
    }

    public BluetoothDevice getDefaultBtDevice() {
        return btDevices.getDefaultDevice();
    }

    public int getNoBtDevices() {
        return btDevices.size();
    }

    public boolean connectToDevice(final BluetoothDevice item) {
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != item) {
            // check our existing connection
            if (isConnectionActive() &&
                    this.lastConnectionType == K_BT &&
                    this.activeConnectionId[K_BT].equals(GetMidiDeviceId(item))) {
                // this is connected already
                isConnected = true;
            }
            else {
                // things can go badly wrong in the depths of MIDI and BT so try/catch it
                try {
                    // store what we are trying here
                    this.lastConnectionType = K_BT;
                    this.activeConnectionId[K_BT] = GetMidiDeviceId(item);
                    // and open the device
                    this.midiManager.openBluetoothDevice(item, new MidiManager.OnDeviceOpenedListener() {
                        @Override
                        public void onDeviceOpened(MidiDevice midiDevice) {
                            // opened a device, find the output port for this and connect to it
                            // also checking that it is from our attempt to connect a BT device
                            if (lastConnectionType == K_BT && connectToDevice(midiDevice)) {
                                // we connected, remember this ID
                                activeConnectionId[K_MIDI] = GetMidiDeviceId(item);
                                // inform listeners
                                synchronized (listeners) {
                                    for (MidiListener listener : listeners) {
                                        listener.midiDeviceConnectionChanged(activeConnectionId[K_BT], true);
                                    }
                                }
                            }
                        }
                    }, new Handler(Looper.getMainLooper()));
                    // if here then it didn't throw - we are connected
                    isConnected = true;
                } catch (Exception e) {
                    // inform the dev but just carry on and return out false
                    e.printStackTrace();
                }
            }
        }
        return isConnected;
    }

    public boolean connectToDevice(final MidiDeviceInfo item) {
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != item) {
            // check our existing connection
            if (isConnectionActive() &&
                    this.lastConnectionType == K_USB &&
                    this.activeConnectionId[K_USB].equals(GetMidiDeviceId(item))) {
                // this is connected already
                isConnected = true;
            }
            else {
                // things can go badly wrong in the depths of MIDI and BT so try/catch it
                try {
                    // store that we are trying a USB connectio here
                    this.lastConnectionType = K_USB;
                    this.activeConnectionId[K_USB] = GetMidiDeviceId(item);
                    // and open the device
                    this.midiManager.openDevice(item, new MidiManager.OnDeviceOpenedListener() {
                        @Override
                        public void onDeviceOpened(MidiDevice midiDevice) {
                            // annoyingly a message from connecting BT MIDI will fire this,
                            // check we are actually connecting via USB
                            if (lastConnectionType == K_USB && connectToDevice(midiDevice)) {
                                // we connected, remember this ID
                                activeConnectionId[K_MIDI] = GetMidiDeviceId(item);
                                // inform listeners
                                synchronized (listeners) {
                                    for (MidiListener listener : listeners) {
                                        listener.midiDeviceConnectionChanged(activeConnectionId[K_USB], true);
                                    }
                                }
                            }
                        }
                    }, new Handler(Looper.getMainLooper()));
                    // if here then it didn't throw - we ar connected
                    isConnected = true;
                } catch (Exception e) {
                    // inform the dev but just carry on and return out false
                    e.printStackTrace();
                }
            }
        }
        return isConnected;
    }

    private boolean connectToDevice(MidiDevice midiDevice) {
        // close any existing connection
        closeOpenMidiConnection();
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && midiDevice != null) {
            // find the first output port and use it to connect
            for (MidiDeviceInfo.PortInfo port : midiDevice.getInfo().getPorts()) {
                if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                    // this is an output port - connect to this
                    openMidiOutputPort = midiDevice.openOutputPort(port.getPortNumber());
                    if (null != openMidiOutputPort) {
                        // got the port OK - connect to it
                        openMidiOutputPort.connect(new MidiReceiver() {
                            @Override
                            public void onSend(byte[] data, int offset, int count, long timestamp) throws IOException {
                                // have data, process this data
                                processMidiData(data, offset, count, timestamp);
                            }
                        });
                        // if here then it workeds
                        isConnected = true;
                        break;
                    }
                }
            }
        }
        return isConnected;
    }

    public boolean isConnectionActive() {
        return this.openMidiOutputPort != null;
    }

    public String getActiveBtConnection() {
        return this.activeConnectionId[K_BT];
    }

    public String getActiveUsbConnection() {
        return this.activeConnectionId[K_USB];
    }

    public String getActiveMidiConnection() {
        return this.activeConnectionId[K_MIDI];
    }

    private void processMidiData(byte[] data, int offset, int count, long timestamp) {
        // we can ignore the timestamp for ourselves, just doing it live, process the data
        // in reality we are sent the data in batches of 3 (command, volume, velocity) however
        // it can send it in larger batches (command, vol, vel, vol, vel etc) so let's deal with
        // each item of data one at a time.
        for (int i = 0; i < count; ++i) {
            // process each item of data one at a time
            processMidiData(data[offset + i], timestamp);
        }
    }

    private void processMidiData(byte data, long timestamp) {
        // one at a time we will be processing the data and commands, check for a new command
        if ((byte)(data & COMMAND_BYTE_MASK) == COMMAND_BYTE_MASK) {
            // this is the command, the MASK means that it is
            byte command = (byte) (data & STATUS_COMMAND_MASK);
            switch (command) {
                case STATUS_NOTE_OFF:
                    this.runningState = MidiCommand.NoteOff;
                    break;
                case STATUS_NOTE_ON:
                    this.runningState = MidiCommand.NoteOn;
                    break;
                default:
                    // we are not interested in all the other rubbish MIDI sends us
                    this.runningState = MidiCommand.None;
                    break;
            }
            // remember the channel, for interest mostly
            this.midiChannel = (byte) (data & STATUS_CHANNEL_MASK);
            // a new command means a new note
            this.runningNote = null;
        }
        else if (this.runningState != MidiCommand.None) {
            // have the command, process the accompanying data for this command
            if (null == this.runningNote) {
                // this is the first bit of data that follows the command, this is the note
                // to which it refers
                int value = data;
                // the value (integer) is just the data, the index of the note in the array
                if (value < this.midiNotes.length && value >= 0) {
                    // this is valid, use it to get the note
                    this.runningNote = this.midiNotes[value];
                }
                if (this.runningNote == null) {
                    // we don't have this note, but something was played, get the closest
                    Notes notes = NoteInvaders.getNotes();
                    if (value < this.midiNotes.length * 0.5f) {
                        // the note is too low for us, use the least note we have available
                        this.runningNote = notes.getNote(0);
                    }
                    else {
                        // the note is too high for us, use the max note we have available
                        this.runningNote = notes.getNote(notes.getNoteCount() - 1);
                    }
                }
            }
            else {
                // this is the second item of data, their being a note... play the note at the velocity specified
                // we want a probability, 40 is very soft (piano) so this can be 100%
                float velocityToProbability = (data / 40f) * 100f;
                // inform people we have detected the hit note, a velocity of 0 is like a NOTE_OFF
                switch (this.runningState) {
                    case NoteOn:
                        informNoteDetection(this.runningNote, velocityToProbability > 40f, velocityToProbability, 1);
                        break;
                    case NoteOff:
                        informNoteDetection(this.runningNote, false, velocityToProbability, 1);
                        break;
                }
                // no note then, done it here
                this.runningNote = null;
            }
        }
    }
}
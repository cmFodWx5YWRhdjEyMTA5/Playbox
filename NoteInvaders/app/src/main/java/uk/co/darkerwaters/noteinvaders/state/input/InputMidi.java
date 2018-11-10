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

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.State;

public class InputMidi extends InputConnection {

    public static final int REQUEST_ENABLE_BT = 123;
    public static final int REQUEST_ENABLE_PERMISSIONS = 122;
    private static final long SCAN_PERIOD = 10000;
    private static final String BT_OVER_LE_UUID = "03B80E5A-EDE8-4B33-A751-6CE34EC4C700";

    public interface MidiListener {
        void midiDeviceConnectivityChanged(MidiDeviceInfo deviceInfo, boolean isConnected);
        void midiDeviceConnectionChanged(String deviceId, boolean isConnected);
    }
    public interface InputMidiScanListener {
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
            deviceId = device.getAddress();
        }
        return deviceId == null ? "" : deviceId;
    }

    private MidiDeviceInfo defaultDevice = null;
    private BluetoothDevice defaultBtDevice = null;
    private MidiOutputPort openMidiOutputPort = null;
    private String activeMidiConnection = null;
    private final Note[] midiNotes;

    public static final byte COMMAND_BYTE_MASK = (byte) 0x80;

    public static final byte STATUS_COMMAND_MASK = (byte) 0xF0;
    public static final byte STATUS_CHANNEL_MASK = (byte) 0x0F;

    // Channel voice messages.
    public static final byte STATUS_NOTE_OFF = (byte) 0x80;
    public static final byte STATUS_NOTE_ON = (byte) 0x90;

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
        Notes notes = Notes.instance();
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

    private void initialiseMidi(final Context context) {
        // setup MIDI on this activity
        // and listen for hot-plugins
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.callback = new MidiManager.DeviceCallback() {
                @Override
                public void onDeviceAdded(MidiDeviceInfo device) {
                    super.onDeviceAdded(device);
                    // change the selected device to be this thing just plugged in
                    if (device.getOutputPortCount() > 0) {
                        // this is a device that outputs notes to us, connect to this
                        State.getInstance().setMidiDeviceId(context, GetMidiDeviceId(device));
                    }
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
                    if (GetMidiDeviceId(device).equals(State.getInstance().getMidiDeviceId())) {
                        // just removed the device that we are using, stop using it
                        State.getInstance().setSelectedInput(State.InputType.keyboard);
                    }
                    synchronized (listeners) {
                        for (MidiListener listener : listeners) {
                            listener.midiDeviceConnectivityChanged(device, false);
                        }
                    }
                }
            };
            this.midiManager.registerDeviceCallback(callback, new Handler(Looper.getMainLooper()));
        }
    }

    @Override
    public boolean stopConnection() {
        // stop listening
        if (null != this.callback && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.midiManager.unregisterDeviceCallback(this.callback);
            this.callback = null;
        }
        // stop any BT scanning that might be running
        scanForBluetoothDevices(null, null, null, false);
        // stop it all
        closeOpenMidiConnection();
        // return our success
        return true;
    }

    private void closeOpenMidiConnection() {
        if (null != openMidiOutputPort) {
            try {
                openMidiOutputPort.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String deviceDisconnected = activeMidiConnection;
            openMidiOutputPort = null;
            activeMidiConnection = null;
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
        if (null == this.bluetoothManager) {
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

    public boolean scanForBluetoothDevices(final Activity context, final InputMidiScanListener callback, Handler handler, boolean enable) {
        if (enable) {
            // initialise it all
            if (!initialiseBluetooth(context)) {
                return false;
            }
        }
        if (null != this.bluetoothManager && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // there is a manager, get the adapter and do the scan
            final BluetoothAdapter adapter = this.bluetoothManager.getAdapter();
            if (null != adapter && adapter.isEnabled()) {
                // start the discovery
                if (enable) {
                    // create the callback
                    btScanCallback = new BluetoothAdapter.LeScanCallback() {
                        @Override
                        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                            // store the default
                            if (null == defaultBtDevice || InputMidi.GetMidiDeviceId(device).equals(State.getInstance().getMidiDeviceId())) {
                                defaultBtDevice = device;
                            }
                            // inform the callback
                            callback.midiBtDeviceDiscovered(device);
                        }
                    };
                    // Stops scanning after a pre-defined scan period.
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isBtScanning = false;
                            adapter.stopLeScan(btScanCallback);
                            callback.midiBtScanStatusChange(false);
                        }
                    }, SCAN_PERIOD);

                    this.defaultBtDevice = null;
                    UUID[] scaningIds = new UUID[] {UUID.fromString(BT_OVER_LE_UUID)};
                    if (adapter.startLeScan(scaningIds, btScanCallback)) {
                        this.isBtScanning = true;
                        callback.midiBtScanStatusChange(true);
                    }
                } else {
                    adapter.stopLeScan(btScanCallback);
                    isBtScanning = false;
                    if (null != callback) {
                        callback.midiBtScanStatusChange(false);
                    }
                }
            }
        }
        return this.isBtScanning;
    }

    public boolean isBtScanning() {
        return this.isBtScanning;
    }

    public boolean addListener(MidiListener listener) {
        synchronized (listeners) {
            return listeners.add(listener);
        }
    }

    public boolean removeListener(MidiListener listener) {
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

    public List<MidiDeviceInfo> getConnectedDevices() {
        List<MidiDeviceInfo> validDevices = new ArrayList<MidiDeviceInfo>();
        this.defaultDevice = null;
        if (null != this.midiManager && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            MidiDeviceInfo[] infos = this.midiManager.getDevices();
            for (MidiDeviceInfo info : infos) {
                if (info.getOutputPortCount() > 0) {
                    // this is valid
                    validDevices.add(info);
                    if (null == this.defaultDevice || InputMidi.GetMidiDeviceId(info).equals(State.getInstance().getMidiDeviceId())) {
                        this.defaultDevice = info;
                    }
                }
            }
        }
        return validDevices;
    }

    public MidiDeviceInfo getDefaultDevice() {
        if (null == this.defaultDevice) {
            getConnectedDevices();
        }
        return this.defaultDevice;
    }

    public BluetoothDevice getDefaultBtDevice() {
        return this.defaultBtDevice;
    }

    public boolean connectToDevice(final BluetoothDevice item) {
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // we need to ensure that this is valid, wrap in a try.catch
            try {
                this.midiManager.openBluetoothDevice(item, new MidiManager.OnDeviceOpenedListener() {
                    @Override
                    public void onDeviceOpened(MidiDevice midiDevice) {
                        // opened a device, find the output port for this and connect to it
                        if (connectToDevice(midiDevice)) {
                            activeMidiConnection = GetMidiDeviceId(item);
                            synchronized (listeners) {
                                for (MidiListener listener : listeners) {
                                    listener.midiDeviceConnectionChanged(activeMidiConnection, true);
                                }
                            }
                        }
                    }
                }, new Handler(Looper.getMainLooper()));
                isConnected = true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isConnected;
    }

    public boolean connectToDevice(final MidiDeviceInfo item) {
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // we need to ensure that this is valid, wrap in a try.catch
            try {
                this.midiManager.openDevice(item, new MidiManager.OnDeviceOpenedListener() {
                    @Override
                    public void onDeviceOpened(MidiDevice midiDevice) {
                        // opened a device, find the output port for this and connect to it
                        if (connectToDevice(midiDevice)) {
                            activeMidiConnection = GetMidiDeviceId(item);
                            synchronized (listeners) {
                                for (MidiListener listener : listeners) {
                                    listener.midiDeviceConnectionChanged(activeMidiConnection, true);
                                }
                            }
                        }
                    }
                }, new Handler(Looper.getMainLooper()));
                isConnected = true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return isConnected;
    }

    private boolean connectToDevice(MidiDevice midiDevice) {
        // close any existing connection
        closeOpenMidiConnection();
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && midiDevice != null) {
            for (MidiDeviceInfo.PortInfo port : midiDevice.getInfo().getPorts()) {
                if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                    // this is an output port - connect to this
                    openMidiOutputPort = midiDevice.openOutputPort(port.getPortNumber());
                    if (null != openMidiOutputPort) {
                        openMidiOutputPort.connect(new MidiReceiver() {
                            @Override
                            public void onSend(byte[] data, int offset, int count, long timestamp) throws IOException {
                                // have data, process this data
                                processMidiData(data, offset, count, timestamp);
                            }
                        });
                        isConnected = true;
                        break;
                    }
                }
            }
        }
        return isConnected;
    }

    public boolean isConnectionActive() {
        return openMidiOutputPort != null && activeMidiConnection != null && activeMidiConnection.isEmpty() == false;
    }

    public String getActiveMidiConnection() {
        return this.activeMidiConnection == null ? "" : this.activeMidiConnection;
    }

    private void processMidiData(byte[] data, int offset, int count, long timestamp) {
        // we can ignore the timestamp for ourselves, just doing it live, process the data
        if ((byte)(data[offset] & COMMAND_BYTE_MASK) == COMMAND_BYTE_MASK) {
            // this is the command
            byte command = (byte) (data[offset] & STATUS_COMMAND_MASK);
            switch (command) {
                case STATUS_NOTE_OFF:
                    this.runningState = MidiCommand.NoteOff;
                    break;
                case STATUS_NOTE_ON:
                    this.runningState = MidiCommand.NoteOn;
                    break;
                default:
                    this.runningState = MidiCommand.None;
                    break;
            }
            this.midiChannel = (byte) (data[offset] & STATUS_CHANNEL_MASK);
            // have the command, process the accompanying data for this command
            if (this.runningState != MidiCommand.None) {
                // this is the data
                int value = data[offset + 1];
                if (value < this.midiNotes.length && value >= 0) {
                    this.runningNote = this.midiNotes[value];
                }
                if (null != this.runningNote) {
                    // get velocity then
                    float velocityToProbability = (data[offset + 2] / 40f) * 100f;
                    switch (this.runningState) {
                        case NoteOn:
                            informNoteDetection(this.runningNote, velocityToProbability > 99f, velocityToProbability, 1);
                            break;
                        case NoteOff:
                            informNoteDetection(this.runningNote, false, velocityToProbability, 1);
                            break;
                    }
                    // no note then, done it
                    this.runningNote = null;
                }
            }
        }
    }
}
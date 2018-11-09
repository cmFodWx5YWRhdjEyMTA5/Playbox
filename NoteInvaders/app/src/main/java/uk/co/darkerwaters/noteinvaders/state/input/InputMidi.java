package uk.co.darkerwaters.noteinvaders.state.input;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.UsbSetupActivity;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.State;

public class InputMidi extends InputConnection {

    public interface MidiListener {
        void midiDeviceConnectivityChanged(MidiDeviceInfo deviceInfo, boolean isConnected);
    }

    // monitor for connections coming and going
    private static class ConnectionMonitor {
        private final MidiManager midiManager;
        private MidiManager.DeviceCallback callback = null;

        private final List<MidiListener> listeners;

        ConnectionMonitor(Context context) {
            this.listeners = new ArrayList<MidiListener>();
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI) &&
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // do MIDI stuff
                this.midiManager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
                initialiseMidi(context);
            }
            else {
                this.midiManager = null;
            }
        }

        MidiManager getMidiManager() {
            return this.midiManager;
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

        void close() {
            if (null != this.callback && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.midiManager.unregisterDeviceCallback(this.callback);
                this.callback = null;
            }
        }

        boolean addListener(MidiListener listener) {
            synchronized (listeners) {
                return listeners.add(listener);
            }
        }

        boolean removeListener(MidiListener listener) {
            synchronized (listeners) {
                return listeners.remove(listener);
            }
        }
    }

    private static ConnectionMonitor monitor = null;

    public static void InitialiseConnectionMonitor(Context context) {
        // initialise monitoring for connectivity, will change the state of the connection
        // when someone plugs in or unplugs their device.
        monitor = new ConnectionMonitor(context);
    }

    public static void CloseConnectionMonitor() {
        if (null != monitor) {
            monitor.close();
            monitor = null;
        }
    }

    public static String GetMidiDeviceId(MidiDeviceInfo info) {
        String deviceId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Bundle properties = info.getProperties();
            deviceId = properties.getString(MidiDeviceInfo.PROPERTY_SERIAL_NUMBER);
            if (null == deviceId || deviceId.isEmpty()) {
                deviceId = properties.getString(MidiDeviceInfo.PROPERTY_NAME);
            }
        }
        return deviceId == null ? "" : deviceId;
    }

    private MidiDeviceInfo defaultDevice = null;
    private MidiOutputPort openMidiOutputPort = null;
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

    public boolean addListener(MidiListener listener) {
        if (null == monitor) {
            return false;
        }
        return monitor.addListener(listener);
    }

    public boolean removeListener(MidiListener listener) {
        if (null == monitor) {
            return false;
        }
        return monitor.removeListener(listener);
    }

    public boolean isMidiAvailable(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI) &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
    }

    public List<MidiDeviceInfo> getConnectedDevices() {
        List<MidiDeviceInfo> validDevices = new ArrayList<MidiDeviceInfo>();
        defaultDevice = null;
        if (null != monitor.midiManager && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            MidiDeviceInfo[] infos = monitor.midiManager.getDevices();
            for (MidiDeviceInfo info : infos) {
                if (info.getOutputPortCount() > 0) {
                    // this is valid
                    validDevices.add(info);
                    if (null == defaultDevice || InputMidi.GetMidiDeviceId(info).equals(State.getInstance().getMidiDeviceId())) {
                        defaultDevice = info;
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

    public boolean connectToDevice(final MidiDeviceInfo item) {
        boolean isConnected = false;
        if (null != monitor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // we need to ensure that this is valid, wrap in a try.catch
            try {
                monitor.midiManager.openDevice(item, new MidiManager.OnDeviceOpenedListener() {
                    @Override
                    public void onDeviceOpened(MidiDevice midiDevice) {
                        // opened a device, find the output port for this
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
                            informNoteDetection(this.runningNote, velocityToProbability > 1f, velocityToProbability, 1);
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

    @Override
    public boolean stopConnection() {
        // stop it all
        if (null != openMidiOutputPort) {
            try {
                openMidiOutputPort.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            openMidiOutputPort = null;
        }

        return true;
    }

    @Override
    public boolean startConnection() {
        // start everything up

        return true;
    }

}
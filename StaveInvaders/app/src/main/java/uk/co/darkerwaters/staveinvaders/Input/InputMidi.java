package uk.co.darkerwaters.staveinvaders.input;

import android.content.Context;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;

public abstract class InputMidi extends Input {

    //MIDI COMMAND MASKS / DATA
    private static final byte COMMAND_BYTE_MASK = (byte) 0x80;
    private static final byte STATUS_COMMAND_MASK = (byte) 0xF0;
    private static final byte STATUS_CHANNEL_MASK = (byte) 0x0F;
    private static final byte STATUS_NOTE_OFF = (byte) 0x80;
    private static final byte STATUS_NOTE_ON = (byte) 0x90;

    protected MidiManager midiManager;
    private MidiManager.DeviceCallback callback = null;

    private enum MidiCommand {
        None,
        NoteOn,
        NoteOff,
    }

    public interface MidiListener {
        void onMidiDeviceDiscovered(MidiDeviceInfo device);
        void onMidiDeviceRemoved(MidiDeviceInfo device);
        void onMidiDeviceOpened(MidiDeviceInfo device);
        void onMidiDeviceConnected(MidiDeviceInfo device);
    }

    private MidiCommand runningState = MidiCommand.None;
    private int midiChannel = 0;
    private MidiOutputPort openMidiOutputPort = null;
    private final Chord[] midiChords;
    private Chord runningChord = null;

    protected final List<MidiListener> listeners;

    public InputMidi(Application application) {
        super(application);

        // create the listening list
        this.listeners = new ArrayList<MidiListener>();

        // map the chords to their MIDI index (middle C being 60)
        Chords chords = application.getSingleChords();
        // chords in MIDI are indexed from 0 to 127, create a nice lookup array for this purpose.
        this.midiChords = new Chord[128];
        // find middle C and add to number 60
        this.midiChords[60] = chords.getChord("C4");
        int middleC = chords.getChordIndex(this.midiChords[60].notes[0].getFrequency());
        // ok then, from here we can do down to zero
        int offset = 1;
        for (int i = 59; i >= 0 && offset <= middleC; --i) {
            // put the next note down in our array
            this.midiChords[i] = chords.getChord(middleC - offset++);
        }
        // also we can go up to 127
        offset = 1;
        for (int i = 61; i < 128 && middleC + offset < chords.getSize(); ++i) {
            // put the next note up in our array
            this.midiChords[i] = chords.getChord(middleC + offset++);
        }
    }

    @Override
    public void initialiseConnection() {
        super.initialiseConnection();
        // initialise this connection here
        if (isMidiAvailable()) {
            // do MIDI stuff
            this.midiManager = (MidiManager) application.getSystemService(Context.MIDI_SERVICE);
            initialiseMidi();
        }
        else {
            this.midiManager = null;
            Log.error("MIDI IS NOT ENABLED, NO MIDI SUPPORTED");
            setStatus(InputSelector.Status.error);
        }
    }

    @Override
    public void shutdown() {
        // stop listening for MIDI connections coming in
        if (null != this.callback &&
                null != this.midiManager &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.midiManager.unregisterDeviceCallback(this.callback);
            this.callback = null;
        }
        // stop it all
        closeOpenMidiConnection();
    }

    public boolean isMidiAvailable() {
        return this.application.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI) &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
    }

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

    public void initialiseMidi() {
        // setup MIDI on this activity and listen for hot-plugins
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
                            listener.onMidiDeviceDiscovered(device);
                        }
                    }
                    Log.debug("MIDI Device " + GetMidiDeviceId(device) + " discovered");
                    InputMidi.this.onDeviceAdded(device);

                }
                @Override
                public void onDeviceRemoved(MidiDeviceInfo device) {
                    super.onDeviceRemoved(device);
                    // inform listeners of this
                    synchronized (listeners) {
                        for (MidiListener listener : listeners) {
                            listener.onMidiDeviceRemoved(device);
                        }
                    }
                    Log.debug("MIDI Device " + GetMidiDeviceId(device) + " removed");
                    InputMidi.this.onDeviceRemoved(device);
                }
            };
            this.midiManager.registerDeviceCallback(this.callback, new Handler(Looper.getMainLooper()));
        }
    }

    protected abstract void onDeviceAdded(MidiDeviceInfo device);

    protected abstract void onDeviceRemoved(MidiDeviceInfo device);

    public void closeOpenMidiConnection() {
        if (null != openMidiOutputPort) {
            try {
                openMidiOutputPort.close();
                openMidiOutputPort = null;
            } catch (IOException e) {
                Log.error("Failed to close the MIDI connection", e);
            }
        }
    }

    protected void openMidiDevice(final MidiDeviceInfo item) {
        this.midiManager.openDevice(item, new MidiManager.OnDeviceOpenedListener() {
            @Override
            public void onDeviceOpened(MidiDevice midiDevice) {
                // once we have opened the device we will be good to go
                String deviceId = GetMidiDeviceId(midiDevice.getInfo());
                //inform people we are connected to a MIDI device
                synchronized (listeners) {
                    for (MidiListener listener : listeners) {
                        listener.onMidiDeviceOpened(item);
                    }
                }
                // annoyingly a message from connecting BT MIDI will fire this as well as USB
                Log.debug("MIDI Device " + deviceId + " opened");
                // connect to this then, to listen to it
                connectToDevice(midiDevice);
            }
        }, new Handler(Looper.getMainLooper()));
    }

    private boolean connectToDevice(MidiDevice midiDevice) {
        // close any existing connection
        closeOpenMidiConnection();
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && midiDevice != null) {
            // find the first output port and use it to connect
            MidiDeviceInfo midiDeviceInfo = midiDevice.getInfo();
            for (MidiDeviceInfo.PortInfo port : midiDeviceInfo.getPorts()) {
                if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                    // this is an output port - connect to this
                    openMidiOutputPort = midiDevice.openOutputPort(port.getPortNumber());
                    if (null != openMidiOutputPort) {
                        // got the port OK - connect to it
                        setStatus(InputSelector.Status.connecting);
                        // and connect
                        openMidiOutputPort.connect(new MidiReceiver() {
                            @Override
                            public void onSend(byte[] data, int offset, int count, long timestamp) throws IOException {
                                // have data, process this data
                                processMidiData(data, offset, count, timestamp);
                            }
                        });
                        // if here then it worked
                        isConnected = true;
                        // inform people this worked
                        onMidiDeviceConnected(midiDevice);
                        break;
                    }
                }
            }
        }
        return isConnected;
    }

    protected void onMidiDeviceConnected(MidiDevice midiDevice) {
        // inform listeners
        synchronized (listeners) {
            for (MidiListener listener : listeners) {
                listener.onMidiDeviceConnected(midiDevice.getInfo());
            }
        }
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
        // we are doing something, connection active - inform the listeners
        this.signalIsProcessing();
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
            this.runningChord = null;
        }
        else if (this.runningState != MidiCommand.None) {
            // have the command, process the accompanying data for this command
            if (null == this.runningChord) {
                // this is the first bit of data that follows the command, this is the note
                // to which it refers
                int value = data;
                // the value (integer) is just the data, the index of the note in the array
                if (value < this.midiChords.length && value >= 0) {
                    // this is valid, use it to get the note
                    this.runningChord = this.midiChords[value];
                }
                if (this.runningChord == null) {
                    // we don't have this note, but something was played, get the closest
                    Chords notes = this.application.getSingleChords();
                    if (value < this.midiChords.length * 0.5f) {
                        // the note is too low for us, use the least note we have available
                        this.runningChord = notes.getChord(0);
                    }
                    else {
                        // the note is too high for us, use the max note we have available
                        this.runningChord = notes.getChord(notes.getSize() - 1);
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
                        onNoteDetected(this.runningChord, velocityToProbability > 40f, velocityToProbability);
                        break;
                    case NoteOff:
                        onNoteDetected(this.runningChord, false, velocityToProbability);
                        break;
                }
                // no note then, done it here
                this.runningChord = null;
            }
        }
    }
}

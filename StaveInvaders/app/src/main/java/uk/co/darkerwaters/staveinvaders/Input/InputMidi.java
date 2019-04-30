package uk.co.darkerwaters.staveinvaders.Input;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
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
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.notes.Note;
import uk.co.darkerwaters.staveinvaders.notes.Notes;

public abstract class InputMidi extends Input {

    //MIDI COMMAND MASKS / DATA
    private static final byte COMMAND_BYTE_MASK = (byte) 0x80;
    private static final byte STATUS_COMMAND_MASK = (byte) 0xF0;
    private static final byte STATUS_CHANNEL_MASK = (byte) 0x0F;
    private static final byte STATUS_NOTE_OFF = (byte) 0x80;
    private static final byte STATUS_NOTE_ON = (byte) 0x90;

    protected final MidiManager midiManager;
    private MidiManager.DeviceCallback callback = null;

    private enum MidiCommand {
        None,
        NoteOn,
        NoteOff,
    }
    private MidiCommand runningState = MidiCommand.None;
    private int midiChannel = 0;
    private MidiOutputPort openMidiOutputPort = null;
    private final Note[] midiNotes;
    private Note runningNote = null;

    public InputMidi(Application application) {
        super(application);

        if (isMidiAvailable()) {
            // do MIDI stuff
            this.midiManager = (MidiManager) application.getSystemService(Context.MIDI_SERVICE);
            initialiseMidi();
        }
        else {
            this.midiManager = null;
            Log.error("MIDI IS NOT ENABLED, NO MIDI SUPPORTED");
        }

        // map the notes to their MIDI index (middle C being 60)
        Notes notes = application.getNotes();
        // notes in MIDI are indexed from 0 to 127, create a nice lookup array for this purpose.
        this.midiNotes = new Note[128];
        // find middle C
        Note lastNote = notes.getNote("C4");
        this.midiNotes[60] = lastNote;
        int middleC = notes.getNoteIndex(this.midiNotes[60].getFrequency());
        // ok then, from here we can do down to zero
        int offset = 1;
        for (int i = 59; i >= 0 && offset <= middleC; --i) {
            // put the next note down in our array
            this.midiNotes[i] = notes.getNote(middleC - offset++);
            if (this.midiNotes[i].getFrequency() == lastNote.getFrequency()) {
                // this note is not a different frequency, this is the flat to the sharp
                // we added last time, ignore this by changing the index to try again
                ++i;
            }
            // remember the last note to compare against
            lastNote = this.midiNotes[i];
        }
        // also we can go up to 127
        offset = 1;
        lastNote = this.midiNotes[60];
        for (int i = 61; i < 128 && middleC + offset < notes.getNoteCount(); ++i) {
            // put the next note up in our array
            this.midiNotes[i] = notes.getNote(middleC + offset++);
            if (this.midiNotes[i].getFrequency() == lastNote.getFrequency()) {
                // this note is not a different frequency, this is the sharp to the flat
                // we added last time, ignore this by changing the index to try again
                --i;
            }
            // remember the last note to compare against
            lastNote = this.midiNotes[i];
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

    public void initialiseMidi() {
        // setup MIDI on this activity and listen for hot-plugins
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                null != this.midiManager &&
                null == this.callback) {
            this.callback = new MidiManager.DeviceCallback() {
                @Override
                public void onDeviceAdded(MidiDeviceInfo device) {
                    super.onDeviceAdded(device);
                    //TODO inform listeners of this discovery
                    Log.debug("MIDI Device " + GetMidiDeviceId(device) + " discovered");

                }
                @Override
                public void onDeviceRemoved(MidiDeviceInfo device) {
                    super.onDeviceRemoved(device);
                    //TODO inform listeners of this
                    Log.debug("MIDI Device " + GetMidiDeviceId(device) + " removed");
                }
            };
            this.midiManager.registerDeviceCallback(this.callback, new Handler(Looper.getMainLooper()));
        }
    }

    public void closeOpenMidiConnection() {
        if (null != openMidiOutputPort) {
            try {
                openMidiOutputPort.close();
                openMidiOutputPort = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
            //TODO inform the listners of this disconnection
        }
    }

    protected void openMidiDevice(MidiDeviceInfo item) {
        this.midiManager.openDevice(item, new MidiManager.OnDeviceOpenedListener() {
            @Override
            public void onDeviceOpened(MidiDevice midiDevice) {
                // once we have opened the device we will be good to go
                String deviceId = GetMidiDeviceId(midiDevice.getInfo());
                //TODO inform people we are connected to a MIDI device
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
                        // if here then it worked
                        isConnected = true;
                        break;
                    }
                }
            }
        }
        return isConnected;
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
                    Notes notes = this.application.getNotes();
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
                        informNoteDetection(this.runningNote, velocityToProbability > 40f, velocityToProbability);
                        break;
                    case NoteOff:
                        informNoteDetection(this.runningNote, false, velocityToProbability);
                        break;
                }
                // no note then, done it here
                this.runningNote = null;
            }
        }
    }

    private void informNoteDetection(Note note, boolean isPressed, float probability) {
        //TODO inform people that the note is interacted with
        if (note == null) {
            Log.error("NULL MIDI note depressed");
        }
        else if (isPressed) {
            Log.debug("MIDI note " + this.runningNote.getName() + " depressed with " + probability + " probability");
        }
        else {
            Log.debug("MIDI note " + this.runningNote.getName() + " released");
        }

    }
}

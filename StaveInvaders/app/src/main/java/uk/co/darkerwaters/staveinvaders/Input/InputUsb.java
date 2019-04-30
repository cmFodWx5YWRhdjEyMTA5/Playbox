package uk.co.darkerwaters.staveinvaders.Input;

import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputUsb extends InputMidi {

    private MidiDeviceInfo defaultUsbDevice = null;
    private List<MidiDeviceInfo> usbDevices = new ArrayList<MidiDeviceInfo>();
    private String activeConnectionId = "";

    public InputUsb(Application application) {
        super(application);

        // on creation we want to search for USB devices and connect to the default
        initialiseConnection();

        Log.debug("input type usb initialised");
    }

    private void initialiseConnection() {
        // setup our connection here, search for a device
        getConnectedUsbDevices();
        // this might have set a default device, has it?
        if (null != this.defaultUsbDevice) {
            // quick - connect to this, this will do the rest as when connected it will do a load
            // of different stuff
            connectToDevice(this.defaultUsbDevice);
        }
    }

    @Override
    public void shutdown() {
        Log.debug("input type usb shutdown");
        super.shutdown();
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
                        if (null == this.defaultUsbDevice || deviceId.equals(application.getSettings().getLastConnectedUsbDevice())) {
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

    public boolean connectToDevice(final MidiDeviceInfo item) {
        boolean isConnected = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && null != item) {
            // check our existing connection
            if (this.activeConnectionId.equals(GetMidiDeviceId(item))) {
                // this is connected already
                isConnected = true;
            }
            else {
                // things can go badly wrong in the depths of MIDI and BT so try/catch it
                try {
                    // store that we are trying a USB connection here
                    this.activeConnectionId = GetMidiDeviceId(item);
                    // and open the device
                    openMidiDevice(item);
                    // if here then it didn't throw - we ar connected
                    isConnected = true;
                } catch (Exception e) {
                    // inform the dev but just carry on and return out false
                    Log.error("Error connecting USB device", e);
                }
            }
        }
        return isConnected;
    }
}

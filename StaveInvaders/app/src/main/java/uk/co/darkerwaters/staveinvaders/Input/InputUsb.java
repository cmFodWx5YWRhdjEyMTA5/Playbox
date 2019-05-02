package uk.co.darkerwaters.staveinvaders.input;

import android.media.midi.MidiDeviceInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;

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
                    // if here then it didn't throw - we are connected
                    isConnected = true;
                    // inform listeners of this
                    synchronized (listeners) {
                        for (MidiListener listener : listeners) {
                            listener.midiDeviceConnectivityChanged(item, true);
                        }
                    }
                } catch (Exception e) {
                    // inform the dev but just carry on and return out false
                    Log.error("Error connecting USB device", e);
                }
            }
        }
        return isConnected;
    }

    @Override
    protected void onDeviceAdded(MidiDeviceInfo device) {
        //TODO MIDI device was added, we will connect to this straight away

    }

    @Override
    protected void onDeviceRemoved(MidiDeviceInfo device) {
        // MIDI device was removed, if this was what we are connected to then
        // we want to reconnect straight away if we can
        if (activeConnectionId.equals(GetMidiDeviceId(device))) {
            // just removed the USB device that we are using, stop using it
            closeOpenMidiConnection();
            // set the state to be keyboard now we disconnected
            this.application.getSettings().setActiveInput(Settings.InputType.keys);
            // try to reconnect as it might just be a little glitch
            getConnectedUsbDevices();
            if (null != this.defaultUsbDevice) {
                // quick - connect to this, this will do the rest as when connected it will do a load
                // of different stuff
                connectToDevice(this.defaultUsbDevice);
            }
        }
    }

    @Override
    public void closeOpenMidiConnection() {
        // remember the connection we are about to close
        String deviceDisconnected = this.activeConnectionId;
        // and close this connection
        super.closeOpenMidiConnection();
        // no active connection now
        this.activeConnectionId = new String();
        // inform the listners of this disconnection
        synchronized (this.listeners) {
            for (MidiListener listener : this.listeners) {
                listener.midiDeviceConnectionChanged(deviceDisconnected, false);
            }
        }
    }
}

package uk.co.darkerwaters.staveinvaders.input;

import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputUsb extends InputMidi {

    private MidiDeviceInfo defaultUsbDevice = null;
    private final List<MidiDeviceInfo> usbDevices = new ArrayList<>();
    private String activeConnectionId = "";

    private final List<UsbInputListener> listeners;

    public interface UsbInputListener {
        void usbDeviceConnectionClosed(String deviceDisconnected);
        void usbDeviceConnectionOpened(MidiDevice item);
    }

    public InputUsb(Application application) {
        super(application);

        // create the listening list
        this.listeners = new ArrayList<>();

        Log.debug("input type usb initialised");
    }

    @Override
    public int getStatusDrawable(InputSelector.Status status) {
        // if we are connected and all okay then return our icon
        if (status == InputSelector.Status.connected) {
            return R.drawable.ic_baseline_usb_24px;
        }
        // else let the base class do its thing
        return super.getStatusDrawable(status);
    }

    @Override
    public void initialiseConnection() {
        super.initialiseConnection();
        // setup our connection here, search for a device
        getConnectedUsbDevices();
        // this might have set a default device, has it?
        if (null != this.defaultUsbDevice) {
            // quick - connect to this, this will do the rest as when connected it will do a load
            // of different stuff
            connectToDevice(this.defaultUsbDevice);
        }
        else {
            // there is no device to connect to
            setStatus(InputSelector.Status.disconnected);
        }
    }

    public boolean addListener(UsbInputListener listener) {
        synchronized (listeners) {
            return listeners.add(listener);
        }
    }

    public boolean removeListener(UsbInputListener listener) {
        synchronized (listeners) {
            return listeners.remove(listener);
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
            // and find all the devices we can connect to
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
            return new ArrayList<>(this.usbDevices);
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
                // ensure we are connected then
                setStatus(InputSelector.Status.connected);
            }
            else {
                // things can go badly wrong in the depths of MIDI and BT so try/catch it
                try {
                    // this is connecting, so set our status
                    setStatus(InputSelector.Status.connecting);
                    // and open the device
                    openMidiDevice(item);
                    // if here then it didn't throw - we are connected
                    isConnected = true;
                    // remember this was the last connected device
                    application.getSettings().setLastConnectedUsbDevice(GetMidiDeviceId(item));
                    // ensure we are connected then
                    setStatus(InputSelector.Status.connected);
                } catch (Exception e) {
                    // inform the dev but just carry on and return out false
                    Log.error("Error connecting USB device", e);
                }
            }
        }
        return isConnected;
    }

    @Override
    protected void onMidiDeviceConnected(MidiDevice midiDevice) {
        super.onMidiDeviceConnected(midiDevice);
        // if here then we just connected a midi device, remember all this
        this.activeConnectionId = GetMidiDeviceId(midiDevice.getInfo());
        // this is connected, so set our status
        setStatus(InputSelector.Status.connected);
        // inform listeners of this
        synchronized (listeners) {
            for (UsbInputListener listener : listeners) {
                listener.usbDeviceConnectionOpened(midiDevice);
            }
        }
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
            // try to reconnect as it might just be a little glitch
            getConnectedUsbDevices();
            if (null != this.defaultUsbDevice) {
                // quick - connect to this, this will do the rest as when connected it will do a load
                // of different stuff
                connectToDevice(this.defaultUsbDevice);
            }
            else {
                // we have nothing to connect to
                setStatus(InputSelector.Status.disconnected);
            }
        }
    }

    @Override
    public void closeOpenMidiConnection() {
        // remember the connection we are about to close
        String deviceDisconnected = this.activeConnectionId;
        // this is disconnecting, so set our status
        setStatus(InputSelector.Status.disconnecting);
        // and close this connection
        super.closeOpenMidiConnection();
        // no active connection now
        this.activeConnectionId = "";
        // this is disconnected, so set our status
        setStatus(InputSelector.Status.disconnected);
        // inform the listeners of this disconnection
        synchronized (this.listeners) {
            for (UsbInputListener listener : listeners) {
                listener.usbDeviceConnectionClosed(deviceDisconnected);
            }
        }
    }
}

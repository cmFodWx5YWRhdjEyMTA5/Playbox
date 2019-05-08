package uk.co.darkerwaters.staveinvaders.actvities;

import android.content.res.Resources;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.actvities.handlers.UsbItemAdapter;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.input.Input;
import uk.co.darkerwaters.staveinvaders.input.InputMidi;
import uk.co.darkerwaters.staveinvaders.input.InputUsb;

public class UsbSetupActivity extends BaseSetupActivity implements
        UsbItemAdapter.MidiListListener,
        InputUsb.UsbInputListener {

    private Button detectButton;
    private TextView deviceLabel;
    private RecyclerView listView;

    private InputUsb inputUsb = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_setup);

        //initialise the base
        initialiseSetupActivity();

        this.deviceLabel = (TextView) findViewById(R.id.text_detected);
        this.listView = (RecyclerView) findViewById(R.id.usb_instrument_list);
        this.detectButton = (Button) findViewById(R.id.button_detect_usb);

        Resources r = getResources();
        int tenPixels = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()));

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        this.listView.setLayoutManager(mLayoutManager);
        //this.listView.addItemDecoration(new SelectableItemActivity.GridSpacingItemDecoration(1, tenPixels, true));
        this.listView.setItemAnimator(new DefaultItemAnimator());

        // when USB is being setup then we want to be sure it is the active connection type
        setInputToUsb();

        // now, if we are MIDI enabled, search for all the USB devices we can find and show them here
        if (null != this.inputUsb && this.inputUsb.isMidiAvailable()) {
            // do MIDI stuff
            this.detectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    discoverDevices();
                }
            });
            this.deviceLabel.setVisibility(View.GONE);
        }
        else {
            // disable MIDI stuff
            this.deviceLabel.setText(R.string.no_midi_available);
            this.detectButton.setEnabled(false);
        }
        // and discover all devices when we startup
        discoverDevices();
    }

    private void setInputToUsb() {
        this.application.getInputSelector().changeInputType(Settings.InputType.usb);
        Input activeInput = this.application.getInputSelector().getActiveInput();
        if (activeInput == null || false == activeInput instanceof InputUsb) {
            // there is no input
            Log.error("Active INPUT is not USB despite us setting it to be, it is " + activeInput);
        }
        else {
            this.inputUsb = (InputUsb) activeInput;
        }
    }

    private void discoverDevices() {
        // set no device detected first of all
        this.deviceLabel.setText(R.string.no_device);
        // get the connected devices
        List<MidiDeviceInfo> connectedDevices = this.inputUsb.getConnectedUsbDevices();
        // if there is one, get the default and connect to it here
        MidiDeviceInfo defaultUsbDevice = this.inputUsb.getDefaultUsbDevice();
        if (null != defaultUsbDevice && null != this.inputUsb) {
            // pretend like the user clicked on it to connect to it
            this.inputUsb.connectToDevice(defaultUsbDevice);
        }
        // show this list of devices
        this.listView.setAdapter(new UsbItemAdapter(connectedDevices, this.application,this));
        if (connectedDevices.size() == 0) {
            // try to help them
            this.deviceLabel.setVisibility(View.VISIBLE);
            this.deviceLabel.setText(R.string.midi_connect_help);
        }
        else {
            this.deviceLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMidiListItemClicked(MidiDeviceInfo item) {
        if (null != inputUsb) {
            // have clicked an item in the list, select this one then
            this.deviceLabel.setText(InputMidi.GetMidiDeviceId(item));
            // also connect to this device on our input
            this.inputUsb.connectToDevice(item);
            // also update the list view, the state of the item connected will have changed
            this.listView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // change to be USB for sure
        setInputToUsb();
        // also we need to listen to USB and MIDI messages
        if (null != this.inputUsb) {
            this.inputUsb.addListener(this);
            this.inputUsb.addMidiListener(this);
        }
        // and discover devices to refresh our list
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // just have a quick look here for a keyboard or whatever
                discoverDevices();
            }
        });
    }

    @Override
    protected void onPause() {
        // remove us as a listener
        if (null != this.inputUsb) {
            this.inputUsb.removeListener(this);
            this.inputUsb.removeMidiListener(this);
        }

        super.onPause();
    }


    @Override
    public void onMidiDeviceDiscovered(MidiDeviceInfo device) {
        // this changes our list, redo the list here
        discoverDevices();
    }

    @Override
    public void onMidiDeviceRemoved(MidiDeviceInfo device) {
        // this changes our list, redo the list here
        discoverDevices();
    }

    @Override
    public void onMidiDeviceOpened(MidiDeviceInfo device) {
        // detecting this from the USB, ignore
    }

    @Override
    public void onMidiDeviceConnected(MidiDeviceInfo device) {
        // detecting this from the USB, ignore
    }

    @Override
    public void usbDeviceConnectionClosed(String deviceDisconnected) {
        // this changes the status display, just update the display of devices
        this.listView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void usbDeviceConnectionOpened(MidiDevice item) {
        // this changes the status display, just update the display of devices
        this.listView.getAdapter().notifyDataSetChanged();
    }
}

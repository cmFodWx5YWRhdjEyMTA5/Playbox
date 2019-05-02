package uk.co.darkerwaters.staveinvaders.actvities;

import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.actvities.handlers.UsbItemAdapter;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.input.Input;
import uk.co.darkerwaters.staveinvaders.input.InputMidi;
import uk.co.darkerwaters.staveinvaders.input.InputUsb;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.views.PianoPlaying;
import uk.co.darkerwaters.staveinvaders.views.PianoTouchable;
import uk.co.darkerwaters.staveinvaders.views.PianoView;

public class UsbSetupActivity extends AppCompatActivity implements
        InputSelector.InputListener,
        PianoView.IPianoViewListener,
        UsbItemAdapter.MidiListListener {

    private PianoTouchable piano = null;
    private TextView pianoRangeText = null;
    private Button detectButton;
    private TextView deviceLabel;
    private RecyclerView listView;

    private float minPitchDetected = -1f;
    private float maxPitchDetected = -1f;

    private InputUsb inputUsb = null;

    private Application application;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_setup);
        // get the application for reference
        this.application = (Application)this.getApplication();

        this.piano = (PianoTouchable) findViewById(R.id.microphone_setup_piano);
        this.pianoRangeText = (TextView) findViewById(R.id.piano_range_text);
        this.deviceLabel = (TextView) findViewById(R.id.text_detected);
        this.listView = (RecyclerView) findViewById(R.id.usb_instrument_list);
        this.detectButton = (Button) findViewById(R.id.button_detect_usb);

        // show the range of the piano
        this.piano.setIsAllowTouch(false);
        this.pianoRangeText.setText(piano.getRangeText());

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
        this.listView.setAdapter(new UsbItemAdapter(connectedDevices, this));
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
        // add the listener back to the piano
        this.piano.addListener(this);
        // listen to changes in connection
        this.application.getInputSelector().addListener(this);
        // change to be USB for sure
        setInputToUsb();
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
        this.piano.removeListener(this);
        this.application.getInputSelector().removeListener(this);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        this.piano.closeView();
        super.onDestroy();
    }

    @Override
    public void onNoteDetected(Settings.InputType type, Chord chord, boolean isDetection, float probability) {
        // add to our range of notes we can detect
        if (isDetection && probability > 1f) {
            addDetectedPitch(chord);
            UsbSetupActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // invalidate the view to display it okay
                    piano.invalidate();
                    // show the range of the piano
                    UsbSetupActivity.this.pianoRangeText.setText(piano.getRangeText());
                }
            });
        }
    }

    private void addDetectedPitch(Chord chord) {
        if (null != chord) {
            float pitch = chord.getLowest().getFrequency();
            // add to the range of pitch we can detect
            if (minPitchDetected < 0 || pitch < minPitchDetected) {
                minPitchDetected = pitch;
            }
            pitch = chord.getHighest().getFrequency();
            if (maxPitchDetected < 0 || pitch > maxPitchDetected) {
                maxPitchDetected = pitch;
            }
            // depress this chord
            this.piano.depressNote(chord);
            // set the detected pitch on the piano we are showing
            this.piano.setNoteRange(minPitchDetected, maxPitchDetected, null);
        }
    }

    @Override
    public void pianoViewSizeChanged(int w, int h, int oldw, int oldh) {
        // interesting but don't really care
    }

    @Override
    public void noteReleased(Chord chord) {
        // invalidate the view, the piano released a note
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UsbSetupActivity.this.piano.invalidate();
            }
        });
    }

    @Override
    public void noteDepressed(Chord chord) {
        // interesting, this is from the piano so we don't really care
    }
}

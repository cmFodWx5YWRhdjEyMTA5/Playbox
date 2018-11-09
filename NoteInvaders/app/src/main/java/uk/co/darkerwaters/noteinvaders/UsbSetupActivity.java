package uk.co.darkerwaters.noteinvaders;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.State;
import uk.co.darkerwaters.noteinvaders.state.input.InputConnectionInterface;
import uk.co.darkerwaters.noteinvaders.state.input.InputMicrophone;
import uk.co.darkerwaters.noteinvaders.state.input.InputMidi;
import uk.co.darkerwaters.noteinvaders.views.PianoView;

public class UsbSetupActivity extends AppCompatActivity implements PianoView.IPianoViewListener, UsbItemAdapter.MidiListListener, InputMidi.MidiListener {

    private PianoView piano = null;
    private TextView pianoRangeText = null;
    private Button detectButton;
    private TextView deviceLabel;
    private RecyclerView listView;

    private float minPitchDetected = -1f;
    private float maxPitchDetected = -1f;

    private InputMidi inputMidi = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_setup);

        this.piano = (PianoView) findViewById(R.id.microphone_setup_piano);
        this.pianoRangeText = (TextView) findViewById(R.id.piano_range_text);
        this.deviceLabel = (TextView) findViewById(R.id.text_detected);
        this.listView = (RecyclerView) findViewById(R.id.usb_instrument_list);
        this.detectButton = (Button) findViewById(R.id.button_detect_usb);

        // show the range of the piano
        this.pianoRangeText.setText(piano.getRangeText());

        Resources r = getResources();
        int tenPixels = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()));

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        this.listView.setLayoutManager(mLayoutManager);
        //this.listView.addItemDecoration(new SelectableItemActivity.GridSpacingItemDecoration(1, tenPixels, true));
        this.listView.setItemAnimator(new DefaultItemAnimator());

        // create the input device
        this.inputMidi = new InputMidi(this);

        if (this.inputMidi.isMidiAvailable(this)) {
            // do MIDI stuff
            this.detectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    testDeviceDiscovery();
                }
            });
        }
        else {
            // disable MIDI stuff
            this.deviceLabel.setText(R.string.no_midi_available);
            this.detectButton.setEnabled(false);
        }

        testDeviceDiscovery();
    }

    private void testDeviceDiscovery() {
        // set no device detected first of all
        this.deviceLabel.setText(R.string.no_device);

        // get the connected devices
        List<MidiDeviceInfo> connectedDevices = this.inputMidi.getConnectedDevices();
        // show this list of devices
        this.listView.setAdapter(new UsbItemAdapter(connectedDevices, this));

        // and select the default if we can
        MidiDeviceInfo defaultDevice = this.inputMidi.getDefaultDevice();
        if (null != defaultDevice) {
            onMidiListItemClicked(defaultDevice);
        }
    }

    @Override
    public void midiDeviceConnectivityChanged(MidiDeviceInfo deviceInfo, boolean isConnected) {
        // just redo the list
        testDeviceDiscovery();
    }

    @Override
    public void onMidiListItemClicked(MidiDeviceInfo item) {
        // have clicked an item in the list, select this one then
        this.deviceLabel.setText(InputMidi.GetMidiDeviceId(item));
        State.getInstance().setMidiDeviceId(this, InputMidi.GetMidiDeviceId(item));
        // also connect to this device on our input
        this.inputMidi.connectToDevice(item);
        // add a listener
        this.inputMidi.addListener(new InputConnectionInterface() {
            @Override
            public void onNoteDetected(final Note note, final boolean isDetection, final float probability, final int frequency) {
                // add to our range of notes we can detect
                addDetectedPitch(note);
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
        });
        // also update the list view, the state of the item connected will have changed
        this.listView.getAdapter().notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // add the listener back to the piano
        this.piano.addListener(this);
        this.inputMidi.addListener(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // just have a quick look here for a keyboard or whatever
                testDeviceDiscovery();
            }
        });
    }

    @Override
    protected void onPause() {
        // remove us as a listener
        this.piano.removeListener(this);
        this.inputMidi.removeListener(this);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        this.piano.closeView();
        super.onDestroy();
    }

    private void addDetectedPitch(Note note) {
        float pitch = note.getFrequency();
        // add to the range of pitch we can detect
        if (minPitchDetected < 0 || pitch < minPitchDetected) {
            minPitchDetected = pitch;
        }
        if (maxPitchDetected < 0 || pitch > maxPitchDetected) {
            maxPitchDetected = pitch;
        }
        // depress this note
        this.piano.depressNote(note);
        // set the detected pitch on the piano we are showing
        this.piano.setNoteRange(minPitchDetected, maxPitchDetected);
    }

    @Override
    public void noteReleased(Note note) {
        // invalidate the view
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UsbSetupActivity.this.piano.invalidate();
            }
        });
    }

    @Override
    public void noteDepressed(Note note) {
        // interesting
    }

    @Override
    public void pianoViewSizeChanged(int w, int h, int oldw, int oldh) {
        // interesting
    }
}

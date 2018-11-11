package uk.co.darkerwaters.noteinvaders;

import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.State;
import uk.co.darkerwaters.noteinvaders.state.input.InputConnectionInterface;
import uk.co.darkerwaters.noteinvaders.state.input.InputMidi;
import uk.co.darkerwaters.noteinvaders.views.PianoView;

public class BtSetupActivity extends AppCompatActivity implements PianoView.IPianoViewListener, BtItemAdapter.BtListListener, InputMidi.MidiListener {

    private PianoView piano = null;
    private TextView pianoRangeText = null;
    private Button detectButton;
    private ProgressBar progressBar;
    private TextView deviceLabel;
    private RecyclerView listView;

    private float minPitchDetected = -1f;
    private float maxPitchDetected = -1f;

    private InputMidi inputMidi = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_setup);

        this.piano = (PianoView) findViewById(R.id.microphone_setup_piano);
        this.pianoRangeText = (TextView) findViewById(R.id.piano_range_text);
        this.deviceLabel = (TextView) findViewById(R.id.text_detected);
        this.listView = (RecyclerView) findViewById(R.id.usb_instrument_list);
        this.detectButton = (Button) findViewById(R.id.button_detect_usb);
        this.progressBar = (ProgressBar) findViewById(R.id.btScanProgressBar);

        // show the range of the piano
        this.pianoRangeText.setText(piano.getRangeText());

        Resources r = getResources();
        int tenPixels = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()));

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        this.listView.setLayoutManager(mLayoutManager);
        //this.listView.addItemDecoration(new SelectableItemActivity.GridSpacingItemDecoration(1, tenPixels, true));
        this.listView.setItemAnimator(new DefaultItemAnimator());
        // set the adapter
        this.listView.setAdapter(new BtItemAdapter(this));

        // create the input device
        this.inputMidi = new InputMidi(this);
        this.inputMidi.startConnection();
        this.progressBar.setVisibility(View.GONE);

        if (this.inputMidi.isMidiAvailable(this) && this.inputMidi.isBtAvailable(this)) {
            // do MIDI stuff
            this.detectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputMidi.isBtScanning()) {
                        // stop scanning
                        inputMidi.stopBluetoothScanning(null);
                    }
                    else {
                        // scan
                        discoverDevices();
                    }
                    showScanningStatus(inputMidi.isBtScanning());
                }
            });
        }
        // add a listener for MIDI input
        this.inputMidi.addListener(new InputConnectionInterface() {
            @Override
            public void onNoteDetected(Note note, final boolean isDetection, final float probability, final int frequency) {
                // add to our range of notes we can detect
                if (isDetection && probability > 1f) {
                    // add this pitch the piano range we are showing
                    addDetectedPitch(note);
                    // update the view of this piano
                    BtSetupActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // invalidate the view to display it okay
                            piano.invalidate();
                            // show the range of the piano
                            BtSetupActivity.this.pianoRangeText.setText(piano.getRangeText());
                        }
                    });
                }
            }
        });
        // and update the label
        showDeviceLabel();
    }

    private void showDeviceLabel() {
        if (false == this.inputMidi.isMidiAvailable(this)) {
            // show the MIDI cannot be done
            this.deviceLabel.setText(R.string.no_midi_available);
            this.detectButton.setEnabled(false);
        }
        else if (!this.inputMidi.isBtAvailable(this)) {
            // show that BTLE cannot be done
            this.deviceLabel.setText(R.string.no_ble_available);
            this.detectButton.setEnabled(false);
        }
        else if (this.inputMidi.isConnectionActive() || this.inputMidi.isBtScanning()) {
            // show the active connection
            deviceLabel.setText(this.inputMidi.getActiveMidiConnection());
            deviceLabel.setVisibility(View.GONE);
        }
        else {
            // show the help
            this.deviceLabel.setVisibility(View.VISIBLE);
            this.deviceLabel.setText(R.string.midi_ble_connect_help);
        }
    }

    private void showScanningStatus(boolean isScanning) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (inputMidi.isBtScanning()) {
                    // are scanning
                    progressBar.setVisibility(View.VISIBLE);
                    detectButton.setText(R.string.scan_bt_stop);
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    detectButton.setText(R.string.scan_bt);
                }
            }
        });

    }

    private void discoverDevices() {
        // scan for the devices to add to this adapter
        this.inputMidi.scanForBluetoothDevices(this);
        // and show the label properly now we are doing something
        showDeviceLabel();
    }

    @Override
    public void midiBtScanStatusChange(boolean isScanning) {
        // update the progress
        showScanningStatus(isScanning);
    }

    @Override
    public void midiBtDeviceDiscovered(final BluetoothDevice device) {
        // discovered a device, add it to the list and connect if it is what we are interested in
        BtItemAdapter adapter = (BtItemAdapter) listView.getAdapter();
        if (adapter.getItemCount() == 0) {
            // hide the label
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showDeviceLabel();
                }
            });
        }
        // discovered a device, add to the list
        adapter.addDevice(device);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case InputMidi.REQUEST_ENABLE_PERMISSIONS:
            case InputMidi.REQUEST_ENABLE_BT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, this would have been done from the user
                    // asking to scan for new devices, but didn't because no permissions.
                    // do that now then
                    discoverDevices();
                } else {
                    // permission denied, their choice though. don't do anything here
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showDeviceLabel();
                        }
                    });
                }
                break;
        }
    }

    @Override
    public void midiDeviceConnectivityChanged(MidiDeviceInfo deviceInfo, boolean isConnected) {
        // just inform the list of the change
        this.listView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void midiDeviceConnectionChanged(final String deviceId, boolean isConnected) {
        // show what we are connected to
        this.listView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onBtListItemClicked(BluetoothDevice item) {
        // have clicked an item in the list, select this one then
        State.getInstance().setMidiDeviceId(this, InputMidi.GetMidiDeviceId(item));
        // also connect to this device on our input
        this.inputMidi.connectToDevice(item);
        // also update the list view, the state of the item connected will have changed
        this.listView.getAdapter().notifyDataSetChanged();
        showDeviceLabel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // add the listener back to the piano
        this.piano.addListener(this);
        this.inputMidi.addListener(this);
    }

    @Override
    protected void onPause() {
        // remove us as a listener
        this.piano.removeListener(this);
        this.inputMidi.removeListener(this);
        // and let the base class pause
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // close it all
        this.piano.closeView();
        this.inputMidi.stopConnection();
        // and destroy the activity
        super.onDestroy();
    }

    private void addDetectedPitch(Note note) {
        if (null != note) {
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
    }

    @Override
    public void noteReleased(Note note) {
        // invalidate the view, the piano released a note
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BtSetupActivity.this.piano.invalidate();
            }
        });
    }

    @Override
    public void noteDepressed(Note note) {
        // interesting, this is from the piano so we don't really care
    }

    @Override
    public void pianoViewSizeChanged(int w, int h, int oldw, int oldh) {
        // interesting but don't really care
    }
}

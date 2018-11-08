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
import uk.co.darkerwaters.noteinvaders.views.PianoView;

public class UsbSetupActivity extends AppCompatActivity implements PianoView.IPianoViewListener, UsbItemAdapter.MidiListListener {

    private PianoView piano = null;
    private TextView pianoRangeText = null;
    private Button detectButton;
    private TextView deviceLabel;
    private RecyclerView listView;

    private float minPitchDetected = -1f;
    private float maxPitchDetected = -1f;

    private MidiManager midiManager = null;


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

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI) &&
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // do MIDI stuff
            this.midiManager = (MidiManager) getSystemService(Context.MIDI_SERVICE);
            initialiseMidi();
        }
        else {
            this.midiManager = null;
            this.deviceLabel.setText(R.string.no_midi_available);
            this.detectButton.setEnabled(false);
        }

        testDeviceDiscovery();
    }

    private void initialiseMidi() {
        // setup MIDI on this activity
        this.detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testDeviceDiscovery();
            }
        });
        // and listen for hot-plugins
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.midiManager.registerDeviceCallback(new MidiManager.DeviceCallback() {
                @Override
                public void onDeviceAdded(MidiDeviceInfo device) {
                    super.onDeviceAdded(device);
                    testDeviceDiscovery();
                }

                @Override
                public void onDeviceRemoved(MidiDeviceInfo device) {
                    super.onDeviceRemoved(device);
                    testDeviceDiscovery();
                }

                @Override
                public void onDeviceStatusChanged(MidiDeviceStatus status) {
                    super.onDeviceStatusChanged(status);
                    testDeviceDiscovery();
                }
            }, new Handler(Looper.getMainLooper()));
        }
    }

    private void testDeviceDiscovery() {
        // set no device detected first of all
        this.deviceLabel.setText(R.string.no_device);

        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            // got a device, show it
            this.deviceLabel.setText(device.getDeviceName());
        }

        List<MidiDeviceInfo> validDevices = new ArrayList<MidiDeviceInfo>();
        MidiDeviceInfo defaultDevice = null;
        if (null != this.midiManager && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            MidiDeviceInfo[] infos = this.midiManager.getDevices();
            for (MidiDeviceInfo info : infos) {
                if (info.getOutputPortCount() > 0) {
                    // this is valid
                    validDevices.add(info);
                    if (null == defaultDevice || getDeviceId(info).equals(State.getInstance().getMidiDeviceId())) {
                        defaultDevice = info;
                    }
                }
            }
        }
        // show this list of devices
        this.listView.setAdapter(new UsbItemAdapter(validDevices, this));
        if (null != defaultDevice) {
            onMidiListItemClicked(defaultDevice);
        }
    }

    @Override
    public void onMidiListItemClicked(final MidiDeviceInfo item) {
        // have clicked an item in the list, select this one then
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.deviceLabel.setText(getDeviceId(item));
            State.getInstance().setMidiDeviceId(this, getDeviceId(item));
            this.midiManager.openDevice(item, new MidiManager.OnDeviceOpenedListener() {
                @Override
                public void onDeviceOpened(MidiDevice midiDevice) {
                    for (MidiDeviceInfo.PortInfo port : midiDevice.getInfo().getPorts()) {
                        if (port.getType() == MidiDeviceInfo.PortInfo.TYPE_OUTPUT) {
                            // this is an output port - connect to this
                            MidiOutputPort midiOutputPort = midiDevice.openOutputPort(port.getPortNumber());
                            midiOutputPort.connect(new MidiReceiver() {
                                @Override
                                public void onSend(final byte[] data, int offset, int count, long timestamp) throws IOException {
                                    // have data!
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            deviceLabel.setText(new String(data));
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            }, new Handler(Looper.getMainLooper()));
        }
        this.listView.getAdapter().notifyDataSetChanged();
    }

    public static String getDeviceId(MidiDeviceInfo info) {
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

    @Override
    protected void onResume() {
        super.onResume();
        // add the listener back to the piano
        this.piano.addListener(this);
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

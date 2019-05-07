package uk.co.darkerwaters.staveinvaders.actvities;

import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.midi.MidiDeviceInfo;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.actvities.handlers.BtItemAdapter;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.input.Input;
import uk.co.darkerwaters.staveinvaders.input.InputBluetooth;

public class BtSetupActivity extends BaseSetupActivity implements
        BtItemAdapter.BtListListener,
        InputBluetooth.BluetoothListener {

    private Button detectButton;
    private ProgressBar progressBar;
    private TextView deviceLabel;
    private RecyclerView listView;

    private InputBluetooth inputBt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_setup);

        //initialise the base
        initialiseSetupActivity();

        this.deviceLabel = (TextView) findViewById(R.id.text_detected);
        this.listView = (RecyclerView) findViewById(R.id.usb_instrument_list);
        this.detectButton = (Button) findViewById(R.id.button_detect_usb);
        this.progressBar = (ProgressBar) findViewById(R.id.btScanProgressBar);

        Resources r = getResources();
        int tenPixels = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()));

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        this.listView.setLayoutManager(mLayoutManager);
        //this.listView.addItemDecoration(new SelectableItemActivity.GridSpacingItemDecoration(1, tenPixels, true));
        this.listView.setItemAnimator(new DefaultItemAnimator());
        // set the adapter
        this.listView.setAdapter(new BtItemAdapter(this));

        // when BT is being setup then we want to be sure it is the active connection type
        setInputToBt();

        this.progressBar.setVisibility(View.GONE);

        if (this.inputBt.isMidiAvailable() && this.inputBt.isBtAvailable(this)) {
            // do MIDI stuff
            this.detectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputBt.isBtScanning()) {
                        // stop scanning
                        inputBt.stopBluetoothScanning(null);
                    }
                    else {
                        // scan
                        discoverDevices();
                    }
                    showScanningStatus(inputBt.isBtScanning());
                }
            });
        }
        // and update the label
        showDeviceLabel();
    }

    private void setInputToBt() {
        this.application.getInputSelector().changeInputType(Settings.InputType.bt);
        Input activeInput = this.application.getInputSelector().getActiveInput();
        if (activeInput == null || false == activeInput instanceof InputBluetooth) {
            // there is no input
            Log.error("Active INPUT is not BT despite us setting it to be, it is " + activeInput);
        }
        else {
            this.inputBt = (InputBluetooth) activeInput;
        }
    }

    private void showDeviceLabel() {
        if (false == this.inputBt.isMidiAvailable()) {
            // show the MIDI cannot be done
            this.deviceLabel.setText(R.string.no_midi_available);
            this.detectButton.setEnabled(false);
        }
        else if (!this.inputBt.isBtAvailable(this)) {
            // show that BTLE cannot be done
            this.deviceLabel.setText(R.string.no_ble_available);
            this.detectButton.setEnabled(false);
        }
        else if (this.inputBt.isConnectionActive() || this.inputBt.isBtScanning()) {
            // show the active connection
            deviceLabel.setText(this.inputBt.getActiveMidiConnection());
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
                if (inputBt.isBtScanning()) {
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
        this.inputBt.scanForBluetoothDevices(this, true);
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
        // are we connected to any BT device?
        String activeBtConnection = this.inputBt.getActiveConnection();
        if (null == activeBtConnection || activeBtConnection.isEmpty()) {
            // there is no active BT connection, but we found something - connect to it
            onBtListItemClicked(device);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case InputBluetooth.REQUEST_ENABLE_PERMISSIONS:
            case InputBluetooth.REQUEST_ENABLE_BT:
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
    public void onMidiDeviceDiscovered(MidiDeviceInfo device) {
        // just inform the list of the change
        this.listView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onMidiDeviceRemoved(MidiDeviceInfo device) {
        // just inform the list of the change
        this.listView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onMidiDeviceOpened(MidiDeviceInfo device) {
        // just inform the list of the change
        this.listView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onMidiDeviceConnected(MidiDeviceInfo device) {
        // just inform the list of the change
        this.listView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onBtListItemClicked(BluetoothDevice item) {
        // have clicked an item in the list, select this one then
        this.application.getSettings().setLastConnectedBtDevice(InputBluetooth.GetMidiDeviceId(item));
        // also connect to this device on our input
        this.inputBt.connectToDevice(item);
        // also update the list view, the state of the item connected will have changed
        this.listView.getAdapter().notifyDataSetChanged();
        showDeviceLabel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // change to be BT for sure
        setInputToBt();
        // also we need to listen to USB and MIDI messages
        if (null != this.inputBt) {
            this.inputBt.addListener(this);
            this.inputBt.addMidiListener(this);
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
        if (null != this.inputBt) {
            this.inputBt.removeListener(this);
            this.inputBt.removeMidiListener(this);
        }

        super.onPause();
    }
}

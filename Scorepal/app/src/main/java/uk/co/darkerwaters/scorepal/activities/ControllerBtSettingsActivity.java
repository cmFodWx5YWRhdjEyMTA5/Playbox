package uk.co.darkerwaters.scorepal.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderRemoteButton;
import uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler;
import uk.co.darkerwaters.scorepal.activities.handlers.RemoteButtonRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.application.RemoteButton;
import uk.co.darkerwaters.scorepal.controllers.KeyController;

import static uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler.MY_PERMISSIONS_REQUEST_BLUETOOTH;

public class ControllerBtSettingsActivity extends ListedActivity {

    /*
    private static final int REQUEST_ENABLE_BT = 123;
    private PermissionHandler permissionHandler = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private BroadcastReceiver bluetoothDeviceReceiver = null;
    private boolean isScanningBle = false;
    */
    private RemoteButtonRecyclerAdapter buttonListAdapter;
    private View.OnKeyListener onKeyListener;
    private View connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_bt_settings);

        ViewGroup mainLayout = findViewById(R.id.main_layout);

        // create the key listener to listen for all the inputs we can find
        this.onKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                boolean isProcess = true;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_HOME:
                        isProcess = false;
                        // never use these
                        break;
                }
                boolean isInterested = false;
                if (isProcess) {
                    Log.info(Character.getName(keyCode));
                    RemoteButton button = buttonListAdapter.getButton(keyCode);
                    if (null == button) {
                        // create a button for this
                        button = new RemoteButton(keyCode);
                        buttonListAdapter.addButton(button);
                    }
                    int index = buttonListAdapter.getButtonPosition(button);
                    if (index != -1) {
                        recyclerView.smoothScrollToPosition(index);
                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(index);
                        if (holder instanceof CardHolderRemoteButton) {
                            ((CardHolderRemoteButton)holder).highlightButton();
                        }
                    }
                    Log.info("key: " + Character.getName(keyCode) + " " + keyEvent.getSource());
                    // and don't pass this on to anything else
                    isInterested = true;
                }
                // only let the base have this if we don't want it
                return isInterested;
            }
        };

        // set the top level listener
        mainLayout.setOnKeyListener(this.onKeyListener);
        // need to set the key listener on all the views on this activity to interecept everything
        KeyController.setKeyListener(mainLayout, this.onKeyListener);

        this.connectButton = findViewById(R.id.connectButton);
        RemoteButton[] remoteButtons = this.application.getSettings().getRemoteButtons();
        // create the list adapter to show this button setup
        this.buttonListAdapter = new RemoteButtonRecyclerAdapter(remoteButtons, this, this.onKeyListener);
        setupRecyclerView(R.id.buttonRecyclerView, this.buttonListAdapter);

        /*
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Phone does not support Bluetooth so let the user know and exit.
        if (this.bluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.bt_not_compatable)
                    .setMessage(R.string.bt_not_compatable_explan)
                    .setNeutralButton(R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            showBleDisabled();
        }
        else {
            // setup bt
            requestBleDevices();
        }*/
    }

    @Override
    protected void onPause() {
        // put the arrangement of buttons back onto the settings
        application.getSettings().setRemoteButtons(this.buttonListAdapter.getButtons());
        // and pause the activity
        super.onPause();
    }

    /*
    @Override
    protected void onDestroy() {
        cancelBleScanning();
        super.onDestroy();
    }
    */

    /*
    private void requestBleDevices() {
        // we need to be sure to have permission to access bluetooth here
        this.permissionHandler = new PermissionHandler(this,
                R.string.bluetooth_access_explanation,
                MY_PERMISSIONS_REQUEST_BLUETOOTH,
                new String[] {
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                new PermissionHandler.PermissionsHandlerConstructor() {
                    @Override
                    public boolean getIsRequestPermission() {
                        return true;
                    }
                    @Override
                    public void onPermissionsDenied(String[] permissions) {
                        showBleDisabled();
                    }
                    @Override
                    public void onPermissionsGranted(String[] permissions) {
                        setupBleDevices();
                    }
                });
        // check / request access to contacts and setup the editing controls accordingly
        this.permissionHandler.requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // pass this message to our handler
        if (!this.permissionHandler.processPermissionsResult(requestCode, permissions, grantResults)) {
            // the handler didn't do anything, pass it on
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showBleDisabled() {

    }

    private void setupBleDevices() {
        if (!this.bluetoothAdapter.isEnabled()) {
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_ENABLE_BT);
        }
        else {
            // scan for devices
            scanForBleDevices();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            // this is the response for our BT enabled response
            if (resultCode == RESULT_OK) {
                scanForBleDevices();
            }
            else {
                showBleDisabled();
            }
        }
        // pass to the base
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanForBleDevices() {
        // add the already discovered devices first
        for (BluetoothDevice device : this.bluetoothAdapter.getBondedDevices()) {
            onBondedDeviceDiscovered(device);
        }
        if (null == this.bluetoothDeviceReceiver) {
            this.bluetoothDeviceReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // get the device
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        // and process it
                        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                            onUnbondedDeviceDiscovered(device);
                        }
                        else {
                            onBondedDeviceDiscovered(device);
                        }
                    }
                    else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                        // we hopefully just bonded with this
                        int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                        int previousState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (state == BluetoothDevice.BOND_BONDED && previousState == BluetoothDevice.BOND_BONDING) {
                            connectToDevice(device);
                        }
                    }
                }
            };
            // register this with a filter
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(this.bluetoothDeviceReceiver, filter);
        }
        // start scanning
        this.bluetoothAdapter.startDiscovery();
        // set that we are scanning
        this.isScanningBle = true;
    }

    private void cancelBleScanning() {
        if (null != this.bluetoothDeviceReceiver) {
            // unregister this
            unregisterReceiver(this.bluetoothDeviceReceiver);
            // and don't use it now it isn't registered
            this.bluetoothDeviceReceiver = null;
        }
        if (isScanningBle) {
            // cancel any open discoveries
            if (this.bluetoothAdapter.isDiscovering()) {
                this.bluetoothAdapter.cancelDiscovery();
            }
            this.isScanningBle = false;
        }
    }

    private void onUnbondedDeviceDiscovered(BluetoothDevice bluetoothDevice) {
        // put in a list to bond with
    }

    private void onBondedDeviceDiscovered(BluetoothDevice bluetoothDevice) {
        // put in a list to bond with
    }

    private void connectToDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            Log.debug("Start Pairing... with: " + bluetoothDevice.getName());
            bluetoothDevice.createBond();
        }
        else {
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        }
    }
    */
}

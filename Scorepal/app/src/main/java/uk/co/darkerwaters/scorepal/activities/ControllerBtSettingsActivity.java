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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler;
import uk.co.darkerwaters.scorepal.application.Log;

import static uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler.MY_PERMISSIONS_REQUEST_BLUETOOTH;

public class ControllerBtSettingsActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 123;

    private PermissionHandler permissionHandler = null;

    private BluetoothAdapter bluetoothAdapter = null;
    private BroadcastReceiver bluetoothDeviceReceiver = null;
    private boolean isScanningBle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller_bt_settings);

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
        }
    }

    @Override
    protected void onDestroy() {
        cancelBleScanning();
        super.onDestroy();
    }

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.info(Character.getName(keyCode));
        // only let the base have this if we don't want it
        return super.onKeyDown(keyCode, event);
    }
}

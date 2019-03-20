package uk.co.darkerwaters.smspeak;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class BluetoothConnection extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        // decode this action so we know when something is connected or not
        switch(intent.getAction()) {
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                // connection state changed
                State state = State.GetInstance(context);
                int connectionState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (null != bluetoothDevice) {
                    // set the state of this connection
                    state.setConnectedBtDevice(bluetoothDevice.getName(), connectionState == BluetoothAdapter.STATE_CONNECTED);
                }
                break;

        }
    }
}

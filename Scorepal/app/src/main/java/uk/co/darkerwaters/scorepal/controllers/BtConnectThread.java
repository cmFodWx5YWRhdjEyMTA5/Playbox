package uk.co.darkerwaters.scorepal.controllers;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import uk.co.darkerwaters.scorepal.application.Log;

public class BtConnectThread {

    private BluetoothSocket bluetoothSocket;

    private Thread processingThread = null;
    private volatile boolean isProcessData = true;

    public interface BtDataListener {
        void onBtDataReceived(int data);
    }

    public boolean connect(BluetoothDevice bTDevice, UUID mUUID) {
        this.bluetoothSocket = null;
        try {
            this.bluetoothSocket = bTDevice.createRfcommSocketToServiceRecord(mUUID);
        } catch (IOException e) {
            Log.debug("Could not create RFCOMM socket:" + e.toString());
            return false;
        }
        try {
            if (null != this.bluetoothSocket) {
                bluetoothSocket.connect();
            }
        } catch(IOException e) {
            Log.debug("Could not connect: " + e.toString());
            try {
                bluetoothSocket.close();
            } catch(IOException close) {
                Log.debug("Could not close connection:" + e.toString());
                return false;
            }
        }
        return true;
    }

    private void processDataInThread() {
    }

    public void listenForData(final BtConnectThread.BtDataListener listener) {
        if (null != this.processingThread) {
            this.isProcessData = true;
            this.processingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int data;
                    while (isProcessData) {
                        try {
                            data = receiveData(bluetoothSocket);
                            listener.onBtDataReceived(data);
                        } catch (IOException e) {
                            Log.error("failed to read", e);
                            break;
                        }
                    }
                }
            });
            this.processingThread.start();
        }
    }

    public boolean cancel() {
        this.isProcessData = false;
        try {
            bluetoothSocket.close();
        } catch(IOException e) {
            Log.debug("Could not close connection:" + e.toString());
            return false;
        }
        return true;
    }

    public void sendData(BluetoothSocket socket, int data) throws IOException{
        ByteArrayOutputStream output = new ByteArrayOutputStream(4);
        output.write(data);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(output.toByteArray());
    }

    private int receiveData(BluetoothSocket socket) throws IOException{
        byte[] buffer = new byte[4];
        ByteArrayInputStream input = new ByteArrayInputStream(buffer);
        InputStream inputStream = socket.getInputStream();
        inputStream.read(buffer);
        return input.read();
    }
}

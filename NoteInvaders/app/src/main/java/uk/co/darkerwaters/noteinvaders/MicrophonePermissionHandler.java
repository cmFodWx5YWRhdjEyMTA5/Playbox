package uk.co.darkerwaters.noteinvaders;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class MicrophonePermissionHandler {
    private final int MY_PERMISSIONS_RECORD_AUDIO = 21;

    public interface MicrophonePermissionListener {
        void onAudioPermissionChange(boolean isPermissionGranted);
    }

    private MicrophonePermissionListener listener;

    public MicrophonePermissionHandler(MicrophonePermissionListener listener, Activity context) {
        this.listener = listener;
    }

    public void close() {
        this.listener = null;
    }

    public void initialiseAudioPermissions(Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.RECORD_AUDIO)) {
                // need to show the user the rationale behind asking
                Toast.makeText(context, "Cannot use the microphone to listen to your music without permission", Toast.LENGTH_LONG).show();
                showAudioPermissionRequest(context);

            } else if (this.listener != null) {
                // are not supposed to ask any more, just inform the listener
                this.listener.onAudioPermissionChange(false);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            // permission is granted, inform the listener
            if (null != this.listener) {
                this.listener.onAudioPermissionChange(true);
            }
        }
    }

    public void showAudioPermissionRequest(Activity context) {
        // Show user dialog to grant permission to record audio
        ActivityCompat.requestPermissions(context,
                new String[]{Manifest.permission.RECORD_AUDIO},
                MY_PERMISSIONS_RECORD_AUDIO);
    }

    public void handlePermissionsRequest(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    if (null != this.listener) {
                        this.listener.onAudioPermissionChange(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    if (null != this.listener) {
                        this.listener.onAudioPermissionChange(false);
                    }
                }
            }
        }
    }
}

package uk.co.darkerwaters.noteinvaders;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class MicrophoneSetupActivity extends AppCompatActivity implements PianoView.IPianoViewListener {

    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private View microphonePermissionPanel = null;
    private PianoView piano = null;
    private TextView microphoneResponseText = null;
    private TextView pianoRangeText = null;
    private ProgressBar microphoneLevel = null;
    private MicrophoneLevelMonitor microphoneLevelMonitor = null;
    private MicrophoneNotesDetector microphoneNotesDetector = null;

    private final static float K_NOTE_DETECTION_PROBABIILITY_THRESHOLD = 0.8f;
    private final static int K_NOTE_DETECTION_FREQUENCY_THRESHOLD = 3;

    private float minPitchDetected = -1f;
    private float maxPitchDetected = -1f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microphone_setup);

        this.microphonePermissionPanel = findViewById(R.id.layout_microphone_permission);
        this.microphoneLevel = (ProgressBar) findViewById(R.id.progress_microphone);
        this.piano = (PianoView) findViewById(R.id.microphone_setup_piano);
        this.microphoneResponseText = (TextView) findViewById(R.id.microphone_response_text);
        this.microphoneResponseText.setVisibility(View.GONE);
        this.pianoRangeText = (TextView) findViewById(R.id.piano_range_text);

        Button microphonePermissionButton = (Button) findViewById(R.id.button_microphone_permission);
        microphonePermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // allow the user to request permissions again
                requestAudioPermissions();
            }
        });
        // and do it for them on showing this panel
        requestAudioPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // start the monitoring on the UI thread else the note pitch detection doesn't work
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // start monitoring audio
                startAudioMonitoring();
                // show the range of the piano
                MicrophoneSetupActivity.this.pianoRangeText.setText(piano.getRangeText());
            }
        });
        this.piano.addListener(this);
    }

    @Override
    protected void onPause() {
        // kill the recorder we are using to get the level
        if (null != microphoneLevelMonitor) {
            // stop monitoring the levels
            this.microphoneLevelMonitor.stop();
            this.microphoneLevelMonitor = null;
        }
        if (null != this.microphoneNotesDetector) {
            // and detecting the notes
            this.microphoneNotesDetector.stop();
            this.microphoneNotesDetector = null;
        }
        this.piano.removeListener(this);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        this.piano.closeView();
        super.onDestroy();
    }

    private void startAudioMonitoring() {
        // monitor the level for a nice display of the microphone working
        microphoneLevelMonitor = new MicrophoneLevelMonitor(new MicrophoneLevelMonitor.IMicrophoneLevelListener() {
            @Override
            public void onMicrophoneLevel(final int maxAplitudePercent) {
                MicrophoneSetupActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MicrophoneSetupActivity.this.microphoneResponseText.setVisibility(View.GONE);
                        MicrophoneSetupActivity.this.microphoneLevel.setProgress(maxAplitudePercent);
                    }
                });
            }
        });
        // also start detecting the notes
        microphoneNotesDetector = new MicrophoneNotesDetector(MicrophoneSetupActivity.this);
        // add a listener
        microphoneNotesDetector.addListener(new MicrophoneNotesDetector.NoteDetectionInterface() {
            @Override
            public void onNoteDetected(final String name, float pitch, final float probability, int frequency, boolean isPitched) {
                // show that the microphone is working
                MicrophoneSetupActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MicrophoneSetupActivity.this.microphoneLevel.setProgress((int)(probability * 100.0));
                        MicrophoneSetupActivity.this.microphoneResponseText.setVisibility(View.VISIBLE);
                        if (probability > 0.4) {
                            MicrophoneSetupActivity.this.microphoneResponseText.setText(name);
                        }
                        else {
                            MicrophoneSetupActivity.this.microphoneResponseText.setText("--");
                        }
                    }
                });
                if (probability > K_NOTE_DETECTION_PROBABIILITY_THRESHOLD && frequency > K_NOTE_DETECTION_FREQUENCY_THRESHOLD) {
                    // exceeded thresholds for detection, add to our range of notes we can detect
                    addDetectedPitch(pitch);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // invalidate the view to display it okay
                        piano.invalidate();
                        // show the range of the piano
                        MicrophoneSetupActivity.this.pianoRangeText.setText(piano.getRangeText());
                    }
                    });
            }
        });
        if (false == microphoneNotesDetector.start()) {
            // failed to start the note detector, start the microphone detector instead
            if (false == this.microphoneLevelMonitor.start()) {
                // failed to start that too
                this.microphonePermissionPanel.setVisibility(View.VISIBLE);
            } else {
                // started the microphone detector
                this.microphonePermissionPanel.setVisibility(View.GONE);
            }
        } else {
            // have permission and it worked, hide that panel
            this.microphonePermissionPanel.setVisibility(View.GONE);
        }
    }

    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            //Go ahead with recording audio now
            startAudioMonitoring();
        }
    }

    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    startAudioMonitoring();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void addDetectedPitch(float pitch) {
        // add to the range of pitch we can detect
        if (minPitchDetected < 0 || pitch < minPitchDetected) {
            minPitchDetected = pitch;
        }
        if (maxPitchDetected < 0 || pitch > maxPitchDetected) {
            maxPitchDetected = pitch;
        }
        // depress this note
        this.piano.depressNote(Notes.instance().getNote(pitch));
        // set the detected pitch on the piano we are showing
        this.piano.setNoteRange(minPitchDetected, maxPitchDetected);
    }

    @Override
    public void noteReleased(Note note) {
        // invalidate the view
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MicrophoneSetupActivity.this.piano.invalidate();
            }
        });
    }
}

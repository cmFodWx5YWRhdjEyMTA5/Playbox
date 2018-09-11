package uk.co.darkerwaters.noteinvaders;

import android.media.MediaRecorder;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class MicrophoneLevelMonitor {

    private MediaRecorder recorder = null;
    private Timer recordingTimer = null;

    private boolean isStarted = false;
    public interface IMicrophoneLevelListener {
        public void onMicrophoneLevel(int maxAmplitudePercent);
    }
    private final IMicrophoneLevelListener listener;
    private static final double K_MAXMAXAMPLITUDE = 32767.0;

    public MicrophoneLevelMonitor(IMicrophoneLevelListener listener) {
        this.listener = listener;
    }

    public void stop() {
        // kill the recorder we are using to get the level
        if (null != this.recordingTimer) {
            this.recordingTimer.cancel();
            this.recordingTimer = null;
        }
        if (null != this.recorder) {
            this.recorder.stop();
            this.recorder = null;
        }
        this.isStarted = false;
    }

    public boolean start() {
        if (null == this.recorder) {
            // create the recorder
            this.recorder = new MediaRecorder();
            this.recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            this.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // set a null output file to not save anything
            this.recorder.setOutputFile("/dev/null");
            try {
                this.recorder.prepare();
                this.recorder.start();
            } catch (Exception e) {
                // failed
                e.printStackTrace();
                this.recorder = null;
            }
        }
        if (null != this.recorder) {
            // created and started the recorder ok
            if (null == this.recordingTimer) {
                this.recordingTimer = new Timer();
                this.recordingTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        int amplitude = (int)((MicrophoneLevelMonitor.this.recorder.getMaxAmplitude() / K_MAXMAXAMPLITUDE) * 100.0);
                        MicrophoneLevelMonitor.this.listener.onMicrophoneLevel(amplitude);
                    }
                }, 0, 100);
            }
            this.isStarted = true;
        }
        return null != this.recordingTimer;
    }
}

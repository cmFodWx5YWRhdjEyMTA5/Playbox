package uk.co.darkerwaters.noteinvaders.state.input;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;

public class InputMicrophone extends InputConnection {

    private AudioDispatcher dispatcher;
    private Thread detectionThread = null;
    private boolean isThreadStarted = false;

    public InputMicrophone(Activity context) {
        super(context);
    }

    public final static float K_NOTE_DETECTION_PROBABIILITY_THRESHOLD = 0.9f;
    public final static int K_NOTE_DETECTION_FREQUENCY_THRESHOLD = 3;

    @Override
    public void initialiseConnection() {
        super.initialiseConnection();
        // ensure we have access to the microphone for this detector
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.RECORD_AUDIO},
                    123);
        }
    }

    @Override
    public boolean stopConnection() {
        // stop it all
        if (this.isThreadStarted) {
            this.isThreadStarted = false;
            this.dispatcher.stop();
        }
        return !this.isThreadStarted;
    }

    @Override
    public boolean startConnection() {
        // start everything up
        if (false == this.isThreadStarted) {
            PitchDetectionHandler pdh = new PitchDetectionHandler() {
                @Override
                public void handlePitch(final PitchDetectionResult result, AudioEvent e) {
                    final float pitchInHz = result.getPitch();
                    if (pitchInHz > 0) {
                        // get the name of the note
                        Note note = Notes.instance().getNote(pitchInHz);
                        if (null != note) {
                            // increase the frequency of this
                            int noteFreq = incrementNoteFrequency(note);
                            // and inform the listeners of the change
                            informNoteDetection(note, result.getProbability(), noteFreq, result.isPitched());
                        }
                    } else {
                        // no pitch detected - clear it all out now
                        clearNoteFrequencyData(true);
                    }
                }
            };
            // start it all up
            try {
                this.dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
                AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
                this.dispatcher.addAudioProcessor(p);
                // create the thread to perform the processing and start it
                this.detectionThread = new Thread(dispatcher, "Audio Dispatcher");
                this.detectionThread.start();
                // return that this works I guess
                this.isThreadStarted = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.isThreadStarted;
    }
}
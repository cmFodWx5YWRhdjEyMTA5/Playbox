package uk.co.darkerwaters.noteinvaders.state.input;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.HashMap;
import java.util.Map;

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

    private final Map<Note, Integer> noteDetectionFrequency;

    private Thread reductionThread = null;
    private final Object waitingObject = new Object();

    public InputMicrophone(Activity context) {
        super(context);
        this.noteDetectionFrequency = new HashMap<Note, Integer>(Notes.instance().getNoteCount());
    }

    private final static float K_NOTE_DETECTION_PROBABIILITY_THRESHOLD = 0.9f;
    private final static int K_NOTE_DETECTION_FREQUENCY_THRESHOLD = 10;
    private final static int K_NOTEREDUCTIONDELAY = 25;

    @Override
    public void initialiseConnection() {
        super.initialiseConnection();
        Notes notes = Notes.instance();
        for (int i = 0; i < notes.getNoteCount(); ++i) {
            // put all the notes into the map and the detection frequency of zero
            Note note = notes.getNote(i);
            synchronized (this.noteDetectionFrequency) {
                this.noteDetectionFrequency.put(note, new Integer(0));
            }
        }
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
        synchronized (this.waitingObject) {
            this.waitingObject.notifyAll();
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
                        if (null != note && result.getProbability() > K_NOTE_DETECTION_PROBABIILITY_THRESHOLD) {
                            // this is a hit, record this
                            boolean isNewHit;
                            synchronized (InputMicrophone.this.noteDetectionFrequency) {
                                // is this a new hit
                                isNewHit = InputMicrophone.this.noteDetectionFrequency.get(note) == 0;
                                // set this to the detection count
                                InputMicrophone.this.noteDetectionFrequency.put(note, K_NOTE_DETECTION_FREQUENCY_THRESHOLD);
                            }
                            if (isNewHit) {
                                // exceeded thresholds for detection, inform the music view we detected this
                                informNoteDetection(note, true, result.getProbability(), K_NOTE_DETECTION_FREQUENCY_THRESHOLD);
                            }
                        }
                    } else {
                        // no pitch detected - clear it all out now
                        //clearNoteFrequencyData(true);
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
                return startReductionThread();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.isThreadStarted;
    }

    protected boolean startReductionThread() {
        // start up the reduction thread this class needs to diminish the detection frequencies
        this.reductionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isThreadStarted) {
                    synchronized (InputMicrophone.this.noteDetectionFrequency) {
                        for (Map.Entry<Note, Integer> noteFreq : InputMicrophone.this.noteDetectionFrequency.entrySet()) {
                            Integer value = noteFreq.getValue();
                            if (value > 0) {
                                value = value - 1;
                                noteFreq.setValue(value);
                                if (value == 0) {
                                    // inform the listeners of this change
                                    informNoteDetection(noteFreq.getKey(), false, -1f, 0);
                                }
                            }
                        }
                    }
                    try {
                        synchronized (InputMicrophone.this.waitingObject) {
                            InputMicrophone.this.waitingObject.wait(K_NOTEREDUCTIONDELAY);
                        }
                    } catch (InterruptedException e) {
                        // fine, will exit the thread
                    }
                }
            }
        });
        // start the reducer thread
        this.reductionThread.start();
        this.isThreadStarted = true;
        // return if the thread was started ok
        return this.isThreadStarted;
    }

    protected int incrementNoteFrequency(Note note) {
        int noteFreq = -1;
        synchronized (this.noteDetectionFrequency) {
            // get the frequency and add one
            noteFreq = this.noteDetectionFrequency.get(note) + 1;
            // put the new value into the map
            this.noteDetectionFrequency.put(note, noteFreq);
        }
        return noteFreq;
    }

    protected boolean clearNoteFrequencyData(boolean isInformListeners) {
        boolean isFrequencyChanged = false;
        // go through the map and set all the data to zero, return if any wasn't zero in the first place
        synchronized (this.noteDetectionFrequency) {
            for (Map.Entry<Note, Integer> noteFreq : this.noteDetectionFrequency.entrySet()) {
                Integer value = noteFreq.getValue();
                if (value > 0) {
                    // this is a change
                    isFrequencyChanged = false;
                    // reset the value
                    value = 0;
                    noteFreq.setValue(value);
                    if (isInformListeners) {
                        // inform the listeners
                        informNoteDetection(noteFreq.getKey(), false, -1f, 0);
                    }
                }
            }
        }
        // return if something is different
        return isFrequencyChanged;
    }
}
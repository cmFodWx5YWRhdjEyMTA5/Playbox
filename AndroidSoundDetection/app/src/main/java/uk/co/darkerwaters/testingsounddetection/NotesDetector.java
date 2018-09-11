package uk.co.darkerwaters.testingsounddetection;

import android.Manifest;
import android.app.Activity;
import android.content.Entity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class NotesDetector {

    public interface NoteDectectionInterface {
        public void onNoteDetected(String name, float pitch, float probability, int frequency);
    }

    private final Activity context;
    private AudioDispatcher dispatcher;
    private Notes notes;
    private final Map<String, Integer> noteDetectionFrequency;
    private Thread detectionThread = null;
    private Thread reductionThread = null;

    private boolean isThreadStarted = false;
    private volatile boolean isStopThread = false;
    private final Object waitingObject = new Object();

    private final List<NoteDectectionInterface> listeners;

    public NotesDetector(Activity context) {
        this.context = context;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.RECORD_AUDIO},
                    123);
        }

        this.notes = new Notes(context);
        this.listeners = new ArrayList<NoteDectectionInterface>();
        this.noteDetectionFrequency = new HashMap<String, Integer>(this.notes.getNoteCount());
        for (String name : this.notes.getNotesNames()) {
            this.noteDetectionFrequency.put(name, new Integer(0));
        }
    }

    public String[] getNotesNames() {
        return this.notes.getNotesNames(-1);
    }

    public String[] getNotesNames(int nameIndex) {
        return this.notes.getNotesNames(nameIndex);
    }

    public boolean addListener(NoteDectectionInterface listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(NoteDectectionInterface listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    public void stop() {
        // stop it
        if (this.isThreadStarted) {
            this.isThreadStarted = false;
            this.dispatcher.stop();
            // and our thread while we are here
            this.isStopThread = true;
            synchronized (this.waitingObject) {
                this.waitingObject.notifyAll();
            }
        }
    }

    public void start() {
        // start everything up
        if (false == this.isThreadStarted) {
            this.isThreadStarted = true;
            // start the reduction thread to decrement the numbers in the list of frequencies
            this.reductionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (false == isStopThread) {
                        synchronized (NotesDetector.this.noteDetectionFrequency) {
                            for (Map.Entry<String, Integer> noteFreq : NotesDetector.this.noteDetectionFrequency.entrySet()) {
                                Integer value = noteFreq.getValue();
                                if (value > 0) {
                                    value = value - 1;
                                    noteFreq.setValue(value);
                                    // inform the listeners of this change
                                    synchronized (NotesDetector.this.listeners) {
                                        String noteName = noteFreq.getKey();
                                        for (NoteDectectionInterface listener : NotesDetector.this.listeners) {
                                            listener.onNoteDetected(noteName, -1f, -1f, value);
                                        }
                                    }
                                }
                            }
                        }
                        try {
                            synchronized (NotesDetector.this.waitingObject) {
                                NotesDetector.this.waitingObject.wait(500);
                            }
                        } catch (InterruptedException e) {
                            // fine, will exit the thread
                        }
                    }
                }
            });
            // start the reducer thread
            //this.reductionThread.start();

            PitchDetectionHandler pdh = new PitchDetectionHandler() {
                @Override
                public void handlePitch(final PitchDetectionResult result, AudioEvent e) {
                    final float pitchInHz = result.getPitch();
                    if (pitchInHz > 0) {
                        if (result.getProbability() > 0.9 && result.isPitched()) {
                            // get the name of the note
                            String noteName = notes.getNote(pitchInHz);
                            Integer noteFreq;
                            // increase the frequency of this
                            synchronized (NotesDetector.this.noteDetectionFrequency) {
                                noteFreq = NotesDetector.this.noteDetectionFrequency.get(noteName) + 1;
                                NotesDetector.this.noteDetectionFrequency.put(noteName, noteFreq);
                            }
                            // inform the listeners of this change
                            synchronized (NotesDetector.this.listeners) {
                                for (NoteDectectionInterface listener : NotesDetector.this.listeners) {
                                    listener.onNoteDetected(noteName, pitchInHz, result.getProbability(), noteFreq);
                                }
                            }
                        }
                    } else {
                        // no pitch detected - clear it all out now
                        synchronized (NotesDetector.this.noteDetectionFrequency) {
                            for (Map.Entry<String, Integer> noteFreq : NotesDetector.this.noteDetectionFrequency.entrySet()) {
                                Integer value = noteFreq.getValue();
                                if (value > 0) {
                                    value = 0;
                                    noteFreq.setValue(value);
                                    // inform the listeners of this change
                                    synchronized (NotesDetector.this.listeners) {
                                        String noteName = noteFreq.getKey();
                                        for (NoteDectectionInterface listener : NotesDetector.this.listeners) {
                                            listener.onNoteDetected(noteName, -1f, -1f, value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            };
            this.dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
            AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
            this.dispatcher.addAudioProcessor(p);
            // create the thread to perform the processing and start it
            this.detectionThread = new Thread(dispatcher, "Audio Dispatcher");
            this.detectionThread.start();
        }
    }
}

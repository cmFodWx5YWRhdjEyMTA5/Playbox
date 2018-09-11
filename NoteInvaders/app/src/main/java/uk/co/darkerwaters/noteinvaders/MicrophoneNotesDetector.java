package uk.co.darkerwaters.noteinvaders;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

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

public class MicrophoneNotesDetector {
    public interface NoteDetectionInterface {
        public void onNoteDetected(String name, float pitch, float probability, int frequency, boolean isPitched);
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

    private final List<NoteDetectionInterface> listeners;

    public MicrophoneNotesDetector(Activity context) {
        this.context = context;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.RECORD_AUDIO},
                    123);
        }
        // first thing to do is to create the notes
        if (null == Notes.instance()) {
            Notes.CreateNotes(context);
        }
        this.notes = Notes.instance();
        // and setup this detector
        this.listeners = new ArrayList<NoteDetectionInterface>();
        this.noteDetectionFrequency = new HashMap<String, Integer>(this.notes.getNoteCount());
        for (int i = 0; i < this.notes.getNoteCount(); ++i) {
            Note note = this.notes.getNote(i);
            this.noteDetectionFrequency.put(note.getName(), new Integer(0));
        }
    }

    public String[] getNotesNames() {
        String[] names = new String[this.notes.getNoteCount()];
        for (int i = 0; i < names.length; ++i) {
            names[i] = this.notes.getNote(i).getName();
        }
        return names;
    }

    public boolean addListener(NoteDetectionInterface listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(NoteDetectionInterface listener) {
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

    public boolean start() {
        // start everything up
        if (false == this.isThreadStarted) {
            // start the reduction thread to decrement the numbers in the list of frequencies
            this.reductionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (false == isStopThread) {
                        synchronized (MicrophoneNotesDetector.this.noteDetectionFrequency) {
                            for (Map.Entry<String, Integer> noteFreq : MicrophoneNotesDetector.this.noteDetectionFrequency.entrySet()) {
                                Integer value = noteFreq.getValue();
                                if (value > 0) {
                                    value = value - 1;
                                    noteFreq.setValue(value);
                                    // inform the listeners of this change
                                    synchronized (MicrophoneNotesDetector.this.listeners) {
                                        String noteName = noteFreq.getKey();
                                        for (NoteDetectionInterface listener : MicrophoneNotesDetector.this.listeners) {
                                            listener.onNoteDetected(noteName, -1f, -1f, value, false);
                                        }
                                    }
                                }
                            }
                        }
                        try {
                            synchronized (MicrophoneNotesDetector.this.waitingObject) {
                                MicrophoneNotesDetector.this.waitingObject.wait(500);
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
                        // get the name of the note
                        String noteName = notes.getNote(pitchInHz).getName();
                        Integer noteFreq;
                        // increase the frequency of this
                        synchronized (MicrophoneNotesDetector.this.noteDetectionFrequency) {
                            noteFreq = MicrophoneNotesDetector.this.noteDetectionFrequency.get(noteName) + 1;
                            MicrophoneNotesDetector.this.noteDetectionFrequency.put(noteName, noteFreq);
                        }
                        // inform the listeners of this change
                        synchronized (MicrophoneNotesDetector.this.listeners) {
                            for (NoteDetectionInterface listener : MicrophoneNotesDetector.this.listeners) {
                                listener.onNoteDetected(noteName, pitchInHz, result.getProbability(), noteFreq, result.isPitched());
                            }
                        }
                    } else {
                        // no pitch detected - clear it all out now
                        synchronized (MicrophoneNotesDetector.this.noteDetectionFrequency) {
                            for (Map.Entry<String, Integer> noteFreq : MicrophoneNotesDetector.this.noteDetectionFrequency.entrySet()) {
                                Integer value = noteFreq.getValue();
                                if (value > 0) {
                                    value = 0;
                                    noteFreq.setValue(value);
                                    // inform the listeners of this change
                                    synchronized (MicrophoneNotesDetector.this.listeners) {
                                        String noteName = noteFreq.getKey();
                                        for (NoteDetectionInterface listener : MicrophoneNotesDetector.this.listeners) {
                                            listener.onNoteDetected(noteName, -1f, -1f, value, false);
                                        }
                                    }
                                }
                            }
                        }
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
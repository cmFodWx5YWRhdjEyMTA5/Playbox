package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Note;

public class PianoPlaying extends PianoView {

    private static final Integer K_INITIAL_NOTE_DEPRESSION_COUNT = 10;
    private static final Integer K_NOTE_DEPRESSION_COUNTER_INTERVAL = 100;

    private Map<Chord, Integer> noteDepressionCount;

    private Thread reductionThread = null;
    private boolean isThreadStarted = false;
    private volatile boolean isStopThread = false;
    private final Object waitingObject = new Object();

    private Paint keyPressPaint;

    public PianoPlaying(Context context) {
        super(context);
    }

    public PianoPlaying(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PianoPlaying(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        super.init(context);

        // initialise the paint for the pressed keys
        this.keyPressPaint = new Paint();
        this.keyPressPaint.setStyle(Paint.Style.FILL);
        this.keyPressPaint.setColor(getResources().getColor(R.color.colorKeyPress));
        this.keyPressPaint.setAntiAlias(true);

        Chords chords = this.application.getSingleChords();
        this.noteDepressionCount = new HashMap<Chord, Integer>(chords.getSize());
        for (int i = 0; i < chords.getSize(); ++i) {
            Chord chord = chords.getChord(i);
            // also initialise the count in the map
            synchronized (this.noteDepressionCount) {
                this.noteDepressionCount.put(chord, 0);
            }
        }

        // fire up the thread that will remove key depressions in time
        if (false == this.isThreadStarted) {
            this.isStopThread = false;
            this.reductionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (false == isStopThread) {
                        synchronized (PianoPlaying.this.noteDepressionCount) {
                            for (Map.Entry<Chord, Integer> depressionCount : PianoPlaying.this.noteDepressionCount.entrySet()) {
                                Integer value = depressionCount.getValue();
                                if (value > 0) {
                                    value = value - 1;
                                    depressionCount.setValue(value);
                                    if (value == 0) {
                                        // this was just released
                                        informListeners(depressionCount.getKey());
                                    }
                                }
                            }
                        }
                        try {
                            synchronized (PianoPlaying.this.waitingObject) {
                                PianoPlaying.this.waitingObject.wait(K_NOTE_DEPRESSION_COUNTER_INTERVAL);
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
        }

    }

    @Override
    public void closeView() {
        // stop the thread from reducing notes
        if (this.isThreadStarted) {
            this.isThreadStarted = false;
            // and our thread while we are here
            this.isStopThread = true;
            synchronized (this.waitingObject) {
                this.waitingObject.notifyAll();
            }
            this.reductionThread = null;
        }
        // close the base
        super.closeView();
    }

    private void informListeners(Chord noteReleased) {
        synchronized (this.listeners) {
            for (IPianoViewListener listener : this.listeners) {
                listener.noteReleased(noteReleased);
            }
        }
    }

    @Override
    protected void drawKey(Canvas canvas, RectF keyRect, Chord keyNote) {
        // let the base class draw the key
        super.drawKey(canvas, keyRect, keyNote);
        // is it pressed?
        if (isNoteDepressed(keyNote)) {
            // highlight this pressed note
            canvas.drawRect(keyRect.left + 2,
                    keyRect.top + 2,
                    keyRect.right - 2,
                    keyRect.bottom - 2,
                    keyPressPaint);
        }
    }

    public void depressNote(Chord chord) {
        // set the depression count
        synchronized (this.noteDepressionCount) {
            this.noteDepressionCount.put(chord, K_INITIAL_NOTE_DEPRESSION_COUNT);
        }
        synchronized (this.listeners) {
            for (IPianoViewListener listener : this.listeners) {
                listener.noteDepressed(chord);
            }
        }
    }

    public Chord getDepressedNotes() {
        ArrayList<Note> notesPressed = new ArrayList<Note>();
        synchronized (this.noteDepressionCount) {
            for (Map.Entry<Chord, Integer> entry : this.noteDepressionCount.entrySet()) {
                if (entry.getValue() > 0) {
                    // this is pressed, add to the notes pressed to return
                    Collections.addAll(notesPressed, entry.getKey().getNotes());
                }
            }
        }
        // return the sound that now contains all the depressed notes
        return new Chord(notesPressed.toArray(new Note[notesPressed.size()]));
    }

    protected boolean isNoteDepressed(Chord note) {
        int depressionCount;
        synchronized (this.noteDepressionCount) {
            depressionCount = this.noteDepressionCount.get(note);
        }
        // return if the counter is above zero
        return depressionCount > 0;
    }
}

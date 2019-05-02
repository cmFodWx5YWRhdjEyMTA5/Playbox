package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Note;
import uk.co.darkerwaters.staveinvaders.notes.Range;

public class PianoTouchable extends PianoPlaying {

    private static final Integer K_INITIAL_NOTE_DEPRESSION_COUNT = 10;
    private static final Integer K_NOTE_DEPRESSION_COUNTER_INTERVAL = 100;

    private Map<Chord, Integer> noteDepressionCount;

    private Thread reductionThread = null;
    private boolean isThreadStarted = false;
    private volatile boolean isStopThread = false;
    private final Object waitingObject = new Object();

    private final ArrayList<PlayableKey> playableKeys = new ArrayList<PlayableKey>();
    private boolean isCreatePlayableKeys = false;

    private class PlayableKey {
        final RectF bounds;
        final Chord note;
        PlayableKey(RectF bounds, Chord note) {
            this.bounds = bounds;
            this.note = note;
        }
    }

    public PianoTouchable(Context context) {
        super(context);
    }

    public PianoTouchable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PianoTouchable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        super.init(context);

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
                        synchronized (PianoTouchable.this.noteDepressionCount) {
                            for (Map.Entry<Chord, Integer> depressionCount : PianoTouchable.this.noteDepressionCount.entrySet()) {
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
                            synchronized (PianoTouchable.this.waitingObject) {
                                PianoTouchable.this.waitingObject.wait(K_NOTE_DEPRESSION_COUNTER_INTERVAL);
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
        }
        super.closeView();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // clear all our keys because it has changed size
        resetPlayableKeys();
        // and change the size
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void informListeners(Chord noteReleased) {
        synchronized (this.listeners) {
            for (IPianoViewListener listener : this.listeners) {
                listener.noteReleased(noteReleased);
            }
        }
    }

    @Override
    public void setNoteRange(Range newRange, Boolean isShowPrimatives) {
        // let the base set the range
        super.setNoteRange(newRange, isShowPrimatives);
        // clear all our keys because it has changed size
        resetPlayableKeys();
    }

    @Override
    public void setNoteRange(float minPitchDetected, float maxPitchDetected, Boolean isShowPrimatives) {
        // let the base set the range
        super.setNoteRange(minPitchDetected, maxPitchDetected, isShowPrimatives);
        // clear all our keys because it has changed size
        resetPlayableKeys();
    }

    private void resetPlayableKeys() {
        if (null != this.playableKeys) {
            synchronized (this.playableKeys) {
                this.playableKeys.clear();
            }
        }
        this.isCreatePlayableKeys = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw all the keys, this will create the playable ones too
        super.onDraw(canvas);
        // next time, don't create keys
        this.isCreatePlayableKeys = false;
    }

    @Override
    protected void drawKey(Canvas canvas, RectF keyRect, Chord keyNote) {
        // draw the key in
        super.drawKey(canvas, keyRect, keyNote);
        if (this.isCreatePlayableKeys) {
            // create the playable key for this one we just drew
            synchronized (this.playableKeys) {
                this.playableKeys.add(new PlayableKey(keyRect, keyNote));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // get masked (not specific to a pointer) action
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                // process this press
                onViewPressed(event);
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                // can ignore this
                break;

        }
        // return our result
        return true;
    }

    private boolean onViewPressed(MotionEvent e) {
        MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
        for (int i = 0; i < e.getPointerCount(); ++i) {
            e.getPointerCoords(i, pointerCoords);
            // check this for a key to have been hit, go through the list backwards
            // to test the ones drawn on top first
            PlayableKey[] keysToTest;
            synchronized (this.playableKeys) {
                keysToTest = this.playableKeys.toArray(new PlayableKey[this.playableKeys.size()]);
            }
            for (int j = keysToTest.length - 1; j >=0; --j) {
                PlayableKey testKey = keysToTest[j];
                if (null != testKey) {
                    // have a key to check, check it for a hit
                    if (testKey.bounds.contains(pointerCoords.x, pointerCoords.y)) {
                        // this is hit, press this key and invalidate the view
                        depressNote(testKey.note);
                        invalidate((int) testKey.bounds.left, (int) testKey.bounds.top,
                                (int) testKey.bounds.right, (int) testKey.bounds.bottom);
                        // break from the inner loop - stop checking keys
                        // so pressing the sharp will not press the white underneath
                        break;
                    }
                }
            }
        }
        // return that this was handled OK and we don't want any more information
        return false;
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
                    // this is pressed, add to the chord to return
                    Chord pressed = entry.getKey();
                    for (Note notePressed : pressed.getNotes()) {
                        notesPressed.add(notePressed);
                    }
                }
            }
        }
        // return the sound that now contains all the depressed notes
        return new Chord(notesPressed.toArray(new Note[notesPressed.size()]));
    }

    protected boolean isNoteDepressed(Chord note) {
        Integer depressionCount = 0;
        synchronized (this.noteDepressionCount) {
            depressionCount = this.noteDepressionCount.get(note);
        }
        // return if the counter is above zero
        return super.isNoteDepressed(note) || depressionCount > 0;
    }
}

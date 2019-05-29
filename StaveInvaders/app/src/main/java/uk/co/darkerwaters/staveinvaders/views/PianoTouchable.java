package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Range;

public class PianoTouchable extends PianoPlaying implements InputSelector.InputTypeListener {

    private static final Integer K_INITIAL_NOTE_DEPRESSION_COUNT = 5;
    private static final Integer K_NOTE_DEPRESSION_COUNTER_INTERVAL = 100;

    private final ArrayList<PlayableKey> playableKeys = new ArrayList<>();
    private boolean isCreatePlayableKeys = false;
    private boolean isAllowTouch = true;

    private final Map<Chord, Integer> noteDepressionCount = new HashMap<>();

    private Thread reductionThread = null;
    private boolean isThreadStarted = false;
    private volatile boolean isStopThread = false;
    private final Object waitingObject = new Object();

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

    public void setIsAllowTouch(boolean isAllowTouch) {
        this.isAllowTouch = isAllowTouch;
        // reset any keys to create if just turned on
        resetPlayableKeys();
    }

    @Override
    public void onInputTypeChanged(Settings.InputType newType) {
        // when the input type changes, reset our touchable keys as might be active / or not
        resetPlayableKeys();
    }

    public boolean getIsAllowTouch() {
        InputSelector inputSelector = this.application.getInputSelector();
        return this.isAllowTouch && null != inputSelector &&
                inputSelector.getActiveInputType() == Settings.InputType.keys;
    }

    @Override
    protected boolean calculateAnimation(float keyWidth, float timeElapsed, Chords singleChords) {
        boolean toReturn = super.calculateAnimation(keyWidth, timeElapsed, singleChords);
        if (toReturn) {
            // keys are moving
            resetPlayableKeys();
        }
        return toReturn;
    }

    @Override
    protected void initialiseView(Context context) {
        super.initialiseView(context);

        // listen to changes in input type
        InputSelector inputSelector = this.application.getInputSelector();
        inputSelector.addListener(this);

        Chords chords = this.application.getSingleChords();
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
                    performThreadedReduction();
                }
            });
            // start the reducer thread
            this.reductionThread.start();
            this.isThreadStarted = true;
        }
    }

    private void performThreadedReduction() {
        while (false == isStopThread) {
            synchronized (this.noteDepressionCount) {
                for (Map.Entry<Chord, Integer> depressionCount :this.noteDepressionCount.entrySet()) {
                    Integer value = depressionCount.getValue();
                    if (value > 0) {
                        value = value - 1;
                        depressionCount.setValue(value);
                        if (value == 0) {
                            // this was just released, release the note
                            releaseNote(depressionCount.getKey());
                        }
                    }
                }
            }
            try {
                synchronized (this.waitingObject) {
                    this.waitingObject.wait(K_NOTE_DEPRESSION_COUNTER_INTERVAL);
                }
            } catch (InterruptedException e) {
                // fine, will exit the thread
            }
        }
    }

    @Override
    public void closeView() {
        // remove as a listener
        InputSelector inputSelector = this.application.getInputSelector();
        if (null != inputSelector) {
            inputSelector.removeListener(this);
        }
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
        // close this view
        super.closeView();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // clear all our keys because it has changed size
        resetPlayableKeys();
        // and change the size
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void depressNote(Chord chord) {
        // set the depression count
        synchronized (this.noteDepressionCount) {
            this.noteDepressionCount.put(chord, K_INITIAL_NOTE_DEPRESSION_COUNT);
        }
        // and let the base do it's thing
        super.depressNote(chord);
        // also - we are acting as an input device, inform the input manager of this action
        InputSelector inputSelector = this.application.getInputSelector();
        if (getIsAllowTouch() && null != inputSelector) {
            // inform the input selector that a key was pressed here then
            inputSelector.onNoteDetected(inputSelector.getActiveInput(), chord, true, 100f);
        }
    }

    @Override
    public void releaseNote(Chord note) {
        // let the base do its thing
        super.releaseNote(note);
        // also - we are acting as an input device, inform the input manager of this action
        InputSelector inputSelector = this.application.getInputSelector();
        if (getIsAllowTouch() && null != inputSelector) {
            // inform the input selector that a key was pressed here then
            inputSelector.onNoteDetected(inputSelector.getActiveInput(), note, false, 0f);
        }
    }

    @Override
    public void setNoteRange(Range newRange) {
        // let the base set the range
        super.setNoteRange(newRange);
        // clear all our keys because it has changed size
        resetPlayableKeys();
    }

    private void resetPlayableKeys() {
        if (null != this.playableKeys) {
            synchronized (this.playableKeys) {
                this.playableKeys.clear();
            }
        }
        // reset the flag to create playable (touchable keys) if not allowed then will not
        this.isCreatePlayableKeys = getIsAllowTouch();
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
                keysToTest = this.playableKeys.toArray(new PlayableKey[0]);
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
}

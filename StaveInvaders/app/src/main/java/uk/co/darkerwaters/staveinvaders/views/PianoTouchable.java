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

    private final ArrayList<PlayableKey> playableKeys = new ArrayList<PlayableKey>();
    private boolean isCreatePlayableKeys = false;
    private boolean isAllowTouch = true;

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
    protected void init(Context context) {
        // initialise this view
        super.init(context);
    }

    @Override
    public void closeView() {
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
        // reset the flag to create playable (touchable keys) if not allowed then will not
        this.isCreatePlayableKeys = isAllowTouch;
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

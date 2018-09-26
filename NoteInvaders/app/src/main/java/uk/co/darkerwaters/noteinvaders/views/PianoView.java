package uk.co.darkerwaters.noteinvaders.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.NoteRange;
import uk.co.darkerwaters.noteinvaders.state.Notes;

public class PianoView extends View {

    private static final Integer K_INITIAL_NOTE_DEPRESSION_COUNT = 10;
    private static final Integer K_NOTE_DEPRESSION_COUNTER_INTERVAL = 100;
    private GestureDetector gestureLetector = null;

    public interface IPianoViewListener {
        void noteReleased(Note note);
        void noteDepressed(Note note);
        void pianoViewSizeChanged(int w, int h, int oldw, int oldh);
    }

    private Paint whitePaint;
    private Paint blackPaint;
    private Paint redPaint;

    private int whiteKeyCount = 0;
    private int initialWhiteKey = 0;

    private int startNoteIndex = 0;
    private NoteRange noteRange = null;

    private Map<Note, Integer> noteDepressionCount;

    private boolean isDrawNoteNames = true;

    private Thread reductionThread = null;
    private boolean isThreadStarted = false;
    private volatile boolean isStopThread = false;
    private final Object waitingObject = new Object();

    private List<IPianoViewListener> listeners = null;

    private boolean isPlayable = false;
    private ArrayList<PlayableKey> playableKeys = null;

    private class PlayableKey {
        final RectF bounds;
        final Note note;
        PlayableKey(RectF bounds, Note note) {
            this.bounds = bounds;
            this.note = note;
        }
    }

    private final static float[] sharpOffsets = {
            0.4f,           // A
            Float.NaN,      // B
            0.6f,           // C
            0.4f,           // D
            Float.NaN,      // E
            0.6f,           // F
            0.4f};          // G

    public PianoView(Context context) {
        super(context);
        init(context);
    }

    public PianoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PianoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

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
    }

    private void init(final Context context) {
        // Initialize new paints to draw the keys
        this.whitePaint = new Paint();
        this.whitePaint.setStyle(Paint.Style.STROKE);
        this.whitePaint.setStrokeWidth(2);
        this.whitePaint.setColor(Color.BLACK);
        this.whitePaint.setAntiAlias(true);
        // and for the black keys
        this.blackPaint = new Paint();
        this.blackPaint.setStyle(Paint.Style.FILL);
        this.blackPaint.setColor(Color.BLACK);
        this.blackPaint.setAntiAlias(true);
        // and for the pressed keys
        this.redPaint = new Paint();
        this.redPaint.setStyle(Paint.Style.FILL);
        this.redPaint.setColor(getResources().getColor(R.color.colorKeyPress));
        this.redPaint.setAntiAlias(true);

        this.listeners = new ArrayList<IPianoViewListener>();

        // and initialise the notes we are going to show on the piano, do the whole range...
        if (null == Notes.instance()) {
            Notes.CreateNotes(context);
        }
        // ok, let's find out the number of white keys we want to display
        Notes notes = Notes.instance();
        this.noteDepressionCount = new HashMap<Note, Integer>(notes.getNoteCount());
        for (int i = 0; i < notes.getNoteCount(); ++i) {
            Note note = notes.getNote(i);
            // also initialise the count in the map
            synchronized (this.noteDepressionCount) {
                this.noteDepressionCount.put(note, 0);
            }
        }
        // start and end the whole range
        setNoteRange(notes.getNote(0).getFrequency(), notes.getNote(notes.getNoteCount() - 1).getFrequency());

        // fire up the thread that will remove key depressions in time
        if (false == this.isThreadStarted) {
            this.reductionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (false == isStopThread) {
                        synchronized (PianoView.this.noteDepressionCount) {
                            for (Map.Entry<Note, Integer> depressionCount : PianoView.this.noteDepressionCount.entrySet()) {
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
                            synchronized (PianoView.this.waitingObject) {
                                PianoView.this.waitingObject.wait(K_NOTE_DEPRESSION_COUNTER_INTERVAL);
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

        setIsPlayable(this.isPlayable);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // clear all our keys because it has changed size
        this.playableKeys = null;
        synchronized (this.listeners) {
            for (IPianoViewListener listener : this.listeners) {
                listener.pianoViewSizeChanged(w, h, oldw, oldh);
            }
        }
    }

    public boolean addListener(IPianoViewListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(IPianoViewListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    private void informListeners(Note noteReleased) {
        synchronized (this.listeners) {
            for (IPianoViewListener listener : this.listeners) {
                listener.noteReleased(noteReleased);
            }
        }
    }

    public void setNoteRange(float minPitchDetected, float maxPitchDetected) {
        // set the notes that are to be shown on this piano
        Notes notes = Notes.instance();
        if (null == this.noteRange) {
            this.noteRange = new NoteRange(null, null);
        }
        // count the white keys from the min pitch
        this.startNoteIndex = notes.getNoteIndex(minPitchDetected);
        this.noteRange.setStart(notes.getNote(this.startNoteIndex));
        // set the end note
        int endNoteIndex = notes.getNoteIndex(maxPitchDetected);
        this.noteRange.setEnd(notes.getNote(endNoteIndex));
        if (this.noteRange.getStart().equals(this.noteRange.getEnd())) {
            // there is no range, make one
            int index = 0;
            for (index = 0; index < notes.getNoteCount(); ++index) {
                if (notes.getNote(index).equals(this.noteRange.getStart())) {
                    // this is our note index
                    this.startNoteIndex = Math.max(index - 7, 0);
                    this.noteRange.setStart(notes.getNote(startNoteIndex));
                    this.noteRange.setEnd(notes.getNote(Math.min(index + 7, notes.getNoteCount() - 1)));
                    break;
                }
            }
        }
        // set this range now
        setNoteRange(this.noteRange);
    }

    public void setNoteRange(NoteRange newRange) {
        // set the members to remember this range to display
        Notes notes = Notes.instance();
        if (null != newRange && null != newRange.getStart() && null != newRange.getEnd()) {
            this.noteRange = newRange;

            // set the starting key, we don't want to start on a sharp or just after one
            // as we won't draw it and it will look and behave weird, go down until we get to
            // a normal kind of note (a white key without a flat, which is a sharp before it)
            // basically this is an F or a C then
            this.startNoteIndex = notes.getNoteIndex(this.noteRange.getStart().getFrequency());

            // if the starting key is a sharp or has a flat, move down away from it
            while (this.startNoteIndex > 0 &&
                    (this.noteRange.getStart().isSharp() ||
                            notes.getNote(this.startNoteIndex - 1).isSharp())) {
                // while there are notes before the start and the start is a sharp or there is a sharp
                // before it, keep looking further down the scale
                this.noteRange.setStart(notes.getNote(--this.startNoteIndex));
            }

            // do the end note too
            int endNoteIndex = notes.getNoteIndex(this.noteRange.getEnd().getFrequency());
            // while the end note is a sharp, or has a sharp - move up from it
            while (endNoteIndex < notes.getNoteCount() - 1 &&
                    (this.noteRange.getEnd().isSharp() ||
                            notes.getNote(endNoteIndex + 1).isSharp())) {
                // while there are notes after and the end is a sharp or has one, keep looking
                this.noteRange.setEnd(notes.getNote(++endNoteIndex));
            }

            // and setup the white keys accordingly
            this.whiteKeyCount = 0;
            for (int i = 0; i < notes.getNoteCount(); ++i) {
                if (this.whiteKeyCount > 0) {
                    // started counting, count this one
                    if (false == notes.getNote(i).isSharp()) {
                        // this is a normal note, count these
                        ++this.whiteKeyCount;
                    }
                } else if (notes.getNote(i).equals(this.noteRange.getStart())) {
                    this.whiteKeyCount = 1;
                }
                // check to see if we have all our notes
                if (notes.getNote(i).equals(this.noteRange.getEnd())) {
                    // last one...
                    break;
                }
            }
            // and remember where to start
            this.initialWhiteKey = notes.getNote(this.startNoteIndex).getNotePrimativeIndex();
        }
    }

    private int getContentWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getContentHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    private int getStartX() {
        return getPaddingLeft();
    }

    private int getKeyWidth() {
        return getContentWidth() / this.whiteKeyCount;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw a solid color on the canvas as background
        canvas.drawColor(Color.WHITE);

        if (this.whiteKeyCount > 0) {
            // allocations per draw cycle.
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int paddingBottom = getPaddingBottom();

            int contentHeight = getContentHeight();

            float keyWidth = getKeyWidth();
            float sharpWidth = keyWidth * 0.4f;


            Notes notes = Notes.instance();
            boolean isCreateKey = false;
            if (this.isPlayable && null == this.playableKeys) {
                // there are no keys - create them
                this.playableKeys = new ArrayList<PlayableKey>();
                isCreateKey = true;
            }
            // draw all the keys, go through twice, drawing white then black over the top of them
            // first let's go through the keys and draw in all the white ones
            int keyIndex = 0;
            int noteIndex = this.startNoteIndex;
            while (keyIndex < this.whiteKeyCount && noteIndex < notes.getNoteCount()) {
                // loop through the notes finding all the white ones
                Note currentNote = notes.getNote(noteIndex++);
                if (false == currentNote.isSharp()) {
                    // this is a white key, draw this
                    RectF keyRect = new RectF(paddingLeft + (keyIndex * keyWidth),
                            paddingTop,
                            paddingLeft + ((keyIndex + 1) * keyWidth),
                            contentHeight - paddingBottom);
                    // draw this white key
                    canvas.drawRect(keyRect, whitePaint);
                    if (isCreateKey) {
                        this.playableKeys.add(new PlayableKey(keyRect, currentNote));
                    }
                    // create a key to press here
                    if (isNoteDepressed(currentNote)) {
                        // highlight this pressed note
                        canvas.drawRect(keyRect.left + 2,
                                keyRect.top + 2,
                                keyRect.right - 2,
                                keyRect.bottom - 2,
                                redPaint);
                    }
                    if (isDrawNoteNames) {
                        canvas.drawText(currentNote.getName(), keyRect.left, keyRect.bottom - 16, blackPaint);
                    }
                    // move on our white key counter
                    ++keyIndex;
                }
            }
            // now we need to go through again for the sharps and flats
            int blackIndex = this.initialWhiteKey;
            float blackLeft = 0f;
            keyIndex = 0;
            noteIndex = this.startNoteIndex;
            while (keyIndex < this.whiteKeyCount && noteIndex < notes.getNoteCount()) {
                // we will go through with reference to the white notes to offset the blacks better
                Note currentNote = notes.getNote(noteIndex++);
                if (false == currentNote.isSharp() && noteIndex < notes.getNoteCount()) {
                    // this is a white key, is there a sharp to draw?
                    Note blackNote = notes.getNote(noteIndex);
                    if (blackNote.isSharp() && false == Float.isNaN(sharpOffsets[blackIndex])) {
                        // this is a sharp, draw this note now
                        blackLeft = paddingLeft + ((keyIndex + 1) * keyWidth) - (sharpWidth * sharpOffsets[blackIndex]);
                        // create the rect for this
                        RectF keyRect = new RectF(blackLeft,
                                paddingTop,
                                blackLeft + sharpWidth,
                                (contentHeight * 0.7f) - paddingBottom);
                        // draw it
                        canvas.drawRect(keyRect, blackPaint);
                        // is it pressed?
                        if (isNoteDepressed(blackNote)) {
                            // highlight this pressed note
                            canvas.drawRect(keyRect.left + 2,
                                    keyRect.top + 2,
                                    keyRect.right - 2,
                                    keyRect.bottom - 2,
                                    redPaint);
                        }
                        if (isCreateKey) {
                            this.playableKeys.add(new PlayableKey(keyRect, blackNote));
                        }
                    }
                    // move on the index for the white key
                    ++keyIndex;
                    // move on the black index to get the offset for this key properly next time
                    if (++blackIndex > sharpOffsets.length - 1) {
                        blackIndex = 0;
                    }
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // by default we are not interested in any touching!
        boolean result = false;
        if (null != gestureLetector) {
            // there is a detector, use this
            result = gestureLetector.onTouchEvent(event);
        }
        // return our result
        return result;
    }

    private boolean onViewPressed(MotionEvent e) {
        MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
        for (int i = 0; i < e.getPointerCount(); ++i) {
            e.getPointerCoords(i, pointerCoords);
            // check this for a key to have been hit, go through the list backwards
            // to test the ones drawn on top first
            for (int j = this.playableKeys.size() - 1; j >=0; --j) {
                PlayableKey testKey = this.playableKeys.get(j);
                if (null != testKey) {
                    // have a key to check, check it for a hit
                    if (testKey.bounds.contains(pointerCoords.x, pointerCoords.y)) {
                        // this is hit, only do the one per press event
                        depressNote(testKey.note);
                        invalidate((int) testKey.bounds.left, (int) testKey.bounds.top,
                                (int) testKey.bounds.right, (int) testKey.bounds.bottom);
                        // stop checking all the other keys
                        break;
                    }
                }
            }
        }
        // return that this was handled OK and we don't want any more information
        return false;
    }

    public void depressNote(Note note) {
        // set the depression count
        synchronized (this.noteDepressionCount) {
            this.noteDepressionCount.put(note, K_INITIAL_NOTE_DEPRESSION_COUNT);
        }
        synchronized (this.listeners) {
            for (IPianoViewListener listener : this.listeners) {
                listener.noteDepressed(note);
            }
        }
    }

    private boolean isNoteDepressed(Note note) {
        Integer depressionCount = 0;
        synchronized (this.noteDepressionCount) {
            depressionCount = this.noteDepressionCount.get(note);
        }
        // return if the counter is above zero
        return depressionCount > 0;
    }

    public String getRangeText() {
        String range = "--";
        if (null != this.noteRange) {
            range = this.noteRange.toString();
        }
        return range;
    }

    public void setIsPlayable(boolean isPlayable) {
        this.isPlayable = isPlayable;
        if (this.isPlayable) {
            // we are playable, fine - onDraw will create the playable views
            // and we can setup the gesture listener
            GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    // we need to return true in order to tell the listener we are interested
                    return onViewPressed(e);
                }
            };
            this.gestureLetector = new GestureDetector(this.getContext(), gestureListener);
        }
        else {
            // are not playable
            this.playableKeys = null;
            this.gestureLetector = null;
        }
    }

    private void onPianoClicked(View v) {
        if (this.isPlayable) {
            // handle the clicking of the piano - what key did we press?

        }
    }
}

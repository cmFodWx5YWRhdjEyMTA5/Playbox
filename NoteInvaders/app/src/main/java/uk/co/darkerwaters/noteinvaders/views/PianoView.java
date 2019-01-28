package uk.co.darkerwaters.noteinvaders.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.state.Chord;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.NoteRange;
import uk.co.darkerwaters.noteinvaders.state.Notes;
import uk.co.darkerwaters.noteinvaders.state.Playable;

public class PianoView extends View {

    private static final Integer K_INITIAL_NOTE_DEPRESSION_COUNT = 10;
    private static final Integer K_NOTE_DEPRESSION_COUNTER_INTERVAL = 100;

    public interface IPianoViewListener {
        void noteReleased(Playable note);
        void noteDepressed(Playable note);
        void pianoViewSizeChanged(int w, int h, int oldw, int oldh);
    }

    private Paint whitePaint;
    private Paint blackPaint;
    private Paint letterPaint;
    private Paint redPaint;

    private int whiteKeyCount = 0;
    private int initialWhiteKey = 0;

    private int startNoteIndex = 0;
    private NoteRange noteRange = null;

    private Map<Note, Integer> noteDepressionCount;

    private boolean isDrawNoteNames = true;
    private boolean isShowPrimatives = false;

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
        // and to draw the letters
        this.letterPaint = new Paint();
        this.letterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.letterPaint.setStrokeWidth(getResources().getDimension(R.dimen.letter_stroke));
        this.letterPaint.setColor(getResources().getColor(R.color.colorLaser));
        this.letterPaint.setAntiAlias(true);
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
        setNoteRange(notes.getNote(0).getFrequency(), notes.getNote(notes.getNoteCount() - 1).getFrequency(), false);

        // and start up the view
        start(context);
    }

    public void start(final Context context) {
        // fire up the thread that will remove key depressions in time
        if (false == this.isThreadStarted) {
            this.isStopThread = false;
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
            if (this.listeners.contains(listener)) {
                return false;
            }
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

    public void setNoteRange(float minPitchDetected, float maxPitchDetected, Boolean isShowPrimatives) {
        // set the notes that are to be shown on this piano
        Notes notes = Notes.instance();
        if (null == this.noteRange) {
            this.noteRange = new NoteRange((Note)null, (Note)null);
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
        setNoteRange(this.noteRange, isShowPrimatives);
    }

    public void setNoteRange(NoteRange newRange, Boolean isShowPrimatives) {
        if (null != isShowPrimatives) {
            this.isShowPrimatives = isShowPrimatives;
        }
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
            int keyCount = 0;
            for (int i = 0; i < notes.getNoteCount(); ++i) {
                if (keyCount > 0) {
                    // started counting, count this one
                    if (false == notes.getNote(i).isSharp()) {
                        // this is a normal note, count these
                        ++keyCount;
                    }
                } else if (notes.getNote(i).equals(this.noteRange.getStart())) {
                    keyCount = 1;
                }
                // check to see if we have all our notes
                if (notes.getNote(i).equals(this.noteRange.getEnd())) {
                    // last one...
                    break;
                }
            }
            this.whiteKeyCount = keyCount;
            // and remember where to start
            this.initialWhiteKey = notes.getNote(this.startNoteIndex).getNotePrimativeIndex();
        }
    }

    public void setIsDrawNoteName(boolean isDrawNoteName) {
        this.isDrawNoteNames = isDrawNoteName;
        this.invalidate();
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
        if (this.whiteKeyCount == 0) {
            return 5;
        }
        else {
            return getContentWidth() / this.whiteKeyCount;
        }
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
            letterPaint.setTextSize(keyWidth * 0.6f);
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
                        canvas.drawText(isShowPrimatives ? "" + currentNote.getNotePrimative() : currentNote.getName(),
                                keyRect.left + (keyWidth * 0.175f),
                                keyRect.bottom - (keyWidth * 0.5f),
                                letterPaint);
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
        if (this.isPlayable) {
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
            for (int j = this.playableKeys.size() - 1; j >=0; --j) {
                PlayableKey testKey = this.playableKeys.get(j);
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

    public void depressNote(Playable playable) {
        // get the notes from this playable
        Note[] notesPressed = playable.toNoteArray();
        // set the depression count
        synchronized (this.noteDepressionCount) {
            for (Note note : notesPressed) {
                this.noteDepressionCount.put(note, K_INITIAL_NOTE_DEPRESSION_COUNT);
            }
        }
        synchronized (this.listeners) {
            for (IPianoViewListener listener : this.listeners) {
                for (Note note : notesPressed) {
                    listener.noteDepressed(note);
                }
            }
        }
    }

    public Chord getDepressedNotes() {
        Chord toReturn = new Chord("pressed");
        synchronized (this.noteDepressionCount) {
            for (Map.Entry<Note, Integer> entry : this.noteDepressionCount.entrySet()) {
                if (entry.getValue() > 0) {
                    // this is pressed, add to the chord to return
                    toReturn.addNote(entry.getKey());
                }
            }
        }
        // return the chord that now contains all the depressed notes
        return toReturn;
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
        if (false == this.isPlayable) {
            // are not playable
            this.playableKeys = null;
        }
    }

    private void onPianoClicked(View v) {
        if (this.isPlayable) {
            // handle the clicking of the piano - what key did we press?

        }
    }
}

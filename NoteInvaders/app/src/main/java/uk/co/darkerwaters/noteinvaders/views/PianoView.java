package uk.co.darkerwaters.noteinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Notes;

public class PianoView extends View {

    private static final Integer K_INITIAL_NOTE_DEPRESSION_COUNT = 10;
    private static final Integer K_NOTE_DEPRESSION_COUNTER_INTERVAL = 100;

    public interface IPianoViewListener {
        public void noteReleased(Note note);
    }

    private Paint whitePaint;
    private Paint blackPaint;
    private Paint redPaint;

    private int whiteKeyCount = 0;
    private int initialWhiteKey = 0;

    private int startNoteIndex = 0;
    private Note startNote = null;
    private Note endNote = null;

    private Map<Note, Integer> noteDepressionCount;

    private Thread reductionThread = null;
    private boolean isThreadStarted = false;
    private volatile boolean isStopThread = false;
    private final Object waitingObject = new Object();

    private List<IPianoViewListener> listeners = null;

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
        this.redPaint.setColor(Color.RED);
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
        // count the white keys from the min pitch
        this.startNoteIndex = notes.getNoteIndex(minPitchDetected);
        this.startNote = notes.getNote(this.startNoteIndex);
        // if the starting key is a sharp - move up from this
        while (this.startNote.isSharp()) {
            this.startNote = notes.getNote(++this.startNoteIndex);
        }

        int endNoteIndex = notes.getNoteIndex(maxPitchDetected);
        this.endNote = notes.getNote(endNoteIndex);
        // if the end note is a sharp - move down from this
        while (this.endNote.isSharp()) {
            this.endNote = notes.getNote(--endNoteIndex);
        }
        if (startNote.equals(endNote)) {
            // there is no range, make one
            int index = 0;
            for (index = 0; index < notes.getNoteCount(); ++index) {
                if (notes.getNote(index).equals(startNote)) {
                    // this is our note index
                    this.startNoteIndex = Math.max(index - 7, 0);
                    this.startNote = notes.getNote(startNoteIndex);
                    this.endNote = notes.getNote(Math.min(index + 7, notes.getNoteCount() - 1));
                    break;
                }
            }
        }
        this.whiteKeyCount = 0;
        for (int i = 0; i < notes.getNoteCount(); ++i) {
            if (this.whiteKeyCount > 0) {
                // started counting, count this one
                if (false == notes.getNote(i).isSharp()) {
                    // this is a normal note, count these
                    ++this.whiteKeyCount;
                }
            }
            else if (notes.getNote(i).equals(startNote)) {
                this.whiteKeyCount = 1;
            }
            // check to see if we have all our notes
            if (notes.getNote(i).equals(endNote)) {
                // last one...
                break;
            }
        }
        // and remember where to start
        this.initialWhiteKey = notes.getNote(this.startNoteIndex).getNotePrimativeIndex();
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
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();

            int contentWidth = getWidth() - paddingLeft - paddingRight;
            int contentHeight = getHeight() - paddingTop - paddingBottom;

            float keyWidth = contentWidth / this.whiteKeyCount;
            float sharpWidth = keyWidth * 0.4f;

            int blackIndex = this.initialWhiteKey;
            float blackLeft = 0f;
            int noteIndex = this.startNoteIndex;
            Notes notes = Notes.instance();
            // draw white keys
            for (int i = 0; i < this.whiteKeyCount; i++) {
                // get the note that we are about to draw
                Note currentNote = notes.getNote(noteIndex++);
                // draw the note
                canvas.drawRect(paddingLeft + (i * keyWidth),
                        paddingTop,
                        paddingLeft + ((i + 1) * keyWidth),
                        contentHeight - paddingBottom,
                        whitePaint);
                if (isNoteDepressed(currentNote)) {
                    // highlight this pressed note
                    canvas.drawRect(paddingLeft + (i * keyWidth) + 2,
                            paddingTop + 2,
                            paddingLeft + ((i + 1) * keyWidth) - 2,
                            contentHeight - paddingBottom - 2,
                            redPaint);
                }
                // do the sharp for this white note
                if (false == Float.isNaN(sharpOffsets[blackIndex]) && i < this.whiteKeyCount - 1) {
                    // get the note that we are about to draw
                    currentNote = notes.getNote(noteIndex++);
                    // draw in the sharp for this key now as not a NaN and not the last key
                    blackLeft = paddingLeft + ((i + 1) * keyWidth) - (sharpWidth * sharpOffsets[blackIndex]);
                    // draw it
                    canvas.drawRect(blackLeft,
                            paddingTop,
                            blackLeft + sharpWidth,
                            (contentHeight * 0.7f) - paddingBottom,
                            isNoteDepressed(currentNote) ? redPaint : blackPaint);
                }
                if (++blackIndex > sharpOffsets.length - 1) {
                    blackIndex = 0;
                }
            }
        }
    }

    public void depressNote(Note note) {
        // set the depression count
        synchronized (this.noteDepressionCount) {
            this.noteDepressionCount.put(note, K_INITIAL_NOTE_DEPRESSION_COUNT);
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
        if (null != startNote && null != endNote) {
            range = startNote.getName(0) + " -- " + endNote.getName(0);
        }
        return range;
    }
}

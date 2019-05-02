package uk.co.darkerwaters.staveinvaders.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Range;

public class PianoView extends View {

    public interface IPianoViewListener {
        void pianoViewSizeChanged(int w, int h, int oldw, int oldh);
        void noteReleased(Chord chord);
        void noteDepressed(Chord chord);
    }

    private Paint whitePaint;
    private Paint blackPaint;
    private Paint letterPaint;

    private int whiteKeyCount = 0;
    private int initialWhiteKey = 0;

    private int startNoteIndex = 0;
    private Range noteRange = null;

    private boolean isDrawNoteNames = true;
    private boolean isShowPrimatives = false;

    protected final List<IPianoViewListener> listeners = new ArrayList<IPianoViewListener>();

    protected final Application application;

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
        // get our application here
        this.application = (Application) ((Activity)context).getApplication();
        // and initialise the data on this view
        init(context);
    }

    public PianoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // get our application here
        this.application = (Application) ((Activity)context).getApplication();
        // and initialise the data on this view
        init(context);
    }

    public PianoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // get our application here
        this.application = (Application) ((Activity)context).getApplication();
        // and initialise the data on this view
        init(context);
    }

    protected void init(final Context context) {
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

        // and initialise the notes we are going to show on the piano, do the whole range...
        Chords notes = this.application.getSingleChords();
        // start and end a middling range
        setNoteRange(notes.getChord("C3").root().getFrequency(), notes.getChord("B5").root().getFrequency(), false);
    }

    public void closeView() {
        // shut down this view
        synchronized (this.listeners) {
            this.listeners.clear();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // inform all the listeners that the view size has changed
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

    public void setNoteRange(float minPitchDetected, float maxPitchDetected, Boolean isShowPrimatives) {
        // set the notes that are to be shown on this piano
        Chords notes = this.application.getSingleChords();
        if (null == this.noteRange) {
            this.noteRange = new Range((Chord)null, (Chord)null);
        }
        // count the white keys from the min pitch
        this.startNoteIndex = notes.getChordIndex(minPitchDetected);
        if (this.startNoteIndex >= 0) {
            this.noteRange.setStart(notes.getChord(this.startNoteIndex));
        }
        // set the end note
        int endNoteIndex = notes.getChordIndex(maxPitchDetected);
        if (endNoteIndex >= 0) {
            this.noteRange.setEnd(notes.getChord(endNoteIndex));
        }
        if (this.noteRange.getStart().equals(this.noteRange.getEnd())) {
            // there is no range, make one
            int index = 0;
            for (index = 0; index < notes.getSize(); ++index) {
                if (notes.getChord(index).equals(this.noteRange.getStart())) {
                    // this is our note index
                    this.startNoteIndex = Math.max(index - 7, 0);
                    this.noteRange.setStart(notes.getChord(startNoteIndex));
                    this.noteRange.setEnd(notes.getChord(Math.min(index + 7, notes.getSize() - 1)));
                    break;
                }
            }
        }
        // set this range now
        setNoteRange(this.noteRange, isShowPrimatives);
    }

    public void setNoteRange(Range newRange, Boolean isShowPrimatives) {
        if (null != isShowPrimatives) {
            this.isShowPrimatives = isShowPrimatives;
        }
        // set the members to remember this range to display
        Chords notes = this.application.getSingleChords();
        if (null != newRange && null != newRange.getStart() && null != newRange.getEnd()) {
            this.noteRange = newRange;

            // set the starting key, we don't want to start on a sharp or just after one
            // as we won't draw it and it will look and behave weird, go down until we get to
            // a normal kind of note (a white key without a flat, which is a sharp before it)
            // basically this is an F or a C then
            this.startNoteIndex = notes.getChordIndex(this.noteRange.getStart().root().getFrequency());
            int endNoteIndex = notes.getChordIndex(this.noteRange.getEnd().root().getFrequency());
            boolean stretchToNoAdjacentSharps = false;

            if (endNoteIndex - startNoteIndex < 10) {
                // there are not many notes, stretch them down to the nice gappy bits
                stretchToNoAdjacentSharps = true;
            }

            // if the starting key is a sharp or has a flat, move down away from it
            while (this.startNoteIndex > 0 &&
                    (this.noteRange.getStart().hasSharp() ||
                            (stretchToNoAdjacentSharps && notes.getChord(this.startNoteIndex - 1).hasSharp()))) {
                // while there are notes before the start and the start is a sharp or there is a sharp
                // before it, keep looking further down the scale
                this.noteRange.setStart(notes.getChord(--this.startNoteIndex));
            }

            // do the end note too

            // while the end note is a sharp, or has a sharp - move up from it
            while (endNoteIndex < notes.getSize() - 1 &&
                    (this.noteRange.getEnd().hasSharp() ||
                            (stretchToNoAdjacentSharps && notes.getChord(endNoteIndex + 1).hasSharp()))) {
                // while there are notes after and the end is a sharp or has one, keep looking
                this.noteRange.setEnd(notes.getChord(++endNoteIndex));
            }

            // and setup the white keys accordingly
            int keyCount = 0;
            for (int i = 0; i < notes.getSize(); ++i) {
                if (keyCount > 0) {
                    // started counting, count this one
                    if (false == notes.getChord(i).hasSharp()) {
                        // this is a normal note, count these
                        ++keyCount;
                    }
                } else if (notes.getChord(i).equals(this.noteRange.getStart())) {
                    keyCount = 1;
                }
                // check to see if we have all our notes
                if (notes.getChord(i).equals(this.noteRange.getEnd())) {
                    // last one...
                    break;
                }
            }
            this.whiteKeyCount = keyCount;
            // and remember where to start
            this.initialWhiteKey = notes.getChord(this.startNoteIndex).root().getNotePrimativeIndex();
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

            // get the notes and draw them then...
            Chords notes = this.application.getSingleChords();

            // draw all the keys, go through twice, drawing white then black over the top of them
            // first let's go through the keys and draw in all the white ones
            int keyIndex = 0;
            int noteIndex = this.startNoteIndex;
            letterPaint.setTextSize(keyWidth * 0.6f);
            while (keyIndex < this.whiteKeyCount && noteIndex < notes.getSize()) {
                // loop through the notes finding all the white ones
                Chord currentNote = notes.getChord(noteIndex++);
                if (false == currentNote.hasSharp()) {
                    // this is a white key, draw this
                    RectF keyRect = new RectF(paddingLeft + (keyIndex * keyWidth),
                            paddingTop,
                            paddingLeft + ((keyIndex + 1) * keyWidth),
                            contentHeight - paddingBottom);
                    // draw this white key
                    drawKey(canvas, keyRect, currentNote);

                    if (isDrawNoteNames) {
                        canvas.drawText(isShowPrimatives ? "" + currentNote.root().getNotePrimative() : currentNote.getTitle(),
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
            while (keyIndex < this.whiteKeyCount - 1 && noteIndex < notes.getSize()) {
                // we will go through with reference to the white notes to offset the blacks better
                Chord currentNote = notes.getChord(noteIndex++);
                if (false == currentNote.hasSharp() && noteIndex < notes.getSize()) {
                    // this is a white key, is there a sharp to draw?
                    Chord blackNote = notes.getChord(noteIndex);
                    if (blackNote.hasSharp() && false == Float.isNaN(sharpOffsets[blackIndex])) {
                        // this is a sharp, draw this note now
                        blackLeft = paddingLeft + ((keyIndex + 1) * keyWidth) - (sharpWidth * sharpOffsets[blackIndex]);
                        // create the rect for this
                        RectF keyRect = new RectF(blackLeft,
                                paddingTop,
                                blackLeft + sharpWidth,
                                (contentHeight * 0.7f) - paddingBottom);
                        // draw it
                        drawKey(canvas, keyRect, blackNote);
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

    protected void drawKey(Canvas canvas, RectF keyRect, Chord keyNote) {
        // just draw the note in here
        if (false == keyNote.hasFlat() && false == keyNote.hasSharp()) {
            canvas.drawRect(keyRect, whitePaint);
        }
        else {
            canvas.drawRect(keyRect, blackPaint);
        }
    }

    public String getRangeText() {
        String range = "--";
        if (null != this.noteRange) {
            range = this.noteRange.toString();
        }
        return range;
    }
}

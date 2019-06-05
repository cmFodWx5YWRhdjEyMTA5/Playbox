package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.notes.Note;
import uk.co.darkerwaters.staveinvaders.notes.Range;

public class KeysView extends BaseView {

    private int whiteNoteCount = -1;

    public interface IKeysViewListener {
        void noteReleased(Chord chord);
        void noteDepressed(Chord chord);
    }

    private Range noteRange = null;

    protected ViewBounds bounds = null;

    protected final List<IKeysViewListener> listeners = new ArrayList<>();

    public KeysView(Context context) {
        super(context);
    }

    public KeysView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeysView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initialiseView(Context context) {
        // initialise the notes we are going to show on the piano
        setNoteRange(getDefaultNoteRange());
    }

    public Range getDefaultNoteRange() {
        return this.application.getSettings().getPianoLettersRange();
    }

    public void closeView() {
        // shut down this view
        synchronized (this.listeners) {
            this.listeners.clear();
        }
    }

    public boolean addListener(IKeysViewListener listener) {
        synchronized (this.listeners) {
            if (this.listeners.contains(listener)) {
                return false;
            }
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(IKeysViewListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    protected Range getNoteRange() {
        return this.noteRange;
    }

    public void setNoteRange(Range newRange) {
        // set the members to remember this range to display
        if (null != newRange && null != newRange.getStart() && null != newRange.getEnd()) {
            this.noteRange = newRange;
        }
        this.whiteNoteCount = -1;
    }

    public boolean isDrawNoteNames() {
        return true;
    }

    protected int getIndex(Chords chords, Note noteToFind) {
        // don't use the matching functions because these ignore key when that input is
        // used, as we always want to draw on the absolute correct line here, do our own
        // search for the index
        int noteIndex = -1;
        for (int i = 0; i < chords.getSize(); ++i) {
            Chord chord = chords.getChord(i);
            boolean isNoteContained = false;
            for (Note note : chord.notes) {
                if (note.exactEquals(noteToFind)) {
                    // this is the note
                    isNoteContained = true;
                    break;
                }
            }
            if (isNoteContained) {
                noteIndex = i;
                break;
            }
        }
        return noteIndex;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // create the bounds
        this.bounds = new ViewBounds();
        Assets assets = getAssets();
        // Draw a solid color on the canvas as background
        canvas.drawColor(Color.WHITE);

        if (null == this.noteRange) {
            setNoteRange(getDefaultNoteRange());
        }

        // get all the letters we want to draw, from A to G...
        Chords singleChords = this.application.getSingleChords();
        int iStart = getIndex(singleChords, this.noteRange.getStart().root());
        int iEnd = getIndex(singleChords, this.noteRange.getEnd().root());

        if (this.whiteNoteCount <= 0) {
            // calculate the note count again
            this.whiteNoteCount = 0;
            for (int noteIndex = iStart; noteIndex <= iEnd; ++noteIndex) {
                Chord chord = singleChords.getChord(noteIndex);
                if (null != chord && (false == chord.hasSharp() && false == chord.hasFlat())) {
                    ++whiteNoteCount;
                }
            }
        }
        // now draw these notes in
        if (whiteNoteCount > 0) {
            // there are notes, divide the space
            float keyWidth = this.bounds.drawingWidth / whiteNoteCount;
            float halfKey = keyWidth * 0.5f;
            float keyHeight = this.bounds.drawingHeight * 0.6f;
            float keyBottom = this.bounds.drawingBottom - keyHeight * 0.3f;
            int i = 0;
            RectF keyRect;
            for (int noteIndex = iStart; noteIndex <= iEnd; ++noteIndex) {
                // draw this note in the correct position
                Chord chord = singleChords.getChord(noteIndex);
                if (null != chord) {
                    int letterColor = assets.letterPaint.getColor();
                    char noteLetter = chord.root().getNotePrimitive();
                    // draw in the normal note here (the white note)
                    if (false == chord.hasFlat() && false == chord.hasSharp()) {
                        keyRect = new RectF(
                                bounds.drawingLeft + (i * keyWidth),
                                bounds.drawingBottom - keyHeight,
                                bounds.drawingLeft + ((i + 1) * keyWidth),
                                keyBottom);
                        // draw the key (white key)
                        drawKey(canvas, keyRect, chord);
                        // and the letter for the note
                        canvas.drawText(Character.toString(noteLetter), keyRect.centerX(), keyRect.centerY(), assets.letterPaint);
                        // move on the white note counter
                        ++i;
                    }
                    else {
                        // draw in the sharp here (the black note)
                        keyRect = new RectF(
                                bounds.drawingLeft + ((i - 1) * keyWidth) + halfKey,
                                bounds.drawingBottom - keyHeight * 1.5f,
                                bounds.drawingLeft + (i * keyWidth) + halfKey,
                                bounds.drawingBottom - keyHeight);
                        drawKey(canvas, keyRect, chord);
                        assets.letterPaint.setColor(Color.WHITE);
                        canvas.drawText(Character.toString(noteLetter) + '#', keyRect.centerX(), keyRect.centerY(), assets.letterPaint);
                        canvas.drawText(Character.toString(nextLetter(noteLetter)) + 'ᵇ', keyRect.centerX(), keyRect.centerY() + assets.letterPaint.getTextSize(), assets.letterPaint);
                        // put the color back
                        assets.letterPaint.setColor(letterColor);
                    }
                }
            }
        }
    }

    private char nextLetter(char noteLetter) {
        if (noteLetter == 'G') {
            return 'A';
        }
        else {
            return (char)(noteLetter + 1);
        }
    }

    protected void drawKey(Canvas canvas, RectF keyRect, Chord keyNote) {
        // just draw the note in here
        canvas.drawRect(keyRect, getAssets().objectPaint);
    }
}

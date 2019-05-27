package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Note;
import uk.co.darkerwaters.staveinvaders.notes.Range;

public class KeysView extends BaseView {

    public interface IKeysViewListener {
        void noteReleased(Chord chord);
        void noteDepressed(Chord chord);
    }

    private Range noteRange = null;

    protected ViewBounds bounds = null;

    protected final List<IKeysViewListener> listeners = new ArrayList<IKeysViewListener>();

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
        Chords notes = this.application.getSingleChords();
        return new Range(notes.getChord("C3"), notes.getChord("B5"));
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
        Chords notes = this.application.getSingleChords();
        if (null != newRange && null != newRange.getStart() && null != newRange.getEnd()) {
            this.noteRange = newRange;

            // set the starting key, we don't want to start on a sharp or just after one
            // as we won't draw it and it will look and behave weird, go down until we get to
            // a normal kind of note (a white key without a flat, which is a sharp before it)
            // basically this is an F or a C then
            int startNoteIndex = notes.getChordIndex(this.noteRange.getStart().root());
            int endNoteIndex = notes.getChordIndex(this.noteRange.getEnd().root());
            boolean stretchToNoAdjacentSharps = false;

            if (endNoteIndex - startNoteIndex < 10) {
                // there are not many notes, stretch them down to the nice gappy bits
                stretchToNoAdjacentSharps = true;
            }

            // if the starting key is a sharp or has a flat, move down away from it
            while (startNoteIndex > 0 && (this.noteRange.getStart().hasSharp() ||
                    (stretchToNoAdjacentSharps && notes.getChord(startNoteIndex - 1).hasSharp()))) {
                // while there are notes before the start and the start is a sharp or there is a sharp
                // before it, keep looking further down the scale
                this.noteRange.setStart(notes.getChord(--startNoteIndex));
            }

            // while the end note is a sharp, or has a sharp - move up from it
            while (endNoteIndex < notes.getSize() - 1 &&
                    (this.noteRange.getEnd().hasSharp() ||
                            (stretchToNoAdjacentSharps && notes.getChord(endNoteIndex + 1).hasSharp()))) {
                // while there are notes after and the end is a sharp or has one, keep looking
                this.noteRange.setEnd(notes.getChord(++endNoteIndex));
            }
        }
    }

    public boolean isDrawNoteNames() {
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // create the bounds
        this.bounds = new ViewBounds();
        Assets assets = getAssets();
        // Draw a solid color on the canvas as background
        canvas.drawColor(Color.WHITE);

        // get all the letters we want to draw, from A to G...
        char lowestLetter = 'A';
        char highestLetter = 'G';

        Chords singleChords = this.application.getSingleChords();
        int iStart = singleChords.getChordIndex("A3");
        int iEnd = singleChords.getChordIndex("G4");

        // now draw these notes in
        int noteCount = highestLetter - lowestLetter + 1;
        if (noteCount > 0) {
            // there are notes, divide the space
            float keyWidth = this.bounds.drawingWidth / noteCount;
            float halfKey = keyWidth * 0.5f;
            float keyHeight = this.bounds.drawingHeight * 0.6f;
            float keyBottom = this.bounds.drawingBottom - keyHeight * 0.3f;
            char noteLetter = lowestLetter;
            for (int i = 0; i < noteCount; ++i) {
                // find a note in the range that represents this letter
                Chord chord = null;
                Chord sharp = null;
                for (int j = iStart; j <= iEnd; ++j) {
                    chord = singleChords.getChord(j);
                    if (false == chord.hasFlat() && false == chord.hasSharp()) {
                        if (chord.root().getNotePrimative() == noteLetter) {
                            // this is a chord that represents the letter to draw
                            if (j < singleChords.getSize() - 2) {
                                sharp = singleChords.getChord(j + 1);
                                if (!sharp.hasSharp()) {
                                    sharp = null;
                                }
                            }
                            break;
                        }
                    }
                }
                if (null != chord) {
                    RectF keyRect = new RectF(
                            bounds.drawingLeft + (i * keyWidth),
                            bounds.drawingBottom - keyHeight,
                            bounds.drawingLeft + ((i + 1) * keyWidth),
                            keyBottom);
                    // draw the key (white key)
                    drawKey(canvas, keyRect, chord);
                    int letterColor = assets.letterPaint.getColor();
                    canvas.drawText(Character.toString(noteLetter), keyRect.centerX(), keyRect.centerY(), assets.letterPaint);
                    if (null != sharp && i < noteCount - 1) {
                        // draw in the sharp
                        keyRect = new RectF(
                                bounds.drawingLeft + (i * keyWidth) + halfKey,
                                bounds.drawingBottom - keyHeight * 1.5f,
                                bounds.drawingLeft + ((i + 1) * keyWidth) + halfKey,
                                bounds.drawingBottom - keyHeight);
                        drawKey(canvas, keyRect, sharp);
                        assets.letterPaint.setColor(Color.WHITE);
                        canvas.drawText(Character.toString(noteLetter) + '#', keyRect.centerX(), keyRect.centerY(), assets.letterPaint);
                        canvas.drawText(Character.toString(nextLetter(noteLetter)) + 'ᵇ', keyRect.centerX(), keyRect.centerY() + assets.letterPaint.getTextSize(), assets.letterPaint);
                        // put the color back
                        assets.letterPaint.setColor(letterColor);
                    }
                }
                // move onto the next letter
                ++noteLetter;
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

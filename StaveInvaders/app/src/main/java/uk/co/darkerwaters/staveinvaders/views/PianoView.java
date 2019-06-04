package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;

import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Note;
import uk.co.darkerwaters.staveinvaders.notes.Range;

public class PianoView extends KeysView {

    private static final float K_VIEW_HEIGHT_FACTOR = 3f;

    private class PianoViewBounds extends ViewBounds {
        PianoViewBounds() {
            super();
        }

        @Override
        float calculateYBorder() {
            // this is overridden because on this view we want to make sure we
            // are super skinny, the width of drawing needs to be a lot more
            // than the height to show it properly
            if (this.viewWidth < this.viewHeight * K_VIEW_HEIGHT_FACTOR) {
                // we are not wide enough, increase the Y border to shrink it down
                float requiredHeight = this.viewWidth / K_VIEW_HEIGHT_FACTOR;
                return (this.viewHeight - requiredHeight) * 0.5f;
            }
            else {
                // just a little more than the base
                return this.viewHeight * 0.15f;
            }
        }
    }

    private int whiteKeyCount = 0;

    private float whiteKeyOffset = 0f;
    private int startNoteIndex = 0;

    private boolean isShowPrimitives = true;

    private long lastDrawnTime = 0L;

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
    }

    public PianoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PianoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setNoteRange(Range newRange) {
        super.setNoteRange(newRange);
        // get the range as it is and check it
        Range noteRange = getNoteRange();
        if (null != noteRange && getIsPiano()) {
            Chords notes = this.application.getSingleChords();
            // set the starting key, we don't want to start on a sharp or just after one
            // as we won't draw it and it will look and behave weird, go down until we get to
            // a normal kind of note (a white key without a flat, which is a sharp before it)
            // basically this is an F or a C then
            int startNoteIndex = notes.getChordIndex(noteRange.getStart().root());
            int endNoteIndex = notes.getChordIndex(noteRange.getEnd().root());
            boolean stretchToNoAdjacentSharps = false;

            if (endNoteIndex - startNoteIndex < 10) {
                // there are not many notes, stretch them down to the nice gappy bits
                stretchToNoAdjacentSharps = true;
            }

            // if the starting key is a sharp or has a flat, move down away from it
            while (startNoteIndex > 0 && (noteRange.getStart().hasSharp() ||
                    (stretchToNoAdjacentSharps && notes.getChord(startNoteIndex - 1).hasSharp()))) {
                // while there are notes before the start and the start is a sharp or there is a sharp
                // before it, keep looking further down the scale
                noteRange.setStart(notes.getChord(--startNoteIndex));
            }

            // while the end note is a sharp, or has a sharp - move up from it
            while (endNoteIndex < notes.getSize() - 1 &&
                    (noteRange.getEnd().hasSharp() ||
                            (stretchToNoAdjacentSharps && notes.getChord(endNoteIndex + 1).hasSharp()))) {
                // while there are notes after and the end is a sharp or has one, keep looking
                noteRange.setEnd(notes.getChord(++endNoteIndex));
            }
            // set this adjusted range on the base
            super.setNoteRange(noteRange);
            // and setup the white keys accordingly
            int keyCount = 0;
            for (int i = 0; i < notes.getSize(); ++i) {
                if (keyCount > 0) {
                    // started counting, count this one
                    if (false == notes.getChord(i).hasSharp()) {
                        // this is a normal note, count these
                        ++keyCount;
                    }
                } else if (notes.getChord(i).equals(noteRange.getStart())) {
                    keyCount = 1;
                }
                // check to see if we have all our notes
                if (notes.getChord(i).equals(noteRange.getEnd())) {
                    // last one...
                    break;
                }
            }
            this.startNoteIndex = notes.getChordIndex(noteRange.getStart().root());
            this.whiteKeyCount = keyCount;
            // animate the movement of range from this time
            this.lastDrawnTime = System.currentTimeMillis();
        }
    }

    @Override
    public boolean isDrawNoteNames() {
        Settings settings = this.application.getSettings();
        return false == settings.getIsKeyInputPiano() || settings.getIsShowPianoLetters();
    }

    public boolean getIsPiano() {
        return this.application.getSettings().getIsKeyInputPiano();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // create the bounds for this piano view
        if (false == getIsPiano()) {
            // just let the base do its thing
            super.onDraw(canvas);
        }
        else {
            // Draw a solid color on the canvas as background
            canvas.drawColor(Color.WHITE);
            this.bounds = new PianoViewBounds();

            long currentTime = System.currentTimeMillis();
            if (this.lastDrawnTime == 0 || this.lastDrawnTime > currentTime) {
                // ignore this
                this.lastDrawnTime = currentTime;
            }
            float timeElapsed = (currentTime - this.lastDrawnTime) / 1000f;
            this.lastDrawnTime = currentTime;

            if (this.whiteKeyCount > 0) {
                // draw in all the keys
                float keyWidth = bounds.drawingWidth / this.whiteKeyCount;
                float sharpWidth = keyWidth * 0.4f;
                Chords notes = this.application.getSingleChords();

                // if there is an offset and it is not in the correct place, move it now
                calculateAnimation(keyWidth, timeElapsed, notes);

                // draw all the keys, go through twice, drawing white then black over the top of them
                // first let's go through the keys and draw in all the white ones
                int keyIndex = 0;
                // the note at the start is from our offset
                int noteIndex = (int) whiteKeyOffset;
                // less a little as never drawn bang on...
                float drawOffset = (whiteKeyOffset - noteIndex) * keyWidth;
                // also get the initial key to use here
                Note initialKey = notes.getChord(noteIndex).root();
                // and remember where to start
                int initialWhiteKey = initialKey.getNotePrimitiveIndex();
                Assets assets = getAssets();
                float textSize = assets.letterPaint.getTextSize();
                assets.letterPaint.setTextSize(keyWidth * 0.6f);
                while (keyIndex < this.whiteKeyCount && noteIndex < notes.getSize()) {
                    // loop through the notes finding all the white ones
                    Chord currentNote = notes.getChord(noteIndex++);
                    if (false == currentNote.hasSharp()) {
                        // this is a white key, draw this
                        RectF keyRect = new RectF(bounds.drawingLeft + (keyIndex * keyWidth) - drawOffset,
                                bounds.drawingTop,
                                bounds.drawingLeft + ((keyIndex + 1) * keyWidth) - drawOffset,
                                bounds.drawingBottom);
                        // draw this white key
                        drawKey(canvas, keyRect, currentNote);

                        if (isDrawNoteNames()) {
                            canvas.drawText(isShowPrimitives ? "" + currentNote.root().getNotePrimitive() : currentNote.getTitle(),
                                    keyRect.left + (keyWidth * 0.5f),
                                    keyRect.bottom - (keyWidth * 0.3f),
                                    assets.letterPaint);
                        }
                        // move on our white key counter
                        ++keyIndex;
                    }
                }
                assets.letterPaint.setTextSize(textSize);
                // now we need to go through again for the sharps and flats
                int blackIndex = initialWhiteKey;
                float blackLeft;
                keyIndex = 0;
                noteIndex = (int) whiteKeyOffset;
                while (keyIndex < this.whiteKeyCount - 1 && noteIndex < notes.getSize()) {
                    // we will go through with reference to the white notes to offset the blacks better
                    Chord currentNote = notes.getChord(noteIndex++);
                    if (false == currentNote.hasSharp() && noteIndex < notes.getSize()) {
                        // this is a white key, is there a sharp to draw?
                        Chord blackNote = notes.getChord(noteIndex);
                        if (blackNote.hasSharp() && false == Float.isNaN(sharpOffsets[blackIndex])) {
                            // this is a sharp, draw this note now
                            blackLeft = bounds.drawingLeft + ((keyIndex + 1) * keyWidth) - (sharpWidth * sharpOffsets[blackIndex]) - drawOffset;
                            // create the rect for this
                            RectF keyRect = new RectF(blackLeft,
                                    bounds.drawingTop,
                                    blackLeft + sharpWidth,
                                    bounds.drawingTop + (bounds.drawingHeight * 0.7f));
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
                // if we are animating then we need to keep invalidating the view to draw the movement
                if (this.startNoteIndex != (int) this.whiteKeyOffset || drawOffset != 0f) {
                    // we need another draw
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            invalidate();
                        }
                    }, 50);
                }
            }
        }
    }

    protected boolean calculateAnimation(float keyWidth, float timeElapsed, Chords singleChords) {
        // calculate the new offset to the white key to get to where
        // we want to really be
        float oldOffset = this.whiteKeyOffset;
        float difference = this.whiteKeyOffset - this.startNoteIndex;
        float sign = difference > 0f ? 1 : -1;
        // if the offset is not at the correct key, calculate the new offset
        if (difference != 0f) {
            // we are not in the correct place, remove the difference over time
            // which will make it move faster farther away, slowing to very slow
            this.whiteKeyOffset -= sign * Math.max(Math.abs(difference * timeElapsed), 10f * timeElapsed);
            int noteIndex = (int)this.whiteKeyOffset;
            // check that we have not moved over to a sharp
            if (noteIndex >= 0 && noteIndex < singleChords.getSize()) {
                Note initialKey = singleChords.getChord(noteIndex).root();
                if (initialKey.isSharp() || initialKey.isFlat()) {
                    // this is not a white key, move past the sharp
                    this.whiteKeyOffset -= sign;
                }
            }
            else {
                this.whiteKeyOffset = this.startNoteIndex;
            }
            if (difference < 0f) {
                // don't go past the target
                if (this.whiteKeyOffset > this.startNoteIndex) {
                    // too far
                    this.whiteKeyOffset = this.startNoteIndex;
                }
            }
            else {
                // or the other way
                if (this.whiteKeyOffset < this.startNoteIndex) {
                    // too far
                    this.whiteKeyOffset = this.startNoteIndex;
                }
            }
        }
        // return if this changed something
        return oldOffset != this.whiteKeyOffset;
    }

    @Override
    protected void drawKey(Canvas canvas, RectF keyRect, Chord keyNote) {
        // just draw the note in here
        if (false == keyNote.hasFlat() && false == keyNote.hasSharp()) {
            canvas.drawRect(keyRect, getAssets().whitePaint);
        }
        else {
            canvas.drawRect(keyRect, getAssets().blackPaint);
        }
    }
}

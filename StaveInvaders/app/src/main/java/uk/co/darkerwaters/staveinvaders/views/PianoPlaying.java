package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;


import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.notes.Chord;

public class PianoPlaying extends PianoView {

    private Paint redPaint;

    public PianoPlaying(Context context) {
        super(context);
    }

    public PianoPlaying(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PianoPlaying(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        super.init(context);

        // initialise the paint for the pressed keys
        this.redPaint = new Paint();
        this.redPaint.setStyle(Paint.Style.FILL);
        this.redPaint.setColor(getResources().getColor(R.color.colorKeyPress));
        this.redPaint.setAntiAlias(true);

    }

    @Override
    protected void drawKey(Canvas canvas, RectF keyRect, Chord keyNote) {
        // let the base class draw the key
        super.drawKey(canvas, keyRect, keyNote);
        // is it pressed?
        if (isNoteDepressed(keyNote)) {
            // highlight this pressed note
            canvas.drawRect(keyRect.left + 2,
                    keyRect.top + 2,
                    keyRect.right - 2,
                    keyRect.bottom - 2,
                    redPaint);
        }
    }

    protected boolean isNoteDepressed(Chord note) {
        return false;
    }
}

package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameNote;
import uk.co.darkerwaters.staveinvaders.games.GamePlayer;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.Chords;
import uk.co.darkerwaters.staveinvaders.notes.Note;

public class MusicViewPlaying extends MusicView {


    public MusicViewPlaying(Context context) {
        super(context);
    }

    public MusicViewPlaying(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicViewPlaying(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MusicViewPlaying(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void initialiseView(Context context) {
        super.initialiseView(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Assets assets = getAssets();

        // now we can draw our gun and handle all that business
        float gunWidth = clefWidth * 0.5f;
        canvas.drawOval(this.bounds.drawingLeft,
                this.bounds.contentHeight - gunWidth,
                this.bounds.drawingLeft + gunWidth,
                this.bounds.contentHeight,
                assets.blackPaint);
        canvas.drawRoundRect(this.bounds.drawingLeft,
                this.bounds.contentHeight - gunWidth * 0.5f,
                this.bounds.drawingLeft + gunWidth,
                this.bounds.contentHeight,
                10f, 10f,
                assets.blackPaint);
    }
}

package uk.co.darkerwaters.noteinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.state.ActiveScore;
import uk.co.darkerwaters.noteinvaders.state.Note;
import uk.co.darkerwaters.noteinvaders.state.Playable;
import uk.co.darkerwaters.noteinvaders.state.State;

public class NotesGraphView extends View {

    private Paint graphPaint;
    private Paint notePaint;

    public NotesGraphView(Context context) {
        super(context);
        init(context);
    }

    public NotesGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NotesGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void closeView() {

    }

    protected void init(final Context context) {
        // Initialize new paints
        this.graphPaint = new Paint();
        this.graphPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.graphPaint.setStrokeWidth(getResources().getDimension(R.dimen.music_line_stroke));
        this.graphPaint.setColor(Color.BLACK);
        this.graphPaint.setAntiAlias(true);

        // and to draw the letters
        this.notePaint = new Paint();
        this.notePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.notePaint.setColor(getResources().getColor(R.color.colorMiss));
        this.notePaint.setAlpha(100);
        this.notePaint.setAntiAlias(true);
    }

    protected Rect getCanvasPadding() {
        // adding a little to the top to draw the top note and bottom notes
        return new Rect(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw a solid color on the canvas as background
        canvas.drawColor(Color.WHITE);

        ActiveScore activeScore = State.getInstance().getCurrentActiveScore();
        Playable[] notesMissed = activeScore.getNotesMissed();

        // draw the axis
        Rect padding = getCanvasPadding();
        Rect graphRect = new Rect(padding.left, padding.top,
                canvas.getWidth() - padding.right,
                canvas.getHeight() - padding.bottom);
        // the x axis
        canvas.drawLine(graphRect.left,
                graphRect.bottom,
                graphRect.right,
                graphRect.bottom,
                graphPaint);
        // the y axis
        canvas.drawLine(graphRect.left,
                graphRect.bottom,
                graphRect.left,
                graphRect.top,
                graphPaint);

        // now all the items
        if (notesMissed.length > 0) {
            // find the max number
            int maxFrequency = 2;
            int noNotesFound = 0;
            for (Playable note : notesMissed) {
                // find the max of the misses and false shots combined
                int frequency = activeScore.getNoteMissedFrequency(note) +
                        activeScore.getNoteFalselyShotFrequency(note);
                maxFrequency = Math.max(frequency, maxFrequency);
                if (note instanceof Note) {
                    ++noNotesFound;
                }
            }
            if (noNotesFound > 0) {
                float noteWidth = graphRect.width() / noNotesFound;
                float xStart = graphRect.left;
                float frequencyHeight = (graphRect.height() - padding.height()) / maxFrequency;
                graphPaint.setTextSize(noteWidth * 0.3f);
                for (int i = 0; i < notesMissed.length; ++i) {
                    // for each note, draw in the bar
                    if (notesMissed[i] instanceof Note) {
                        float missedbarHeight = frequencyHeight * activeScore.getNoteMissedFrequency(notesMissed[i]);
                        this.notePaint.setColor(getResources().getColor(R.color.colorMiss));
                        canvas.drawRect(xStart + 5f,
                                graphRect.bottom - missedbarHeight,
                                xStart + noteWidth,
                                graphRect.bottom,
                                notePaint);
                        // and the false shots on top of this
                        float falseHeight = frequencyHeight * activeScore.getNoteFalselyShotFrequency(notesMissed[i]);
                        this.notePaint.setColor(getResources().getColor(R.color.colorFalseFire));
                        canvas.drawRect(xStart + 5f,
                                graphRect.bottom - (missedbarHeight + falseHeight),
                                xStart + noteWidth,
                                graphRect.bottom - missedbarHeight,
                                notePaint);
                        // put the name in
                        canvas.drawText(notesMissed[i].getName(0),
                                xStart + (noteWidth * 0.1f),
                                graphRect.bottom - (noteWidth * 0.1f),
                                graphPaint);
                        xStart += noteWidth;
                    }
                }
            }
        }
    }
}

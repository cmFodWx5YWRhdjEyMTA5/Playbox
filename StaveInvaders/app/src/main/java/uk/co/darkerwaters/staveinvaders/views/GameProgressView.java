package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.games.Game;

/**
 * TODO: document your custom view class.
 */
public class GameProgressView extends View {

    private Paint outlinePaint;
    private Paint letterPaint;
    private Game game;

    public GameProgressView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        this.outlinePaint = new Paint();
        this.outlinePaint.setStyle(Paint.Style.STROKE);
        this.outlinePaint.setStrokeWidth(getResources().getDimension(R.dimen.music_line_stroke));
        this.outlinePaint.setColor(getResources().getColor(R.color.colorFalseFire));
        this.outlinePaint.setAntiAlias(true);

        this.letterPaint = new Paint();
        this.letterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.letterPaint.setStrokeWidth(getResources().getDimension(R.dimen.letter_stroke));
        this.letterPaint.setColor(getResources().getColor(R.color.colorLaser));
        this.letterPaint.setAntiAlias(true);
        this.letterPaint.setTextSize(36f);
    }

    public void setViewData(Game game) {
        // set the data to show on this view
        this.game = game;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        float contentWidth = getWidth() - paddingLeft - paddingRight;
        float contentHeight = getHeight() - paddingTop - paddingBottom;

        float border = contentHeight * 0.1f;

        // draw the outline of the view
        canvas.drawRoundRect(0f, 0f, contentWidth, contentHeight, border, border, outlinePaint);

        // draw the game count
        String gameCount = "0/" + this.game.children.length;
        float letterLeft = contentWidth - border - letterPaint.measureText(gameCount);
        canvas.drawText(gameCount, letterLeft, contentHeight - border, letterPaint);

        float graphRight = letterLeft - border;
        float graphLeft = border;
        float graphBottom = contentHeight - border;
        float graphTop = border;
        //canvas.drawLine(graphLeft, graphBottom, graphRight, graphBottom, outlinePaint);
        //canvas.drawLine(graphLeft, graphBottom, graphLeft, graphTop, outlinePaint);

        if (this.game.children.length > 0) {
            // draw all the child progress in
            float entryWidth = (graphRight - graphLeft) / this.game.children.length;
            for (int i = 0; i < this.game.children.length; ++i) {
                float progress = this.game.children[i].getGameProgress();
                float barHeight = (graphBottom - graphTop) * progress;
                if (barHeight <= 0f) {
                    // show a line of nothing
                    barHeight = outlinePaint.getStrokeWidth();
                }
                canvas.drawRoundRect(graphLeft + i * entryWidth,
                        graphBottom - barHeight,
                        graphLeft + i * entryWidth + (0.9f * entryWidth),
                        graphBottom,
                        border, border,
                        outlinePaint);
            }
        }

    }
}

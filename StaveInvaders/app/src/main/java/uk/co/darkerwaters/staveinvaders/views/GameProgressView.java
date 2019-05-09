package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
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
    private Paint xAxisPaint;
    private Paint barPaint;

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

        this.barPaint = new Paint();
        this.barPaint.setAntiAlias(true);
        this.barPaint.setStyle(Paint.Style.FILL);

        this.xAxisPaint = new Paint();
        this.xAxisPaint.setAntiAlias(true);
        this.xAxisPaint.setStrokeWidth(getResources().getDimension(R.dimen.letter_stroke));

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
        this.letterPaint.setTextScaleX(1f);
        canvas.drawText(gameCount, letterLeft, contentHeight - border, letterPaint);


        Rect textBounds = new Rect();
        this.letterPaint.getTextBounds(gameCount, 0, gameCount.length() - 1, textBounds);

        float graphRight = letterLeft - border;
        float graphLeft = border;
        float graphTop = border + textBounds.height();
        float graphBottom = contentHeight - (border * 2f);
        //canvas.drawLine(graphLeft, graphBottom, graphRight, graphBottom, outlinePaint);
        //canvas.drawLine(graphLeft, graphBottom, graphLeft, graphTop, outlinePaint);

        // create the path for the x-axis path to draw
        Path axisPath = new Path();
        axisPath.moveTo(graphLeft, graphBottom);
        axisPath.lineTo(graphRight, graphBottom);
        axisPath.lineTo(graphRight, graphBottom + border);
        axisPath.lineTo(graphLeft, graphBottom);
        // and the shader to draw this nicely
        this.xAxisPaint.setShader(new LinearGradient(graphLeft, graphBottom, graphRight, graphBottom + border, Color.WHITE, this.letterPaint.getColor(), Shader.TileMode.MIRROR));
        // and draw it
        canvas.drawPath(axisPath, xAxisPaint);

        // now draw the parts of the graph to display

        if (this.game.children.length > 0) {
            // draw all the child progress in
            float entryWidth = (graphRight - graphLeft) / this.game.children.length;
            for (int i = 0; i < this.game.children.length; ++i) {
                float progress = 1f;//this.game.children[i].getGameProgress();
                float barHeight = (graphBottom - graphTop) * progress;
                if (barHeight <= 0f) {
                    // show a line of nothing
                    barHeight = outlinePaint.getStrokeWidth();
                }
                String progressText = "60";//this.game.getTopTempo();

                RectF barRect = new RectF(graphLeft + i * entryWidth,
                        graphBottom - barHeight,
                        graphLeft + i * entryWidth + (0.8f * entryWidth),
                        graphBottom);
                // create the shader
                this.barPaint.setShader(new LinearGradient(barRect.left, barRect.bottom, barRect.right, barRect.top, this.outlinePaint.getColor(), Color.WHITE, Shader.TileMode.MIRROR));
                canvas.drawRoundRect( barRect, border, border, this.barPaint);

                String topBpm = "60"; //this.game.getTopTempo();
                this.letterPaint.setTextScaleX(1f);
                this.letterPaint.setTextScaleX(Math.min(barRect.width() / letterPaint.measureText(topBpm), 1f));
                canvas.drawText(topBpm, barRect.left, barRect.top - textBounds.height() * 0.5f, letterPaint);
            }
        }

    }
}

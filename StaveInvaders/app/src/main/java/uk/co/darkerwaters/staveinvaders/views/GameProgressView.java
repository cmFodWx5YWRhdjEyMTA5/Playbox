package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.games.Game;

/**
 * TODO: document your custom view class.
 */
public class GameProgressView extends View {

    private Paint backgroundPaint;
    private Paint outlinePaint;
    private Paint letterPaint;
    private Paint xAxisPaint;
    private Paint barPaint;
    private Paint highlightPaint;

    private Game game;
    private String xAxisTitle;
    private String yAxisTitle;

    private boolean isDrawBpmValues = false;

    private Game selectedChild = null;

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

        this.xAxisTitle = getResources().getText(R.string.xAxis).toString();
        this.yAxisTitle = getResources().getText(R.string.yAxis).toString();

        this.backgroundPaint = new Paint();
        this.backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.backgroundPaint.setStrokeWidth(getResources().getDimension(R.dimen.music_line_stroke));
        this.backgroundPaint.setColor(getResources().getColor(R.color.secondaryDarkColor));
        this.backgroundPaint.setAntiAlias(true);

        this.outlinePaint = new Paint();
        this.outlinePaint.setStyle(Paint.Style.STROKE);
        this.outlinePaint.setStrokeWidth(1f);
        this.outlinePaint.setColor(getResources().getColor(R.color.secondaryLightColor));
        this.outlinePaint.setAntiAlias(true);
        this.outlinePaint.setTextSize(20f);

        this.highlightPaint = new Paint();
        this.highlightPaint.setStyle(Paint.Style.STROKE);
        this.highlightPaint.setStrokeWidth(getResources().getDimension(R.dimen.music_line_stroke));
        this.highlightPaint.setColor(getResources().getColor(R.color.secondaryTextColor));
        this.highlightPaint.setAntiAlias(true);
        this.highlightPaint.setAlpha(150);

        this.letterPaint = new Paint();
        this.letterPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.letterPaint.setStrokeWidth(getResources().getDimension(R.dimen.letter_stroke));
        this.letterPaint.setColor(getResources().getColor(R.color.secondaryTextColor));
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
        canvas.drawRoundRect(0f, 0f, contentWidth, contentHeight, border, border, backgroundPaint);

        // draw the game count
        String gameCount = "0/" + this.game.children.length;
        float letterLeft = contentWidth - border - letterPaint.measureText(gameCount);
        this.letterPaint.setTextScaleX(1f);
        canvas.drawText(gameCount, letterLeft, contentHeight - border, letterPaint);


        Rect textBounds = new Rect();
        this.outlinePaint.getTextBounds(gameCount, 0, gameCount.length() - 1, textBounds);

        float graphRight = letterLeft - border;
        float graphLeft = border + outlinePaint.getTextSize();
        float graphTop = border + (isDrawBpmValues ? textBounds.height() : 0);
        float graphBottom = contentHeight - (border * 2.5f);
        //canvas.drawLine(graphLeft, graphBottom, graphRight, graphBottom, outlinePaint);
        //canvas.drawLine(graphLeft, graphBottom, graphLeft, graphTop, outlinePaint);

        // create the path for the x-axis path to draw
        Path axisPath = new Path();
        axisPath.moveTo(graphLeft + border, graphBottom);
        axisPath.lineTo(graphRight, graphBottom);
        axisPath.lineTo(graphRight, graphBottom + border * 2f);
        axisPath.lineTo(graphLeft + border, graphBottom);
        // and the shader to draw this nicely
        this.xAxisPaint.setShader(new LinearGradient(graphLeft,
                graphBottom,
                graphRight,
                graphBottom + border,
                getResources().getColor(R.color.secondaryLightColor),
                getResources().getColor(R.color.secondaryDarkColor),
                Shader.TileMode.MIRROR));
        // draw around the graph here
        canvas.drawRoundRect(graphLeft, graphTop, graphRight, graphBottom, border, border, outlinePaint);
        // and draw the faded triangle
        canvas.drawPath(axisPath, xAxisPaint);

        // now draw the parts of the graph to display

        if (this.game.children.length > 0) {
            // draw all the child progress in
            float entryWidth = (graphRight - graphLeft) / this.game.children.length;
            for (int i = 0; i < this.game.children.length; ++i) {
                float progress = this.game.children[i].getGameProgress();
                float barHeight = (graphBottom - graphTop) * progress;
                if (barHeight <= 0f) {
                    // show a line of nothing
                    barHeight = backgroundPaint.getStrokeWidth();
                }

                RectF barRect = new RectF(graphLeft + i * entryWidth,
                        graphBottom - barHeight,
                        graphLeft + i * entryWidth + (0.8f * entryWidth),
                        graphBottom);
                // create the shader
                this.barPaint.setShader(new LinearGradient(
                        barRect.left,
                        barRect.bottom,
                        barRect.right,
                        barRect.top,
                        getResources().getColor(R.color.secondaryDarkColor),
                        getResources().getColor(R.color.secondaryLightColor),
                        Shader.TileMode.MIRROR));

                canvas.drawRoundRect(barRect, border, border, this.barPaint);
                if (this.game.children[i] == this.selectedChild) {
                    // this is the selected child game
                    barRect.top = graphTop;
                    canvas.drawRoundRect(barRect, border, border, this.highlightPaint);
                }

                if (isDrawBpmValues) {
                    //draw the top BPM in on top of the bar here
                    String progressText = "" + (int) (progress * 600f) / 10;//this.game.getTopTempo();
                    this.letterPaint.setTextScaleX(1f);
                    this.letterPaint.setTextScaleX(Math.min(barRect.width() / letterPaint.measureText(progressText), 1f));
                    canvas.drawText(progressText, barRect.left, barRect.top - textBounds.height() * 0.5f, letterPaint);
                }
            }

            // draw the graph titles here
            this.outlinePaint.setTextScaleX(1f);
            canvas.drawText(xAxisTitle, (contentWidth - outlinePaint.measureText(xAxisTitle)) * 0.5f, contentHeight - border * 0.5f, outlinePaint);
            canvas.save();
            canvas.rotate(-90f, 0, 0);
            canvas.drawText(yAxisTitle, (contentHeight + outlinePaint.measureText(xAxisTitle)) * -0.5f, outlinePaint.getTextSize(), outlinePaint);
            canvas.restore();
        }

    }

    public void setSelectedChild(Game selectedGame) {
        this.selectedChild = selectedGame;
    }
}

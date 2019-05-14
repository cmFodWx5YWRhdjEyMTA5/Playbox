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
public class GameProgressView extends BaseView {

    private Paint xAxisPaint;
    private Paint barPaint;

    private Game game;
    private String xAxisTitle;
    private String yAxisTitle;

    private boolean isDrawBpmValues = false;

    private Game selectedChild = null;

    public GameProgressView(Context context) {
        super(context);
    }

    public GameProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initialiseView(Context context) {
        // initialise the extras this view requires
        this.xAxisTitle = getResources().getText(R.string.xAxis).toString();
        this.yAxisTitle = getResources().getText(R.string.yAxis).toString();

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

        // get our assets for paints etc
        Assets assets = getAssets();

        ViewBounds bounds = new ViewBounds();

        // draw the outline of the view
        canvas.drawRoundRect(bounds.paddingLeft, bounds.paddingTop, bounds.contentWidth, bounds.contentHeight, bounds.border, bounds.border, assets.backgroundPaint);

        // draw the game count
        String gameCount = "0/" + this.game.children.length;
        float letterLeft = bounds.drawingRight - assets.letterPaint.measureText(gameCount);
        assets.letterPaint.setTextScaleX(1f);
        canvas.drawText(gameCount, letterLeft, bounds.drawingBottom, assets.letterPaint);


        Rect textBounds = new Rect();
        assets.outlinePaint.getTextBounds(gameCount, 0, gameCount.length() - 1, textBounds);

        float graphRight = letterLeft - bounds.border;
        float graphLeft = bounds.drawingLeft + assets.outlinePaint.getTextSize();
        float graphTop = bounds.drawingTop + (isDrawBpmValues ? textBounds.height() : 0);
        float graphBottom = bounds.drawingBottom - (bounds.border * 2.5f);
        //canvas.drawLine(graphLeft, graphBottom, graphRight, graphBottom, outlinePaint);
        //canvas.drawLine(graphLeft, graphBottom, graphLeft, graphTop, outlinePaint);

        // create the path for the x-axis path to draw
        Path axisPath = new Path();
        axisPath.moveTo(graphLeft + bounds.border, graphBottom);
        axisPath.lineTo(graphRight, graphBottom);
        axisPath.lineTo(graphRight, graphBottom + bounds.border * 2f);
        axisPath.lineTo(graphLeft + bounds.border, graphBottom);
        // and the shader to draw this nicely
        this.xAxisPaint.setShader(new LinearGradient(graphLeft,
                graphBottom,
                graphRight,
                graphBottom + bounds.border,
                getResources().getColor(R.color.secondaryLightColor),
                getResources().getColor(R.color.secondaryDarkColor),
                Shader.TileMode.MIRROR));
        // draw around the graph here
        canvas.drawRoundRect(graphLeft, graphTop, graphRight, graphBottom, bounds.border, bounds.border, assets.outlinePaint);
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
                    barHeight = assets.backgroundPaint.getStrokeWidth();
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

                canvas.drawRoundRect(barRect, bounds.border, bounds.border, this.barPaint);
                if (this.game.children[i] == this.selectedChild) {
                    // this is the selected child game
                    barRect.top = graphTop;
                    canvas.drawRoundRect(barRect, bounds.border, bounds.border, assets.highlightPaint);
                }

                if (isDrawBpmValues) {
                    //draw the top BPM in on top of the bar here
                    String progressText = "" + (int) (progress * 600f) / 10;//this.game.getTopTempo();
                    assets.letterPaint.setTextScaleX(1f);
                    assets.letterPaint.setTextScaleX(Math.min(barRect.width() / assets.letterPaint.measureText(progressText), 1f));
                    canvas.drawText(progressText,
                            barRect.left,
                            barRect.top - textBounds.height() * 0.5f,
                            assets.letterPaint);
                }
            }

            // draw the graph titles here
            assets.outlinePaint.setTextScaleX(1f);
            canvas.drawText(xAxisTitle,
                    (bounds.contentWidth - assets.outlinePaint.measureText(xAxisTitle)) * 0.5f,
                    bounds.contentHeight - bounds.border * 0.5f,
                    assets.outlinePaint);
            canvas.save();
            canvas.rotate(-90f, 0, 0);
            canvas.drawText(yAxisTitle,
                    (bounds.contentHeight + assets.outlinePaint.measureText(xAxisTitle)) * -0.5f,
                    assets.outlinePaint.getTextSize(), assets.outlinePaint);
            canvas.restore();
        }

    }

    public void setSelectedChild(Game selectedGame) {
        this.selectedChild = selectedGame;
    }
}

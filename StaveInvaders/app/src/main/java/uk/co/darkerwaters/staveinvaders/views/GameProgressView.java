package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Scores;
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
        this.barPaint.setColor(getResources().getColor(R.color.primaryColor));
        this.barPaint.setShadowLayer(12, 6, 6, getResources().getColor(R.color.primaryDarkColor));
        setLayerType(LAYER_TYPE_SOFTWARE, this.barPaint);

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

        // show the scores for the currently selected clefs
        MusicView.Clefs[] selectedClefs = this.application.getSettings().getSelectedClefs();

        // draw the game count, how many are passed?
        int gamesPassed = 0;
        for (int i = 0; i < this.game.children.length; ++i) {
            // get the passed game count for this clef
            boolean isPassed = true;
            for (MusicView.Clefs clef : selectedClefs) {
                if (false == this.game.children[i].getIsGamePassed(clef)) {
                    // this game is not passed
                    isPassed = false;
                    break;
                }
            }
            if (isPassed) {
                // this game was passed on the active clefs, add
                ++gamesPassed;
            }
        }
        String gameCount = gamesPassed + "/" + this.game.children.length;
        float letterLeft = bounds.drawingRight - assets.letterPaint.measureText(gameCount) * 0.5f;

        Rect textBounds = new Rect();
        assets.letterPaint.getTextBounds(gameCount, 0, gameCount.length() - 1, textBounds);

        float graphRight = bounds.drawingRight - bounds.border;
        float graphLeft = bounds.drawingLeft + assets.letterPaint.getTextSize();
        float graphTop = bounds.drawingTop + (isDrawBpmValues ? textBounds.height() : 0);
        float graphBottom = bounds.drawingBottom - (bounds.border * 2.5f);

        // draw around the graph here
        //canvas.drawRoundRect(graphLeft, graphTop, graphRight, graphBottom, bounds.border, bounds.border, assets.outlinePaint);

        // now draw the parts of the graph to display
        if (this.game.children.length > 0 && selectedClefs.length > 0) {
            // draw all the child progress in
            float entryWidth = (graphRight - graphLeft) / this.game.children.length;
            for (int i = 0; i < this.game.children.length; ++i) {
                // create the rect for the bar
                RectF barRect = new RectF(graphLeft + i * entryWidth,
                        graphTop,
                        graphLeft + i * entryWidth + 0.8f * entryWidth,
                        graphBottom);
                float barWidth = barRect.width() / selectedClefs.length;
                int topTempo = 0;
                float topBarHeight = 0f;
                for (int j = 0; j < selectedClefs.length; ++j) {
                    // for each clef, draw the bar
                    float progress = this.game.children[i].getGameProgress(selectedClefs[j]);
                    float barHeight = barRect.height() * progress;
                    topBarHeight = Math.max(barHeight, topBarHeight);
                    topTempo = Math.max(this.game.children[i].getGameTopTempo(selectedClefs[j]), topTempo);
                    if (barHeight <= 0f) {
                        // show a line of nothing
                        barHeight = assets.backgroundPaint.getStrokeWidth();
                    }
                    RectF clefRect = new RectF(barRect.left + (barWidth * j),
                            barRect.bottom - barHeight,
                            barRect.left + (barWidth * (j + 1)),
                            barRect.bottom);
                    canvas.drawRoundRect(clefRect, bounds.border, bounds.border, this.barPaint);
                }
                if (this.game.children[i] == this.selectedChild) {
                    // this is the selected child game
                    barRect.top = graphTop;
                    canvas.drawRoundRect(barRect, bounds.border, bounds.border, assets.highlightPaint);
                    // and to the level at which we would pass the bar
                    float passLine = barRect.bottom - Scores.K_PASS_BPM_FACTOR * barRect.height();
                    canvas.drawLine(barRect.left, passLine, barRect.right, passLine, assets.blackPaint);
                }

                if (isDrawBpmValues) {
                    //draw the top BPM in on top of the bar here
                    String progressText = Integer.toString(topTempo);
                    assets.letterPaint.setTextScaleX(1f);
                    assets.letterPaint.setTextScaleX(Math.min(barRect.width() / assets.letterPaint.measureText(progressText), 1f));
                    canvas.drawText(progressText,
                            barRect.centerX(),
                            topBarHeight - textBounds.height() * 0.5f,
                            assets.letterPaint);
                }
            }

            // draw the graph titles here
            canvas.drawText(xAxisTitle,
                    (bounds.contentWidth - assets.letterPaint.measureText(xAxisTitle)) * 0.5f,
                    bounds.contentHeight - bounds.border * 0.5f,
                    assets.letterPaint);
            canvas.save();
            canvas.rotate(-90f, 0, 0);
            canvas.drawText(yAxisTitle,
                    (bounds.contentHeight + assets.letterPaint.measureText(xAxisTitle)) * -0.25f,
                    assets.letterPaint.getTextSize(), assets.letterPaint);
            canvas.restore();
        }
        // draw the number of passed games in on top
        assets.letterPaint.setTextScaleX(1f);
        canvas.drawText(gameCount, letterLeft, bounds.drawingBottom, assets.letterPaint);

    }

    public void setSelectedChild(Game selectedGame) {
        this.selectedChild = selectedGame;
    }
}

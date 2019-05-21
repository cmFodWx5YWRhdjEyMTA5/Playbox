package uk.co.darkerwaters.staveinvaders.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;

public class CircleProgressView extends BaseView {

    private final static float K_START_ANGLE = 90f;

    private RectF mOval;
    private float progress = 0.33f;
    private String progressText = "60";

    public CircleProgressView(Context context) {
        super(context);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // make this always square on the smallest dimension
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void initialiseView(Context context) {
        // Initialize new data for this view

    }

    public void setProgress(float progress, String text) {
        this.progress = progress;
        this.progressText = text;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // get our assets for paints etc
        Assets assets = getAssets();
        ViewBounds bounds = new ViewBounds();
        // the arc is a fraction of the width of the oval...
        float scoreStroke = Math.min(bounds.contentWidth, bounds.contentHeight) * 0.1f;
        // set the oval to fill the view (square normally)
        mOval = new RectF(
                bounds.drawingLeft + (scoreStroke * 0.5f),
                bounds.drawingTop + (scoreStroke * 0.5f),
                bounds.drawingRight - (scoreStroke * 0.5f),
                bounds.drawingBottom - (scoreStroke * 0.5f));

        // draw in the background first
        float strokeWidth = assets.backgroundPaint.getStrokeWidth();
        assets.backgroundPaint.setStrokeWidth(scoreStroke);
        canvas.drawArc(mOval, 0, 360, false, assets.backgroundPaint);
        assets.backgroundPaint.setStrokeWidth(strokeWidth);

        // now the score over this
        strokeWidth = assets.highlightPaint.getStrokeWidth();
        assets.highlightPaint.setStrokeWidth(scoreStroke);
        canvas.drawArc(mOval, K_START_ANGLE,
                this.progress * 360f, false, assets.highlightPaint);
        assets.highlightPaint.setStrokeWidth(strokeWidth);

        // finally the text in the middle
        float textSize = assets.letterPaint.getTextSize();
        assets.letterPaint.setTextSize(mOval.height() / progressText.length());
        Rect textBounds = new Rect();
        assets.letterPaint.getTextBounds(progressText, 0, progressText.length(), textBounds);
        canvas.drawText(progressText,
                mOval.centerX(),
                mOval.centerY() + textBounds.height() * 0.5f,
                assets.letterPaint);
        assets.letterPaint.setTextSize(textSize);
    }
}

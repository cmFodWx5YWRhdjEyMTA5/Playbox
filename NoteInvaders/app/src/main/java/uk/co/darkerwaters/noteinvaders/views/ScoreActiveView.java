package uk.co.darkerwaters.noteinvaders.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import uk.co.darkerwaters.noteinvaders.R;
import uk.co.darkerwaters.noteinvaders.state.ActiveScore;

public class ScoreActiveView extends View {

    private Paint missedPaint, falsePaint, backPaint;
    private RectF mOval;
    private Paint mTextPaint;

    private ActiveScore score = new ActiveScore();

    public ScoreActiveView(Context context) {
        super(context);
        init(context);
    }

    public ScoreActiveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ScoreActiveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // make this always square on the smallest dimension
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

    private void init(final Context context) {
        // Initialize new paints to draw this view
        missedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        missedPaint.setColor(getResources().getColor(R.color.colorMiss));
        missedPaint.setStyle(Paint.Style.STROKE);
        missedPaint.setDither(true);                    // set the dither to true
        missedPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        missedPaint.setStrokeCap(Paint.Cap.BUTT);      // set the paint cap to round too
        missedPaint.setPathEffect(new PathEffect());   // set the path effect when they join.

        falsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        falsePaint.setColor(getResources().getColor(R.color.colorFalseFire));
        falsePaint.setStyle(Paint.Style.STROKE);
        falsePaint.setStrokeWidth(10);
        falsePaint.setDither(true);                    // set the dither to true
        falsePaint.setStrokeJoin(Paint.Join.BEVEL);    // set the join to round you want
        falsePaint.setStrokeCap(Paint.Cap.BUTT);      // set the paint cap to round too
        falsePaint.setPathEffect(new PathEffect());   // set the path effect when they join.


        backPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backPaint.setColor(getResources().getColor(android.R.color.background_light));
        backPaint.setStyle(Paint.Style.STROKE);
        backPaint.setStrokeWidth(10);
        backPaint.setDither(true);                    // set the dither to true
        backPaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        backPaint.setStrokeCap(Paint.Cap.BUTT);      // set the paint cap to round too
        backPaint.setPathEffect(new PathEffect());   // set the path effect when they join.

        measure(MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
        mOval = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(getResources().getColor(android.R.color.primary_text_light));
        //mTextPaint.setTypeface(Font.);
        mTextPaint.setTextSize(getHeight() * 0.5f);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mOval = new RectF(getPaddingLeft(), getPaddingTop(),
                getWidth() - (getPaddingLeft() + getPaddingRight()),
                getHeight() - (getPaddingTop() + getPaddingBottom()));
    }

    public void setScore(ActiveScore score){
        // set the score
        this.score = score;
        // and update the view
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF canvasRect = new RectF(getPaddingLeft(), getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingTop());

        // the arc is a fraction of the width of the oval...
        float scoreStroke = Math.min(canvasRect.width(), canvasRect.height()) * 0.2f;
        // set the oval to fill the view (square normally)
        mOval = new RectF(
                canvasRect.left + (scoreStroke * 0.5f),
                canvasRect.top + (scoreStroke * 0.5f),
                canvasRect.right - (scoreStroke * 0.5f),
                canvasRect.bottom - (scoreStroke * 0.5f));
                // set this on the paints
        missedPaint.setStrokeWidth(scoreStroke);
        falsePaint.setStrokeWidth(scoreStroke);
        backPaint.setStrokeWidth(scoreStroke);

        // draw in the background first
        canvas.drawArc(mOval, 0, 360, false, backPaint);
        // now the scores over this
        canvas.drawArc(mOval, 90f,
                this.score.getMisses() /
                        ((float)ActiveScore.K_PERMITTED_MISS_COUNT) * 180f,
                false, missedPaint);
        canvas.drawArc(mOval, 90f,
                this.score.getFalseShots() /
                        ((float)ActiveScore.K_PERMITTED_FALSE_SHOT_COUNT) * -180f,
                false, falsePaint);

        // finally the text in the middle
        mTextPaint.setTextSize(mOval.height() * 0.5f);
        canvas.drawText(Integer.toString(this.score.getHits()),
                mOval.left + (mOval.width() * 0.5f),
                mOval.top + (mOval.height() * 0.7f),
                mTextPaint);
    }
}

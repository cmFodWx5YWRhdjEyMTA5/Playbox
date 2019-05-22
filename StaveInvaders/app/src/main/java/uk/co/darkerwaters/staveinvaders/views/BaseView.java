package uk.co.darkerwaters.staveinvaders.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;

public abstract class BaseView extends View {

    protected final Application application;

    protected class Assets {
        protected final Paint backgroundPaint;
        protected final Paint outlinePaint;
        protected final Paint letterPaint;
        protected final Paint highlightPaint;
        protected final Paint objectPaint;

        protected final Paint whitePaint;
        protected final Paint blackPaint;

        private Assets() {
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
            this.letterPaint.setTextAlign(Paint.Align.CENTER);
            this.letterPaint.setTextSize(36f);

            this.objectPaint = new Paint();
            this.objectPaint.setAntiAlias(true);
            this.objectPaint.setStyle(Paint.Style.FILL);
            this.objectPaint.setColor(getResources().getColor(R.color.primaryColor));
            this.objectPaint.setShadowLayer(12, 6, 6, getResources().getColor(R.color.primaryDarkColor));
            setLayerType(LAYER_TYPE_SOFTWARE, this.objectPaint);

            this.whitePaint = new Paint();
            this.whitePaint.setStyle(Paint.Style.STROKE);
            this.whitePaint.setStrokeWidth(2);
            this.whitePaint.setColor(Color.BLACK);
            this.whitePaint.setAntiAlias(true);

            this.blackPaint = new Paint();
            this.blackPaint.setStyle(Paint.Style.FILL);
            this.blackPaint.setColor(Color.BLACK);
            this.blackPaint.setAntiAlias(true);
        }
    }

    class ViewBounds {
        final float paddingLeft;
        final float paddingTop;
        final float paddingRight;
        final float paddingBottom;

        final float contentWidth;
        final float contentHeight;

        final float border;

        final float drawingRight;
        final float drawingLeft;
        final float drawingTop;
        final float drawingBottom;

        ViewBounds() {

            paddingLeft = getPaddingLeft();
            paddingTop = getPaddingTop();
            paddingRight = getPaddingRight();
            paddingBottom = getPaddingBottom();

            contentWidth = getContentWidth();
            contentHeight = getContentHeight();

            border = contentHeight * 0.1f;

            drawingRight = contentWidth - (paddingLeft + border);
            drawingLeft = paddingLeft + border;
            drawingTop = paddingTop + border;
            drawingBottom = contentHeight - (paddingBottom + border);
        }

        int getContentWidth() {
            return getWidth() - getPaddingLeft() - getPaddingRight();
        }

        private int getContentHeight() {
            return getHeight() - getPaddingTop() - getPaddingBottom();
        }
    }

    private static Assets ASSETS = null;

    public BaseView(Context context) {
        super(context);
        // get our application here
        this.application = (Application) ((Activity)context).getApplication();
        // initialise the view
        initialiseView(context);
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // get our application here
        this.application = (Application) ((Activity)context).getApplication();
        // initialise the view
        initialiseView(context);
    }

    public BaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // get our application here
        this.application = (Application) ((Activity)context).getApplication();
        // initialise the view
        initialiseView(context);
    }

    public BaseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // get our application here
        this.application = (Application) ((Activity)context).getApplication();
        // initialise the view
        initialiseView(context);
    }

    protected abstract void initialiseView(Context context);

    protected Assets getAssets() {
        synchronized (this) {
            if (ASSETS == null) {
                ASSETS = new Assets();
            }
        }
        return ASSETS;
    }
}

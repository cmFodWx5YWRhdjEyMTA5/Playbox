package uk.co.darkerwaters.scorepal.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import uk.co.darkerwaters.scorepal.R;

public class ServerView extends View {

    private boolean isShowText;
    private int labelPosition;

    public ServerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get the style attributes (defined in attrs.xml and set in the layout.xml)
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ServerView,
                0, 0);
        // get the data, ensuring that we recycle the typed array of attributes
        try {
            this.isShowText = a.getBoolean(R.styleable.ServerView_showText, false);
            this.labelPosition = a.getInteger(R.styleable.ServerView_labelPosition, 0);
        } finally {
            // be sure to recycle this to free up the memory
            a.recycle();
        }
    }

    public boolean isShowText() {
        return this.isShowText;
    }

    public void setShowText(boolean showText) {
        this.isShowText = showText;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // clear the canvas
        canvas.drawColor(Color.WHITE);
        // and draw who is serving
        int width = canvas.getWidth();
        Paint circlePaint = new Paint(Color.BLACK);
        canvas.drawCircle(width * 0.5f, width * 0.5f, width * 0.4f, circlePaint);
    }
}

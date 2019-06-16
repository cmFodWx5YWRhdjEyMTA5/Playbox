package uk.co.darkerwaters.scorepal.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import uk.co.darkerwaters.scorepal.R;

public class EndView extends View {

    private boolean isShowText;
    private int labelPosition;

    public EndView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get the style attributes (defined in attrs.xml and set in the layout.xml)
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.EndView,
                0, 0);
        // get the data, ensuring that we recycle the typed array of attributes
        try {
            this.isShowText = a.getBoolean(R.styleable.EndView_showText, false);
            this.labelPosition = a.getInteger(R.styleable.EndView_labelPosition, 0);
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
        Paint circlePaint = new Paint(Color.BLACK);
        canvas.drawCircle(canvas.getWidth() * 0.9f, canvas.getHeight() * 0.5f, canvas.getHeight() * 0.4f, circlePaint);
    }
}

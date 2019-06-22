package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class ResizeTextView extends android.support.v7.widget.AppCompatTextView {

    private final float originalTextSize;
    private final String referenceText;

    public ResizeTextView(Context context, String referenceText) {
        this(context, referenceText, null);
    }

    public ResizeTextView(Context context, String referenceText, AttributeSet attrs) {
        super(context, attrs);
        this.referenceText = referenceText;
        this.originalTextSize = getTextSize();
    }

    private void refitText(String text, int textWidth, int textHeight) {
        if (textWidth > 0) {
            float availableWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
            if (availableWidth <= 0)
                return;
            // setup the original size to measure our string
            TextPaint tp = getPaint();
            setTextSize(TypedValue.COMPLEX_UNIT_PX, this.originalTextSize);
            // and get the bounds of the text
            float ratio = availableWidth / tp.measureText(this.referenceText);
            if (ratio > 0f) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(this.originalTextSize, this.originalTextSize * ratio * 0.9f));
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        refitText(this.getText().toString(), parentWidth, parentHeight);
        this.setMeasuredDimension(parentWidth, parentHeight);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start,
                                 final int before, final int after) {
        refitText(text.toString(), this.getWidth(), this.getHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w, h);
        }
    }
}
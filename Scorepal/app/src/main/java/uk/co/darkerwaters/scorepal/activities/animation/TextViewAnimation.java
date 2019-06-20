package uk.co.darkerwaters.scorepal.activities.animation;

import android.app.Activity;
import android.widget.TextView;

public abstract class TextViewAnimation {

    protected final Activity context;
    protected final TextView view;
    protected int repetitions;
    protected boolean isCancel;
    protected String animatedText;

    public TextViewAnimation(Activity context, TextView view, int repetitions) {
        // set the content of the text view
        this.context = context;
        this.view = view;
        this.repetitions = repetitions;
        this.animatedText = null;
    }

    protected void setAnimatedText(String message) {
        this.animatedText = message;
        if (null != this.view && null != this.context) {
            // set the text on the view
            this.view.setText(this.animatedText);
        }
    }

    public String getAnimatedText() {
        return this.animatedText;
    }

    protected abstract void animateTextIn();
    protected abstract void animateTextOut();

    public void cancel() {
        this.isCancel = true;
    };


}

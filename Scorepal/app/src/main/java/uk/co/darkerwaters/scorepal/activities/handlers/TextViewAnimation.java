package uk.co.darkerwaters.scorepal.activities.handlers;

import android.app.Activity;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;

public abstract class TextViewAnimation {

    protected final Activity context;
    protected final TextView view;
    protected int repetitions;
    protected boolean isCancel;

    public TextViewAnimation(Activity context, TextView view, int repetitions) {
        // set the content of the text view
        this.context = context;
        this.view = view;
        this.repetitions = repetitions;
        this.view.setText(context.getString(R.string.change_ends));
    }

    protected abstract void animateTextIn();
    protected abstract void animateTextOut();

    public void cancel() {
        this.isCancel = true;
    };


}

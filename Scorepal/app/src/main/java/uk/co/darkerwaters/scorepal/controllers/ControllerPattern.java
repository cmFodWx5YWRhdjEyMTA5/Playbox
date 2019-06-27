package uk.co.darkerwaters.scorepal.controllers;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;

public enum ControllerPattern {
    SingleClick(0, R.string.pattern_singleClick),
    DoubleClick(1, R.string.pattern_doubleClick),
    LongClick(2, R.string.pattern_longClick),
    TripleClick(3, R.string.pattern_tripleClick);

    public static final ControllerPattern K_DEFAULT = DoubleClick;

    final int stringResId;
    final int val;

    ControllerPattern(int val, int stringResId) {
        this.val = val;
        this.stringResId = stringResId;
    }

    public String toString(Context context) {
        return context.getString(this.stringResId);
    }

    public static ControllerPattern fromVal(int val) {
        for (ControllerPattern button : values()) {
            if (button.val == val) {
                return button;
            }
        }
        return K_DEFAULT;
    }

    public int getVal() {
        return this.val;
    }
}

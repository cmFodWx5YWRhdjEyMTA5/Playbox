package uk.co.darkerwaters.scorepal.controllers;

import android.content.Context;

import uk.co.darkerwaters.scorepal.R;

public enum ControllerAction {
    PointServer(0, R.string.action_ptServer),
    PointReceiver(1, R.string.action_ptReceiver),
    PointTeamOne(2, R.string.action_ptTeamOne),
    PointTeamTwo(3, R.string.action_ptTeamTwo),
    UndoLastPoint(4, R.string.action_ptUndo);

    public static final ControllerAction K_DEFAULT = PointServer;

    final int stringResId;
    final int val;

    ControllerAction(int val, int stringResId) {
        this.val = val;
        this.stringResId = stringResId;
    }

    public String toString(Context context) {
        return context.getString(this.stringResId);
    }

    public static ControllerAction fromVal(int val) {
        for (ControllerAction button : values()) {
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

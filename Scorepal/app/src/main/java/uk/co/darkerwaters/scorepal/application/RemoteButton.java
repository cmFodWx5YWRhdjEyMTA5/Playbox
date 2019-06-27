package uk.co.darkerwaters.scorepal.application;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;

public class RemoteButton {
    private final int keyCode;
    private final List<Action> actions;

    public class Action {
        private RemoteButtonAction action;
        private RemoteButtonPattern pattern;

        public Action(RemoteButtonAction action, RemoteButtonPattern pattern) {
            this.action = action;
            this.pattern = pattern;
        }

        public RemoteButtonAction getAction() {
            return this.action;
        }

        public RemoteButtonPattern getPattern() {
            return this.pattern;
        }

        public void setAction(RemoteButtonAction action) {
            this.action = action;
        }

        public void getPattern(RemoteButtonPattern pattern) {
            this.pattern = pattern;
        }
    }

    public enum RemoteButtonAction {
        PointServer(0, R.string.action_ptServer),
        PointReceiver(1, R.string.action_ptReceiver),
        PointTeamOne(2, R.string.action_ptTeamOne),
        PointTeamTwo(3, R.string.action_ptTeamTwo),
        UndoLastPoint(4, R.string.action_ptUndo);

        public static final RemoteButtonAction K_DEFAULT = PointServer;

        final int stringResId;
        final int val;

        RemoteButtonAction(int val, int stringResId) {
            this.val = val;
            this.stringResId = stringResId;
        }

        public String toString(Context context) {
            return context.getString(this.stringResId);
        }

        public static RemoteButtonAction fromVal(int val) {
            for (RemoteButtonAction button : values()) {
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

    public enum RemoteButtonPattern {
        SingleClick(0, R.string.pattern_singleClick),
        DoubleClick(1, R.string.pattern_doubleClick),
        LongClick(2, R.string.pattern_longClick),
        TripleClick(3, R.string.pattern_tripleClick);

        public static final RemoteButtonPattern K_DEFAULT = DoubleClick;

        final int stringResId;
        final int val;

        RemoteButtonPattern(int val, int stringResId) {
            this.val = val;
            this.stringResId = stringResId;
        }

        public String toString(Context context) {
            return context.getString(this.stringResId);
        }

        public static RemoteButtonPattern fromVal(int val) {
            for (RemoteButtonPattern button : values()) {
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

    public RemoteButton(int keyCode) {
        this.keyCode = keyCode;
        this.actions = new ArrayList<Action>();
    }

    public void addAction(RemoteButtonAction action, RemoteButtonPattern pattern) {
        this.actions.add(new Action(action, pattern));
    }

    public static SpinnerAdapter getPatternAdapter(Context context) {
        List<String> stringList = new ArrayList<String>();
        for (RemoteButtonPattern button : RemoteButtonPattern.values()) {
            stringList.add(button.toString(context));
        }
        // create the adapter from this
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, stringList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        return adapter;
    }

    public static SpinnerAdapter getActionAdapter(Context context) {
        List<String> stringList = new ArrayList<String>();
        for (RemoteButtonAction button : RemoteButtonAction.values()) {
            stringList.add(button.toString(context));
        }
        // create the adapter from this
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, stringList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        return adapter;
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public Action[] getActions() {
        return this.actions.toArray(new Action[0]);
    }

    public boolean removeAction(int i) {
        boolean isRemoved = false;
        if (this.actions.size() > i) {
            this.actions.remove(i);
            isRemoved = true;
        }
        return isRemoved;
    }
}

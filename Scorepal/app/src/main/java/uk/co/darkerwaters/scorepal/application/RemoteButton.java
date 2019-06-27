package uk.co.darkerwaters.scorepal.application;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.controllers.ControllerAction;
import uk.co.darkerwaters.scorepal.controllers.ControllerPattern;

public class RemoteButton {
    private final int keyCode;
    private final List<Action> actions;

    public class Action {
        private ControllerAction action;
        private ControllerPattern pattern;

        public Action(ControllerAction action, ControllerPattern pattern) {
            this.action = action;
            this.pattern = pattern;
        }

        public ControllerAction getAction() {
            return this.action;
        }

        public ControllerPattern getPattern() {
            return this.pattern;
        }

        public void setAction(ControllerAction action) {
            this.action = action;
        }

        public void getPattern(ControllerPattern pattern) {
            this.pattern = pattern;
        }
    }



    public RemoteButton(int keyCode) {
        this.keyCode = keyCode;
        this.actions = new ArrayList<Action>();
    }

    public void addAction(ControllerAction action, ControllerPattern pattern) {
        this.actions.add(new Action(action, pattern));
    }

    public static SpinnerAdapter getPatternAdapter(Context context) {
        List<String> stringList = new ArrayList<String>();
        for (ControllerPattern button : ControllerPattern.values()) {
            stringList.add(button.toString(context));
        }
        // create the adapter from this
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, stringList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        return adapter;
    }

    public static SpinnerAdapter getActionAdapter(Context context) {
        List<String> stringList = new ArrayList<String>();
        for (ControllerAction button : ControllerAction.values()) {
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

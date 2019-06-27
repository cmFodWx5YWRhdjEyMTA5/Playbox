package uk.co.darkerwaters.scorepal.controllers;

import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderRemoteButton;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.application.RemoteButton;

public class KeyController extends Controller {

    private final View.OnKeyListener keyListener;
    private final RemoteButton[] buttons;

    public KeyController(ViewGroup mainLayout, final RemoteButton[] buttons) {
        this.buttons = Arrays.copyOf(buttons, buttons.length);
        // create the key listener to listen for all the inputs we can find
        this.keyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                // find the button for this
                boolean isProcessed = false;
                for (RemoteButton button : buttons) {
                    if (button.getKeyCode() == keyCode) {
                        // this is the one
                        isProcessed = processButtonPress(button, keyEvent);
                        break;
                    }
                }
                return isProcessed;
            }
        };

        // we are using the BT button, set the key listener accordingly
        KeyController.setKeyListener(mainLayout, this.keyListener);
    }

    private boolean processButtonPress(RemoteButton button, KeyEvent keyEvent) {
        // process each of the actions
        for (RemoteButton.Action action : button.getActions()) {
            if (isPatternPerformed(action, keyEvent)) {
                informControllerListeners(action.getAction());
            }
        }
        // we used this
        return true;
    }

    private boolean isPatternPerformed(RemoteButton.Action action, KeyEvent keyEvent) {
        //TODO process this button to see if we have clicked it right
        return true;
    }

    static public void setKeyListener(ViewGroup layout, View.OnKeyListener keyListener) {
        for (int i = 0; i < layout.getChildCount(); ++i) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                setKeyListener((ViewGroup)child, keyListener);
            }
            else {
                child.setOnKeyListener(keyListener);
            }
        }
    }
}

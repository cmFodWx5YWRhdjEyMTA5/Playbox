package uk.co.darkerwaters.scorepal.activities.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BaseActivity;

public abstract class ExpandingFragment extends Fragment {

    private static final long K_ANIMATION_DURATION = 1000;

    private View[] buttons;
    private View masterButton;
    private View mainView;

    private boolean isButtonsShown = true;

    private static final List<ExpandingFragment> ActiveFragments = new ArrayList<ExpandingFragment>();

    public ExpandingFragment() {
        // required constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // register this with the static list
        synchronized (ActiveFragments) {
            ActiveFragments.add(this);
        }
    }

    @Override
    public void onDetach() {
        // un-register
        synchronized (ActiveFragments) {
            ActiveFragments.remove(this);
        }
        super.onDetach();
    }

    protected void setupExpandingFragment(View mainView, View master, View[] buttons) {
        // remember all this
        this.mainView = mainView;
        this.masterButton = master;
        this.buttons = buttons;
        // start invisible
        this.mainView.setVisibility(View.INVISIBLE);

        this.masterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHideButtons(false);
            }
        });
    }

    protected void updateViewFromData() {
        int color = getContext().getColor(R.color.primaryTextColor);
        for (View button : this.buttons) {
            BaseActivity.SetIconTint(button, color);
        }
    }

    public void setVisibility(int visibility) {
        if (null != this.mainView) {
            this.mainView.setVisibility(visibility);
        }
    }

    public void hideButtons(boolean isInstant) {
        if (this.isButtonsShown) {
            showHideButtons(isInstant);
        }
    }

    private void showHideButtons(boolean isInstant) {
        this.isButtonsShown = !this.isButtonsShown;

        if (!this.isButtonsShown) {
            // and the size we need to be
            float hideWidth = this.masterButton.getWidth();
            float hideHeight = this.masterButton.getHeight();
            // get where we need to go to to hide our button
            float hideXPosition = this.masterButton.getX() - hideWidth;
            float hideYPosition = this.masterButton.getY();

            // animate each button down to this size and position
            for (View button : this.buttons) {
                hide(button, hideXPosition, hideYPosition, hideWidth, hideHeight, isInstant);
            }
        }
        else {
            // update the view from the data, this will update the icons and their colours
            updateViewFromData();
            // and restore them to their original locations
            for (View button : this.buttons) {
                restore(button);
            }
            // all other fragments of this type should hide their buttons
            synchronized (ActiveFragments) {
                for (ExpandingFragment fragment : ActiveFragments) {
                    if (fragment != this) {
                        fragment.hideButtons(false);
                    }
                }
            }
        }

    }

    private void hide(View button, float targetX, float targetY, float targetW, float targetH, boolean isInstant) {
        // calculate the movements
        float movementX = targetX - button.getX();
        float movementY = targetY - button.getY();
        float scaleX = 0f;//targetW / button.getWidth();
        float scaleY = 0f;//targetH / button.getHeight();
        // and animate to here
        button.animate()
                .translationX(movementX)
                .translationY(movementY)
                .scaleX(scaleX)
                .scaleY(scaleY)
                .setDuration(isInstant ? 0 : K_ANIMATION_DURATION)
                .start();
    }

    private void restore(final View button) {
        // animate back
        button.animate()
                .translationX(0f)
                .translationY(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(K_ANIMATION_DURATION)
                .start();
    }
}

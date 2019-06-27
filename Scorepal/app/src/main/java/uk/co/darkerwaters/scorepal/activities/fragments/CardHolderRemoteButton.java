package uk.co.darkerwaters.scorepal.activities.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.RemoteButtonActionRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.RemoteButton;

public class CardHolderRemoteButton extends RecyclerView.ViewHolder {

    private final View parent;

    private final ImageView remoteButtonImage;
    private final Button addActionButton;
    private final TextView buttonCodeText;
    private final RecyclerView remoteButtonRecyclerView;

    private final Context context;
    private RemoteButtonActionRecyclerAdapter recyclerViewAdapter;

    public CardHolderRemoteButton(@NonNull View itemView, Context context) {
        super(itemView);
        this.parent = itemView;
        this.context = context;

        // card is created, find all our children views and stuff here
        this.remoteButtonImage = this.parent.findViewById(R.id.remoteButtonImage);
        this.addActionButton = this.parent.findViewById(R.id.addActionButton);
        this.buttonCodeText = this.parent.findViewById(R.id.buttonCodeText);
        this.remoteButtonRecyclerView = this.parent.findViewById(R.id.remoteButtonRecyclerView);
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

    public void initialiseCard(final RemoteButton button, View.OnKeyListener keyListener) {
        this.buttonCodeText.setText(Integer.toString(button.getKeyCode()));

        this.recyclerViewAdapter = new RemoteButtonActionRecyclerAdapter(this, button, keyListener);
        this.remoteButtonRecyclerView.setAdapter(this.recyclerViewAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 1);
        this.remoteButtonRecyclerView.setLayoutManager(layoutManager);

        this.addActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addActionToButton(button);
            }
        });

        // also handle the changes here, set this on the application settings, or wherever
        if (parent instanceof ViewGroup) {
            // need to set the key listener on all the views on this activity to intercept everything
            setKeyListener((ViewGroup)parent, keyListener);
        }
    }

    private void addActionToButton(RemoteButton button) {
        // add the action
        button.addAction(RemoteButton.RemoteButtonAction.K_DEFAULT, RemoteButton.RemoteButtonPattern.K_DEFAULT);
        int newIndex = button.getActions().length - 1;
        // and update the adapter view of this
        this.recyclerViewAdapter.notifyItemInserted(newIndex);
        this.remoteButtonRecyclerView.smoothScrollToPosition(newIndex);
    }

    public void deleteAction(RemoteButton button, RemoteButton.Action action) {
        RemoteButton.Action[] actions = button.getActions();
        for (int i = 0; i < actions.length; ++i) {
            if (actions[i] == action) {
                // this is the one
                if (button.removeAction(i)) {
                    // and update the view
                    this.recyclerViewAdapter.notifyItemRemoved(i);
                }
                break;
            }
        }
    }

    public Context getContext() {
        return this.context;
    }

    public void highlightButton() {
        if (null != this.remoteButtonImage) {
            final ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(
                    this.remoteButtonImage,
                    PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.2f));
            scaleUp.setDuration(310);
            final ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                    this.remoteButtonImage,
                    PropertyValuesHolder.ofFloat("scaleX", 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f));
            scaleDown.setDuration(310);
            // when scaled up, scale back down
            scaleUp.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    scaleDown.start();
                }
            });
            scaleUp.start();
        }
    }
}

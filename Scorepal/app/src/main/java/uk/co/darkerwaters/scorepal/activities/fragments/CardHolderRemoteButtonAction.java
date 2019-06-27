package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.RemoteButton;

public class CardHolderRemoteButtonAction extends RecyclerView.ViewHolder {

    private final View parent;

    private final Spinner buttonPatternSpinner;
    private final Spinner buttonActionSpinner;

    private final ImageButton deleteActionButton;

    private final Context context;

    public CardHolderRemoteButtonAction(@NonNull View itemView, Context context) {
        super(itemView);
        this.parent = itemView;
        this.context = context;

        // card is created, find all our children views and stuff here
        this.buttonPatternSpinner = this.parent.findViewById(R.id.buttonPatternSpinner);
        this.buttonActionSpinner = this.parent.findViewById(R.id.buttonActionSpinner);
        this.deleteActionButton = this.parent.findViewById(R.id.deleteActionButton);

        // set the adapters on these spinners
        this.buttonPatternSpinner.setAdapter(RemoteButton.getPatternAdapter(context));
        this.buttonActionSpinner.setAdapter(RemoteButton.getActionAdapter(context));

        this.buttonPatternSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int i, long l) {
                // set the correct properties as we want them
                setAdapterTextProperties(parentView);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        this.buttonActionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View view, int i, long l) {
                // set the correct properties as we want them
                setAdapterTextProperties(parentView);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    private void setAdapterTextProperties(AdapterView<?> parentView) {
        int textColor = context.getColor(R.color.primaryTextColor);
        TextView view = ((TextView) parentView.getChildAt(0));
        view.setTextColor(textColor);
    }

    public void initialiseCard(final CardHolderRemoteButton holder,
                               final RemoteButton button,
                               final RemoteButton.Action action,
                               View.OnKeyListener keyListener) {
        this.buttonPatternSpinner.setSelection(action.getPattern().getVal());
        this.buttonActionSpinner.setSelection(action.getAction().getVal());

        this.deleteActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.deleteAction(button, action);
            }
        });

        // also handle the changes here, set this on the application settings, or wherever
        if (parent instanceof ViewGroup) {
            // need to set the key listener on all the views on this activity to intercept everything
            setKeyListener((ViewGroup)parent, keyListener);
        }
    }
}

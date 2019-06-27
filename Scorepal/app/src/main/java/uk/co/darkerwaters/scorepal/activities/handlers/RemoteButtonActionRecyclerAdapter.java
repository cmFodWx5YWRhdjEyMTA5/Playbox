package uk.co.darkerwaters.scorepal.activities.handlers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderRemoteButton;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderRemoteButtonAction;
import uk.co.darkerwaters.scorepal.application.RemoteButton;
import uk.co.darkerwaters.scorepal.controllers.ControllerAction;
import uk.co.darkerwaters.scorepal.controllers.ControllerPattern;

public class RemoteButtonActionRecyclerAdapter extends RecyclerView.Adapter<CardHolderRemoteButtonAction> {

    // create the cards here
    private final RemoteButton button;
    private final CardHolderRemoteButton context;
    private final View.OnKeyListener keyListener;

    public RemoteButtonActionRecyclerAdapter(CardHolderRemoteButton context, RemoteButton button, View.OnKeyListener keyListener) {
        // create the list of cards to show here
        this.context = context;
        this.keyListener = keyListener;
        this.button = button;
    }

    @Override
    public CardHolderRemoteButtonAction onCreateViewHolder(ViewGroup viewGroup, int i) {
        // GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_remote_button_action, viewGroup, false);
        // create the holder and return it
        return new CardHolderRemoteButtonAction(v, context.getContext());
    }

    @Override
    public void onBindViewHolder(CardHolderRemoteButtonAction viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(context, this.button, this.button.getActions()[i], this.keyListener);
    }

    @Override
    public int getItemCount() {
        return this.button.getActions().length;
    }

    public ControllerAction getNextAction() {
        List<ControllerAction> remainingActions = new ArrayList<ControllerAction>();
        Collections.addAll(remainingActions, ControllerAction.values());
        RemoteButton.Action[] actions = this.button.getActions();
        // remove those used from the list
        for (RemoteButton.Action action  : actions) {
            remainingActions.remove(action.getAction());
        }
        // and use the top one left
        if (remainingActions.isEmpty()) {
            return ControllerAction.K_DEFAULT;
        }
        else {
            return remainingActions.get(0);
        }
    }

    public ControllerPattern getNextPattern() {
        List<ControllerPattern> remainingPatterns = new ArrayList<ControllerPattern>();
        Collections.addAll(remainingPatterns, ControllerPattern.values());
        RemoteButton.Action[] actions = this.button.getActions();
        // remove those used from the list
        for (RemoteButton.Action action  : actions) {
            remainingPatterns.remove(action.getPattern());
        }
        // and use the top one left
        if (remainingPatterns.isEmpty()) {
            return ControllerPattern.K_DEFAULT;
        }
        else {
            return remainingPatterns.get(0);
        }
    }
}

package uk.co.darkerwaters.scorepal.activities.handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderRemoteButton;
import uk.co.darkerwaters.scorepal.application.RemoteButton;

public class RemoteButtonRecyclerAdapter extends RecyclerView.Adapter<CardHolderRemoteButton> {

    // create the cards here
    private final List<RemoteButton> buttons;
    private final Application application;
    private final Context context;
    private final View.OnKeyListener keyListener;

    public RemoteButtonRecyclerAdapter(Application application, Context context, View.OnKeyListener keyListener) {
        // create the list of cards to show here
        this.application = application;
        this.context = context;
        this.keyListener = keyListener;
        this.buttons = new ArrayList<RemoteButton>();
    }

    @Override
    public CardHolderRemoteButton onCreateViewHolder(ViewGroup viewGroup, int i) {
        // GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_remote_button, viewGroup, false);
        // create the holder and return it
        return new CardHolderRemoteButton(v, context);
    }

    @Override
    public void onBindViewHolder(CardHolderRemoteButton viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.application, this.buttons.get(i), this.keyListener);
    }
    
    public boolean addButton(RemoteButton button) {
        boolean isAdded = false;
        if (null == getButton(button.getKeyCode())) {
            // not in the list yet
            if (this.buttons.add(button)) {
                int addedIndex = this.buttons.size() - 1;
                this.notifyItemChanged(addedIndex);
                isAdded = true;
            }
        }
        return isAdded;
    }

    @Override
    public int getItemCount() {
        return this.buttons.size();
    }

    public RemoteButton getButton(int keyCode) {
        for (RemoteButton existing : this.buttons) {
            if (existing.getKeyCode() == keyCode) {
                return existing;
            }
        }
        return null;
    }

    public int getButtonPosition(RemoteButton button) {
        int index = 0;
        for (RemoteButton existing : this.buttons) {
            if (existing.getKeyCode() == button.getKeyCode()) {
                return index;
            }
            ++index;
        }
        return -1;
    }
}

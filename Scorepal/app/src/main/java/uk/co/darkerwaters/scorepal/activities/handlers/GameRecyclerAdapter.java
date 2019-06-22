package uk.co.darkerwaters.scorepal.activities.handlers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderGame;

public class GameRecyclerAdapter extends RecyclerView.Adapter<CardHolderGame> {

    // create the cards here
    private final File[] matches;
    private final Application application;

    public GameRecyclerAdapter(Application application, File[] matches) {
        // create the list of cards to show here
        this.application = application;
        this.matches = matches;
    }

    @Override
    public CardHolderGame onCreateViewHolder(ViewGroup viewGroup, int i) {
        //GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_game, viewGroup, false);
        // create the holder and return it
        return new CardHolderGame(v);
    }

    @Override
    public void onBindViewHolder(CardHolderGame viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.application, this.matches[i]);
    }

    @Override
    public int getItemCount() {
        return this.matches.length;
    }
}

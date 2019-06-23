package uk.co.darkerwaters.scorepal.activities.handlers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderMatch;

public class GameRecyclerAdapter extends RecyclerView.Adapter<CardHolderMatch> {

    // create the cards here
    private File[] matches;
    private final Application application;

    public GameRecyclerAdapter(Application application) {
        this(application, new File[0]);
    }

    public GameRecyclerAdapter(Application application, File[] matches) {
        // create the list of cards to show here
        this.application = application;
        this.matches = matches;
    }

    public void setMatches(File[] matches) {
        this.matches = matches;
        this.notifyDataSetChanged();
    }

    @Override
    public CardHolderMatch onCreateViewHolder(ViewGroup viewGroup, int i) {
        //GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_match, viewGroup, false);
        // create the holder and return it
        return new CardHolderMatch(v);
    }

    @Override
    public void onBindViewHolder(CardHolderMatch viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.application, this.matches[i]);
    }

    @Override
    public int getItemCount() {
        return this.matches.length;
    }
}

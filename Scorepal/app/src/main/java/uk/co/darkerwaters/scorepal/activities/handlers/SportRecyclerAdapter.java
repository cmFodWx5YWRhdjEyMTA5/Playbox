package uk.co.darkerwaters.scorepal.activities.handlers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderSport;
import uk.co.darkerwaters.scorepal.score.Sport;

public class SportRecyclerAdapter extends RecyclerView.Adapter<CardHolderSport> {

    // create the cards here
    private final Sport[] cards;
    private final Application application;
    private final Context context;

    public SportRecyclerAdapter(Application application, Context context) {
        // create the list of cards to show here
        this.application = application;
        this.context = context;
        this.cards = Sport.values();
    }

    @Override
    public CardHolderSport onCreateViewHolder(ViewGroup viewGroup, int i) {
        // GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_sport, viewGroup, false);
        // create the holder and return it
        return new CardHolderSport(v, context);
    }

    @Override
    public void onBindViewHolder(CardHolderSport viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.application, this.cards[i]);
    }

    @Override
    public int getItemCount() {
        return this.cards.length;
    }
}

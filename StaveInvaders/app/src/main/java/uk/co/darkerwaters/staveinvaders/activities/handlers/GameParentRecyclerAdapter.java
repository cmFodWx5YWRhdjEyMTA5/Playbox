package uk.co.darkerwaters.staveinvaders.activities.handlers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.activities.fragments.GameParentCardHolder;

public class GameParentRecyclerAdapter extends RecyclerView.Adapter<GameParentCardHolder> {

    // create the cards here
    private final Game[] cards;
    private final Application application;

    public GameParentRecyclerAdapter(Application application, Game[] cards) {
        // create the list of cards to show here
        this.application = application;
        this.cards = cards;
    }

    @Override
    public GameParentCardHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.game_parent_card, viewGroup, false);
        // create the holder and return it
        return new GameParentCardHolder(v);
    }

    @Override
    public void onBindViewHolder(GameParentCardHolder viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.application, this.cards[i]);
    }

    @Override
    public int getItemCount() {
        return this.cards.length;
    }
}

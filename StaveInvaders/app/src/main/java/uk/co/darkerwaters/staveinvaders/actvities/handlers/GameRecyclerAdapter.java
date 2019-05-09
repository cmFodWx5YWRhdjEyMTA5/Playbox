package uk.co.darkerwaters.staveinvaders.actvities.handlers;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameCardHolder;

public class GameRecyclerAdapter extends RecyclerView.Adapter<GameCardHolder> {

    // create the cards here
    private final Game[] cards;

    public GameRecyclerAdapter(Game[] cards) {
        // create the list of cards to show here
        this.cards = cards;
    }

    @Override
    public GameCardHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.game_card, viewGroup, false);
        // create the holder and return it
        GameCardHolder holder = new GameCardHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(GameCardHolder viewHolder, int i) {
        // initialise the card holder here
        viewHolder.initialiseCard(this.cards[i]);
    }

    @Override
    public int getItemCount() {
        return this.cards.length;
    }
}

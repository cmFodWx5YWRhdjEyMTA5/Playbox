package uk.co.darkerwaters.staveinvaders.actvities.handlers;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.darkerwaters.staveinvaders.actvities.cards.NoteGameCard;

public class MainRecyclerAdapter extends RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final Card card;
        public ViewHolder(@NonNull View itemView, Card card) {
            super(itemView);
            this.card = card;
        }
    }

    // create the cards here
    private final Card[] cards;
    private int childCreationCounter = 0;

    public static abstract class Card {
        protected View parentView = null;

        ViewHolder createViewHolder(View v) {
            return new ViewHolder(v, this);
        }

        public void onCardCreated(View v) {
            // nothing to do here
            this.parentView = v;
        }

        public abstract int getLayoutId();

        public abstract void initialiseCard(ViewHolder viewHolder);
    }

    public MainRecyclerAdapter(Card[] cards) {
        // create the list of cards to show here
        this.cards = cards;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        Card activeCard = this.cards[childCreationCounter++];
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(activeCard.getLayoutId(), viewGroup, false);
        activeCard.onCardCreated(v);
        return activeCard.createViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Card activeCard = this.cards[i];
        activeCard.initialiseCard(viewHolder);
    }

    @Override
    public int getItemCount() {
        return this.cards.length;
    }
}

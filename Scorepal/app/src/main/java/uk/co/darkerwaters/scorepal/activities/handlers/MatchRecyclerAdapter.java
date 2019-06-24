package uk.co.darkerwaters.scorepal.activities.handlers;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderMatch;
import uk.co.darkerwaters.scorepal.score.MatchPersistanceManager;

public class MatchRecyclerAdapter extends RecyclerView.Adapter<CardHolderMatch> {

    // create the cards here
    private SortedList<File> matches;
    private final Application application;

    public MatchRecyclerAdapter(Application application) {
        this(application, new File[0]);
    }

    public MatchRecyclerAdapter(Application application, File[] matches) {
        // create the list of cards to show here
        this.application = application;
        this.matches = new SortedList<File>(File.class, new SortedListAdapterCallback<File>(this) {
            @Override
            public boolean areContentsTheSame(File oldItem, File newItem) {
                return oldItem.getName().equals(newItem.getName());
            }
            @Override
            public boolean areItemsTheSame(File item1, File item2) {
                return item1 == item2;
            }

            @Override
            public int compare(File o1, File o2) {
                // sort in reverse filename order to put the latest at the top
                return -(o1.getName().compareTo(o2.getName()));
            }
        });
        // add all the items to the list, will sort them all too
        this.matches.beginBatchedUpdates();
        for (File matchFile : matches) {
            this.matches.add(matchFile);
        }
        this.matches.endBatchedUpdates();
    }

    public void updateMatches(File[] matches) {
        List<File> newMatches = Arrays.asList(matches);
        for (File matchFile : newMatches) {
            if (-1 == this.matches.indexOf(matchFile)) {
                // this is a new one, add this to the list
                int index = this.matches.add(matchFile);
                if (index >= 0) {
                    // need to inform the adapter of this new item
                    notifyItemInserted(index);
                }
            }
        }
        File[] existingMatches = new File[this.matches.size()];
        // gather the existing files into another array to look through
        for (int i = 0; i < existingMatches.length; ++i) {
            existingMatches[i] = this.matches.get(i);
        }
        for (File existingMatch : existingMatches) {
            // if not in the matches passed in, we need to delete
            if (false == newMatches.contains(existingMatch)) {
                // this shouldn't be there
                int index = this.matches.indexOf(existingMatch);
                if (index >= 0) {
                    // this is int the list of matches (as expected) - remove it
                    this.matches.removeItemAt(index);
                    notifyItemRemoved(index);
                }
            }
        }
    }

    public void removeAt(int position) {
        this.matches.removeItemAt(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.matches.size());
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
        viewHolder.initialiseCard(this.application, this.matches.get(i), this);
    }

    @Override
    public int getItemCount() {
        return this.matches.size();
    }
}

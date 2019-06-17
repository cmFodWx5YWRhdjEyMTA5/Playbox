package uk.co.darkerwaters.scorepal.activities.handlers;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.PointsSetupActivity;
import uk.co.darkerwaters.scorepal.activities.TennisSetupActivity;
import uk.co.darkerwaters.scorepal.activities.fragments.CardHolderSport;
import uk.co.darkerwaters.scorepal.score.ScoreFactory;

public class SportRecyclerAdapter extends RecyclerView.Adapter<CardHolderSport> {

    // store the data here
    public class Sport {
        public final String title;
        public final String subtitle;
        public final String imageFilename;
        public final ScoreFactory.ScoreMode mode;
        public final Class<? extends Activity> activityClass;

        Sport(String title, String subtitle, String imageFilename, ScoreFactory.ScoreMode mode, Class<? extends Activity> activityClass) {
            this.title = title;
            this.subtitle = subtitle;
            this.mode = mode;
            this.imageFilename = imageFilename;
            this.activityClass = activityClass;
        }
    }

    // create the cards here
    private final Sport[] cards;
    private final Application application;

    public SportRecyclerAdapter(Application application, Context context) {
        // create the list of cards to show here
        this.application = application;
        this.cards = new Sport[] {
                new Sport(context.getString(R.string.tennis),
                        context.getString(R.string.tennisSubtitle),
                        "images/tennis.jpg",
                        ScoreFactory.ScoreMode.K_TENNIS,
                        TennisSetupActivity.class),
                new Sport(context.getString(R.string.points_sport),
                        context.getString(R.string.pointsSubtitle),
                        "images/points.jpg",
                        ScoreFactory.ScoreMode.K_POINTS,
                        PointsSetupActivity.class)

        };
    }

    @Override
    public CardHolderSport onCreateViewHolder(ViewGroup viewGroup, int i) {
        // GRR - have to use our own counter for getting the class to create as i == 0 regardless...
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_sport, viewGroup, false);
        // create the holder and return it
        return new CardHolderSport(v);
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

package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.MatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistanceManager;
import uk.co.darkerwaters.scorepal.score.Sport;

public class CardHolderMatch extends RecyclerView.ViewHolder {

    public static final String K_SELECTED_CARD_FULL_NAME = "selected_parent";
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 102;

    private final View parent;

    private final TextView itemTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;
    private final Button resumeButton;

    private final TextView matchCompletedText;

    private final View progressLayout;
    private final View dataLayout;
    private MatchRecyclerAdapter.MatchFileListener listener;

    private ViewGroup scoreSummaryContainer;

    private LayoutScoreSummary summaryLayout = null;

    private MatchRecyclerAdapter adapter;

    private File matchFile;
    private Match loadedMatch;

    public CardHolderMatch(@NonNull View itemView) {
        super(itemView);
        this.parent = itemView;
        // card is created, find all our children views and stuff here
        this.progressLayout = this.parent.findViewById(R.id.progressLayout);
        this.dataLayout = this.parent.findViewById(R.id.dataLayout);

        this.itemImage = this.parent.findViewById(R.id.item_image);
        this.itemTitle = this.parent.findViewById(R.id.item_title);
        this.itemDetail = this.parent.findViewById(R.id.item_detail);
        this.resumeButton = this.parent.findViewById(R.id.resume_button);
        this.matchCompletedText = this.parent.findViewById(R.id.matchCompletedText);

        this.scoreSummaryContainer = this.parent.findViewById(R.id.scoreSummaryLayout);

        // show the progress
        this.progressLayout.setVisibility(View.VISIBLE);
        this.dataLayout.setVisibility(View.INVISIBLE);
    }

    public void initialiseCard(MatchRecyclerAdapter.MatchFileListener listener, final File matchFile, MatchRecyclerAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
        // hide the old game progress view
        new Thread(new Runnable() {
            @Override
            public void run() {
                // load the data in a thread to save blocking up this view
                loadMatchData(matchFile);
            }
        }).start();
    }

    private void loadMatchData(File matchFile) {
        this.matchFile = matchFile;
        // load the data
        MatchPersistanceManager persistanceManager = new MatchPersistanceManager(this.parent.getContext());
        final boolean isLoaded = persistanceManager.loadFromFile(this.matchFile);
        this.loadedMatch = persistanceManager.getMatch();
        // and put ourselves back in the UI thread
        Handler mainHandler = new Handler(this.parent.getContext().getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                // setup match data
                setupMatchData(isLoaded);
            }
        });
    }

    private void setupMatchData(boolean isValid) {
        // we are no longer loading, hide the progress
        this.progressLayout.setVisibility(View.GONE);
        this.dataLayout.setVisibility(View.VISIBLE);

        final Context context = this.parent.getContext();

        if (null != this.loadedMatch) {
            // set the title
            this.itemTitle.setText(this.loadedMatch.getSport().getTitle(context));
            // create the description
            this.itemDetail.setText(this.loadedMatch.getDescriptionShort(context));

            // so when we are here we need to get the layout for the match type
            // and inflate it to show the summary of the score
            final Sport sport = this.loadedMatch.getSport();
            switch (sport) {
                case TENNIS:
                    // inflate the tennis score summary and show the data
                    this.summaryLayout = new LayoutTennisSummary();
                    break;
                case POINTS:
                    //TODO the other types of score

                    break;
            }
            if (null != sport.imageFilename && false == sport.imageFilename.isEmpty()) {
                this.itemImage.setImageBitmap(listener.getScorepalApplication().GetBitmapFromAssets(sport.imageFilename, context));
            }

            if (null != this.summaryLayout) {
                // create this layout, first remove anything hanging around
                this.scoreSummaryContainer.removeAllViews();
                // now we can inflate the new view required by the layout class
                LayoutInflater inflater = LayoutInflater.from(this.parent.getContext());
                View layout = this.summaryLayout.createView(inflater, this.scoreSummaryContainer);
                // add this to the container
                this.scoreSummaryContainer.addView(layout);
                // now we are added, we need to initialise the data here too
                this.summaryLayout.setDataFromMatch(this.loadedMatch, this);
            }

            // also handle the button click here, show the active game for this parent
            this.resumeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // set the active match on the application
                    listener.getScorepalApplication().setActiveMatch(loadedMatch);
                    // and start the play activity to resume this match
                    if (null != sport.playActivityClass) {
                        Intent intent = new Intent(context, sport.playActivityClass);
                        context.startActivity(intent);
                    }
                    else {
                        Log.error("No play activity for " + sport.toString());
                    }
                }
            });
            // show if this is completed or not
            this.matchCompletedText.setVisibility(this.loadedMatch.isMatchOver() ? View.VISIBLE : View.INVISIBLE);
        }
        else {
            // show that this failed to load
            this.itemDetail.setText(R.string.loading_failure);
        }
    }

    public void deleteMatchFile() {
        // ask the top listener to delete this match file
        this.listener.deleteMatchFile(this.loadedMatch, this.matchFile);
    }

    public void shareMatchFile() {
        // ask the top listener to share this match file
        this.listener.shareMatchFile(this.loadedMatch, this.matchFile);
    }
}

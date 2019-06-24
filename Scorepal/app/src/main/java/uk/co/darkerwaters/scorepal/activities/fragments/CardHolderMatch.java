package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.activities.TennisPlayActivity;
import uk.co.darkerwaters.scorepal.activities.TennisSetupActivity;
import uk.co.darkerwaters.scorepal.activities.handlers.MatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistanceManager;
import uk.co.darkerwaters.scorepal.score.Sport;
import uk.co.darkerwaters.scorepal.score.TennisMatch;
import uk.co.darkerwaters.scorepal.score.TennisScore;

public class CardHolderMatch extends RecyclerView.ViewHolder {

    public static final String K_SELECTED_CARD_FULL_NAME = "selected_parent";
    private final View parent;

    private final TextView itemTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;
    private final Button resumeButton;
    private final Button deleteButton;

    private final ViewGroup moreLessLayout;
    private final Button moreLessButton;

    private final TextView matchCompletedText;

    private final View progressLayout;
    private final View dataLayout;
    private Application application;

    private final TextView[] totalPoints = new TextView[2];
    private final TextView[] breakPoints = new TextView[2];

    private ViewGroup scoreSummaryContainer;

    private LayoutScoreSummary summaryLayout = null;

    private boolean isMoreShown;
    private MatchRecyclerAdapter adapter;

    private File matchFile;

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
        this.deleteButton = this.parent.findViewById(R.id.deleteButton);
        this.matchCompletedText = this.parent.findViewById(R.id.matchCompletedText);

        this.moreLessButton = this.parent.findViewById(R.id.moreLessButton);
        this.moreLessLayout = this.parent.findViewById(R.id.moreLessLayout);

        this.totalPoints[0] = this.parent.findViewById(R.id.totalPointsText_teamOne);
        this.totalPoints[1] = this.parent.findViewById(R.id.totalPointsText_teamTwo);
        this.breakPoints[0] = this.parent.findViewById(R.id.breakPointsText_teamOne);
        this.breakPoints[1] = this.parent.findViewById(R.id.breakPointsText_teamTwo);

        this.scoreSummaryContainer = this.parent.findViewById(R.id.scoreSummaryLayout);

        // show the progress
        this.progressLayout.setVisibility(View.VISIBLE);
        this.dataLayout.setVisibility(View.INVISIBLE);
    }

    public void initialiseCard(final Application application, final File matchFile, MatchRecyclerAdapter adapter) {
        this.application = application;
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
        final Match loadedMatch = persistanceManager.getMatch();
        // and put ourselves back in the UI thread
        Handler mainHandler = new Handler(this.parent.getContext().getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                // setup match data
                setupMatchData(loadedMatch, isLoaded);
            }
        });
    }

    private void setupMatchData(final Match match, boolean isValid) {
        // we are no longer loading, hide the progress
        this.progressLayout.setVisibility(View.GONE);
        this.dataLayout.setVisibility(View.VISIBLE);

        final Context context = this.parent.getContext();
        this.isMoreShown = true;

        // so when we are here we need to get the layout for the match type
        // and inflate it to show the summary of the score
        final Sport sport = match.getSport();
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
            this.itemImage.setImageBitmap(application.GetBitmapFromAssets(sport.imageFilename, context));
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
            this.summaryLayout.setDataFromMatch(match);
        }

        // now we can get all the data we need and show it on the card
        if (false == isValid) {
            this.itemTitle.setText("invalid");
        }
        else {
            this.itemTitle.setText(match.getSport().getTitle(context));
        }
        // who won
        Team matchWinner = match.getMatchWinner();
        Team matchLoser = match.getOtherTeam(matchWinner);
        // create the description
        int minutesPlayed = match.getMatchMinutesPlayed();
        int hoursPlayed = (int)(minutesPlayed / 60f);
        minutesPlayed = minutesPlayed - (hoursPlayed * 60);
        Date matchPlayedDate = match.getMatchPlayedDate();
        String description = String.format(context.getString(R.string.match_description)
                , matchWinner.getTeamName()
                , match.isMatchOver() ? context.getString(R.string.match_beat) : context.getString(R.string.match_beating)
                , matchLoser.getTeamName()
                , String.format("%d",hoursPlayed)
                , String.format("%02d",minutesPlayed)
                , DateFormat.getTimeInstance(DateFormat.SHORT).format(matchPlayedDate)
                , DateFormat.getDateInstance(DateFormat.LONG).format(matchPlayedDate));
        // show this on the card
        this.itemDetail.setText(description);

        // also handle the button click here, show the active game for this parent
        this.resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set the active match on the application
                application.setActiveMatch(match);
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
        this.moreLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMoreLess();
            }
        });
        this.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMatch();
            }
        });

        // set the total points counters
        this.totalPoints[0].setText(Integer.toString(match.getPointsTotal(0, 0)));
        this.totalPoints[1].setText(Integer.toString(match.getPointsTotal(0, 1)));


        this.matchCompletedText.setVisibility(match.isMatchOver() ? View.VISIBLE : View.INVISIBLE);

        // and the break point counters
        //TODO will have to hide this if no such thing as break points
        if (match instanceof TennisMatch) {
            TennisScore score = ((TennisMatch)match).getScore();
            this.breakPoints[0].setText(
                    score.getBreakPointsConverted(0)
                            + " / "
                            + score.getBreakPoints(0));
            this.breakPoints[1].setText(
                    score.getBreakPointsConverted(1)
                            + " / "
                            + score.getBreakPoints(1));
        }

        // hide the more controls
        showMoreLess();
    }

    private void deleteMatch() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked, delete this data!
                        if (matchFile.delete()) {
                            adapter.removeAt(getAdapterPosition());
                        }
                        else {
                            // failed to delete for some reason
                            Toast.makeText(parent.getContext(), "Failed to delete, sorry...", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        // show the dialog to check for totally sure
        AlertDialog.Builder builder = new AlertDialog.Builder(this.parent.getContext());
        builder.setMessage(R.string.matchDeleteConfirmation)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }

    private void showMoreLess() {
        this.isMoreShown = !this.isMoreShown;
        // hide show everything except the button
        for (int i = 0; i < this.moreLessLayout.getChildCount(); ++i) {
            View child = this.moreLessLayout.getChildAt(i);
            if (child == this.moreLessButton) {
                // ignore
            }
            else {
                child.setVisibility(this.isMoreShown ? View.VISIBLE : View.GONE);
            }
        }
        if (this.isMoreShown) {
            // more is shown
            this.moreLessButton.setText(R.string.btn_less);
            this.moreLessButton.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.ic_baseline_keyboard_arrow_left_24px, 0);
        }
        else {
            // less is shown
            this.moreLessButton.setText(R.string.btn_more);
            this.moreLessButton.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    R.drawable.ic_baseline_keyboard_arrow_right_24px, 0);
        }
        // set the icon buttons to be white
        BaseActivity.SetIconTint(this.deleteButton, Color.WHITE);
        BaseActivity.SetIconTint(this.moreLessButton, Color.WHITE);
    }
}

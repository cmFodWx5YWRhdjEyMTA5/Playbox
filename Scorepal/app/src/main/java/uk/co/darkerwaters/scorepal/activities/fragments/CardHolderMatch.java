package uk.co.darkerwaters.scorepal.activities.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.TennisPlayActivity;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.application.Settings;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistanceManager;
import uk.co.darkerwaters.scorepal.score.ScoreFactory;

public class CardHolderMatch extends RecyclerView.ViewHolder {

    public static final String K_SELECTED_CARD_FULL_NAME = "selected_parent";
    private final View parent;

    private final TextView itemTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;

    private final View progressLayout;
    private final View dataLayout;
    private Application application;

    public CardHolderMatch(@NonNull View itemView) {
        super(itemView);
        this.parent = itemView;
        // card is created, find all our children views and stuff here
        this.progressLayout = this.parent.findViewById(R.id.progressLayout);
        this.dataLayout = this.parent.findViewById(R.id.dataLayout);

        this.itemImage = this.parent.findViewById(R.id.item_image);
        this.itemTitle = this.parent.findViewById(R.id.item_title);
        this.itemDetail = this.parent.findViewById(R.id.item_detail);

        // show the progress
        this.progressLayout.setVisibility(View.VISIBLE);
        this.dataLayout.setVisibility(View.INVISIBLE);
    }

    public void initialiseCard(final Application application, final File matchFile) {
        this.application = application;
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
        // load the data
        MatchPersistanceManager persistanceManager = new MatchPersistanceManager(this.parent.getContext());
        final boolean isLoaded = persistanceManager.loadFromFile(matchFile);
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

        // now we can get all the data we need and show it on the card
        if (false == isValid) {
            this.itemTitle.setText("invalid");
        }
        else {
            this.itemTitle.setText(match.getScoreMode().toString());
        }
        this.itemDetail.setText(match.getMatchPlayedDate().toString());
        /*if (null != card.image && false == card.image.isEmpty()) {
            this.itemImage.setImageBitmap(getBitmapFromAssets(card.image, parent.getContext()));
        }
        else {
            this.itemImage.setImageResource(R.drawable.piano);
        }*/

        // also handle the click here, show the active game for this parent
        final Context context = this.parent.getContext();
        this.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set the active match on the application
                application.setActiveMatch(match);
                // and start the play activity to resume this match
                Intent intent = null;
                switch (match.getScoreMode()) {
                    case K_TENNIS :
                        intent = new Intent(context, TennisPlayActivity.class);
                        break;
                    case K_POINTS:
                        Log.error("No play activity for points");
                        //intent = new Intent(context, PointsPlayActivity.class);
                        break;
                    case K_BADMINTON:
                        Log.error("No play activity for badminton");
                        //intent = new Intent(context, BadmintonPlayActivity.class);
                        break;
                    default:
                        Log.error("No play activity for ??");
                        break;
                }
                if (null != intent) {
                    context.startActivity(intent);
                }
            }
        });
    }

    public static Bitmap getBitmapFromAssets(String fileName, Context context) {
        // Custom method to get assets folder image as bitmap
        AssetManager am = context.getAssets();
        InputStream is = null;
        try{
            is = am.open(fileName);
        }catch(IOException e){
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(is);
    }
}

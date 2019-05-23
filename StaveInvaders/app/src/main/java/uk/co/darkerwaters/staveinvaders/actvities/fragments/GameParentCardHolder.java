package uk.co.darkerwaters.staveinvaders.actvities.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.actvities.GameSelectActivity;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.views.ClefProgressView;
import uk.co.darkerwaters.staveinvaders.views.GameProgressView;
import uk.co.darkerwaters.staveinvaders.views.LevelProgressView;

public class GameParentCardHolder extends RecyclerView.ViewHolder {

    public static final String K_SELECTED_CARD_FULL_NAME = "selected_parent";
    public static final String K_IS_STARTING_HELP_ON = "is_start_help_on";
    public static final String K_STARTING_TEMPO = "starting_tempo";
    private final View parent;

    private final TextView itemTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;
    private final GameProgressView progressView;
    private final ClefProgressView trebleProgressView;
    private final LevelProgressView levelsProgressView;
    private final ClefProgressView bassProgressView;

    public GameParentCardHolder(@NonNull View itemView) {
        super(itemView);
        this.parent = itemView;
        // card is created, find all our children views and stuff here
        this.itemImage = (ImageView)this.parent.findViewById(R.id.item_image);
        this.itemTitle = (TextView)this.parent.findViewById(R.id.item_title);
        this.itemDetail = (TextView)this.parent.findViewById(R.id.item_detail);
        this.progressView = (GameProgressView)this.parent.findViewById(R.id.gameProgress);

        this.trebleProgressView = this.parent.findViewById(R.id.treble_progress_view);
        this.levelsProgressView = this.parent.findViewById(R.id.levels_progress_view);
        this.bassProgressView = this.parent.findViewById(R.id.bass_progress_view);
    }

    public void initialiseCard(final Application application, final Game card) {

        // hide the old game progress view
        this.progressView.setVisibility(View.GONE);
        Settings settings = application.getSettings();
        // hide the treble / bass accordingly
        if (settings.getIsHideClef(Clef.treble)) {
            // hide treble
            this.parent.findViewById(R.id.treble_progress_layout).setVisibility(View.INVISIBLE);
        }
        if (settings.getIsHideClef(Clef.bass)) {
            // hide bass
            this.parent.findViewById(R.id.bass_progress_layout).setVisibility(View.INVISIBLE);
        }

        this.itemTitle.setText(card.name);
        this.itemDetail.setText(card.description == null ? card.getFullName() : card.description);
        if (null != card.image && false == card.image.isEmpty()) {
            this.itemImage.setImageBitmap(getBitmapFromAssets(card.image, parent.getContext()));
        }
        else {
            this.itemImage.setImageResource(R.drawable.piano);
        }
        // and the progress view
        this.progressView.setViewData(card);

        // set the progress on the bass and treble views
        this.trebleProgressView.setProgress(card, Clef.treble);
        this.bassProgressView.setProgress(card, Clef.bass);

        // and the levels too
        this.levelsProgressView.setProgress(card);

        // also handle the click here, show the active game for this parent
        final Context context = this.parent.getContext();
        this.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GameSelectActivity.class);
                intent.putExtra(K_SELECTED_CARD_FULL_NAME, card.getFullName());
                context.startActivity(intent);
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

package uk.co.darkerwaters.scorepal.activities.fragments;

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

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.SportRecyclerAdapter;
import uk.co.darkerwaters.scorepal.application.Settings;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.ScoreFactory;

public class CardHolderSport extends RecyclerView.ViewHolder {

    public static final String K_SELECTED_CARD_FULL_NAME = "selected_parent";
    private final View parent;

    private final TextView itemTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;

    public CardHolderSport(@NonNull View itemView) {
        super(itemView);
        this.parent = itemView;
        // card is created, find all our children views and stuff here
        this.itemImage = this.parent.findViewById(R.id.item_image);
        this.itemTitle = this.parent.findViewById(R.id.item_title);
        this.itemDetail = this.parent.findViewById(R.id.item_detail);
    }

    public void initialiseCard(final Application application, final SportRecyclerAdapter.Sport card) {
        this.itemTitle.setText(card.title);
        this.itemDetail.setText(card.subtitle);
        if (null != card.imageFilename && false == card.imageFilename.isEmpty()) {
            this.itemImage.setImageBitmap(application.GetBitmapFromAssets(card.imageFilename, parent.getContext()));
        }

        // also handle the click here, show the active game for this parent
        final Context context = this.parent.getContext();
        this.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // create the match from that the user just selected
                application.setActiveMatch(ScoreFactory.CreateMatchFromMode(context, card.mode));
                // and show the activity for this created match
                Intent intent = new Intent(context, card.activityClass);
                context.startActivity(intent);
            }
        });
    }
}

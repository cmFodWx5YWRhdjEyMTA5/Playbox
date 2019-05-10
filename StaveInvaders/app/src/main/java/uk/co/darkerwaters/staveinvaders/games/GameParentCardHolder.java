package uk.co.darkerwaters.staveinvaders.games;

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

import uk.co.darkerwaters.staveinvaders.actvities.GameSelectActivity;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.views.GameProgressView;

public class GameParentCardHolder extends RecyclerView.ViewHolder {

    public static final String K_SELECTED_CARD_FULL_NAME = "selected_parent";
    private final View parent;

    private final TextView itemTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;
    private final GameProgressView progressView;

    public GameParentCardHolder(@NonNull View itemView) {
        super(itemView);
        this.parent = itemView;
        // card is created, find all our children views and stuff here
        this.itemImage = (ImageView)this.parent.findViewById(R.id.item_image);
        this.itemTitle = (TextView)this.parent.findViewById(R.id.item_title);
        this.itemDetail = (TextView)this.parent.findViewById(R.id.item_detail);
        this.progressView = (GameProgressView)this.parent.findViewById(R.id.gameProgress);
    }

    public void initialiseCard(final Game card) {

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

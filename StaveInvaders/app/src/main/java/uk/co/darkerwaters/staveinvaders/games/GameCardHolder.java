package uk.co.darkerwaters.staveinvaders.games;

import android.content.Context;
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

import uk.co.darkerwaters.staveinvaders.R;

public class GameCardHolder extends RecyclerView.ViewHolder {

    private final View parent;

    private final TextView itemTitle;
    private final ImageView itemImage;
    private final TextView itemDetail;

    public GameCardHolder(@NonNull View itemView) {
        super(itemView);
        this.parent = itemView;
        // card is created, find all our children views and stuff here
        this.itemImage = (ImageView)this.parent.findViewById(R.id.item_image);
        this.itemTitle = (TextView)this.parent.findViewById(R.id.item_title);
        this.itemDetail = (TextView)this.parent.findViewById(R.id.item_detail);
    }

    public void initialiseCard(Game card) {

        this.itemTitle.setText(card.name);
        this.itemDetail.setText(card.description == null ? card.getFullName() : card.description);
        if (null != card.image && false == card.image.isEmpty()) {
            this.itemImage.setImageBitmap(getBitmapFromAssets(card.image, parent.getContext()));
        }
        else {
            this.itemImage.setImageResource(R.drawable.piano);
        }
    }

    public Bitmap getBitmapFromAssets(String fileName, Context context) {
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

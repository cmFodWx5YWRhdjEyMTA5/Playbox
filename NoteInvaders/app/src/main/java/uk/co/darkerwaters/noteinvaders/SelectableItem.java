package uk.co.darkerwaters.noteinvaders;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;

public abstract class SelectableItem {
    private String name;
    private int thumbnail;

    public SelectableItem(Activity context, String name, int thumbnail) {
        this.name = name;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public abstract String getSubtitle(Activity context);

    public abstract int getProgress(Activity context);

    public void setName(String name) {
        this.name = name;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void onBindViewHolder(SelectableItemActivity context, SelectableItemAdapter.MyViewHolder holder) {
        // default does very little
    }

    public void onDestroy(SelectableItemAdapter.MyViewHolder holder) {
        // tidy up anything here then
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

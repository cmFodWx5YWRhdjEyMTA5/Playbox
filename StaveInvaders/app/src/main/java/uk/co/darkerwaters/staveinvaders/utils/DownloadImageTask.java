package uk.co.darkerwaters.staveinvaders.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

import uk.co.darkerwaters.staveinvaders.MainActivity;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    public final ImageView bmImage;
    public final String imageUrl;

    public DownloadImageTask(ImageView bmImage, String imageUrl) {
        // set the members
        this.bmImage = bmImage;
        this.imageUrl = imageUrl;
    }

    protected Bitmap doInBackground(String... data) {
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(this.imageUrl).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(MainActivity.TAG, e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    public static String GetImageLoaded(ImageView view) {
        return view.getContentDescription().toString();
    }

    public boolean isImageDifferent() {
        return false == getImageToLoad().equals(getImageLoaded());
    }

    public String getImageToLoad() {
        return this.imageUrl == null ? "" : this.imageUrl;
    }

    public String getImageLoaded() {
        String image = this.bmImage.getContentDescription().toString();
        return image == null ? "" : image;
    }

    protected void onPostExecute(Bitmap result) {
        // set the image
        bmImage.setImageBitmap(result);
        // and remember that we did
        bmImage.setContentDescription(this.imageUrl);
    }
}
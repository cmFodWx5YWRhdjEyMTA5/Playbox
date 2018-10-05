package uk.co.darkerwaters.noteinvaders.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;

import uk.co.darkerwaters.noteinvaders.R;

public class PaletteBitmap {
    public final Palette palette;
    public final Bitmap bitmap;

    public PaletteBitmap(Bitmap bitmap, Palette palette) {
        this.bitmap = bitmap;
        this.palette = palette;
    }

    public static int getComplimentColor(int color) {
        // get existing colors
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        // find compliments
        red = (~red) & 0xff;
        blue = (~blue) & 0xff;
        green = (~green) & 0xff;

        return Color.argb(alpha, red, green, blue);
    }

    public interface PaletteBitmapImageLoader {
        void onImageLoaded(PaletteBitmap resource);
    }

    public static Palette.Swatch getBestSwatch(Palette palette) {
        Palette.Swatch bestSwatch = palette.getMutedSwatch();
        if (null == bestSwatch) {
            for (Palette.Swatch swatch : palette.getSwatches()) {
                if (null != swatch) {
                    bestSwatch = swatch;
                    break;
                }
            }
        }
        return bestSwatch;
    }

    public static boolean loadImageIntoView(Context context, ImageView target, int imageId, final PaletteBitmapImageLoader loader) {
        boolean isSuccess = false;
        try {
            Glide.with(context)
                    .load(imageId)
                    .asBitmap()
                    .transcode(new PaletteBitmapTranscoder(context), PaletteBitmap.class)
                    .fitCenter()
                    .placeholder(R.drawable.instruments)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new ImageViewTarget<PaletteBitmap>(target) {
                        @Override
                        protected void setResource(PaletteBitmap resource) {
                            super.view.setImageBitmap(resource.bitmap);
                            if (null != loader) {
                                loader.onImageLoaded(resource);
                            }
                        }
                    });
            isSuccess = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSuccess;
    }
}

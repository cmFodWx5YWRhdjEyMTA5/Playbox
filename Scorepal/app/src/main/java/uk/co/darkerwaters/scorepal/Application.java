package uk.co.darkerwaters.scorepal;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.SizeF;

import java.io.IOException;
import java.io.InputStream;

import uk.co.darkerwaters.scorepal.activities.BaseActivity;
import uk.co.darkerwaters.scorepal.activities.MainActivity;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.application.Settings;

public class Application extends android.app.Application {

    private Log log = null;
    private Settings settings = null;
    private BaseActivity mainActivity = null;
    private BaseActivity activeActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // create the log and the settings so can access our state
        this.log = Log.CreateLog(this);
        this.settings = new Settings(this);

        Log.debug("Application initialised...");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // clear all the notes from memory to help out
    }

    public SizeF getDisplaySize(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return new SizeF(dpWidth, dpHeight);
    }

    @Override
    public void onTerminate() {
        // close things down

        Log.debug("Application terminated...");
        // set everything to null, no longer around
        this.settings = null;
        this.log = null;
        this.activeActivity = null;
        this.mainActivity = null;

        // and terminate the app
        super.onTerminate();
    }

    public Log getLog() {
        return this.log;
    }

    public Settings getSettings() {
        // return the settings (exist as long as the application does)
        return this.settings;
    }

    public void setMainActivity(BaseActivity activity) {
        // set the activity to use to set things up
        this.mainActivity = activity;
    }

    public BaseActivity getMainActivity() {
        return this.mainActivity;
    }

    public BaseActivity getActiveActivity() {
        return this.activeActivity;
    }

    public void setActiveActivity(BaseActivity activity) {
        if (null == this.mainActivity) {
            setMainActivity(activity);
        }
        this.activeActivity = activity;
    }

    public void activityDestroyed(BaseActivity activity) {
        // clear the pointers as they go away
        if (this.mainActivity == activity) {
            this.mainActivity = null;
        }
        if (this.activeActivity == activity) {
            this.activeActivity = null;
        }
    }

    public static Bitmap GetBitmapFromAssets(String fileName, Context context) {
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
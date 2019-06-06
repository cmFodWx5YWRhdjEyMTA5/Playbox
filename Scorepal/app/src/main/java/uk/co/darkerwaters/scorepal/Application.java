package uk.co.darkerwaters.scorepal;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.SizeF;

import uk.co.darkerwaters.scorepal.activities.MainActivity;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.application.Settings;

public class Application extends android.app.Application {

    private Log log = null;
    private Settings settings = null;
    private MainActivity mainActivity = null;

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

    public void setMainActivity(MainActivity activity) {
        // set the activity to use to set things up
        this.mainActivity = activity;
    }

    public MainActivity getMainActivity() {
        return this.mainActivity;
    }
}
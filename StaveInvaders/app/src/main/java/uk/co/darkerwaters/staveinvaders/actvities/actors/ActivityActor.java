package uk.co.darkerwaters.staveinvaders.actvities.actors;

import android.app.Activity;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;

public abstract class ActivityActor {
    protected final Application application;
    protected final Activity parent;

    public ActivityActor(Activity parent) {
        if (parent == null || false == parent.getApplication() instanceof Application) {
            // OOPS
            Log.error("Activity from unknown app! " + parent);
        }
        this.application = (Application) parent.getApplication();
        this.parent = parent;
    }

    public abstract void close();
}

package uk.co.darkerwaters.staveinvaders;

import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;

public class Application extends android.app.Application {

    private Log log = null;
    private Settings settings = null;
    private InputSelector input = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // create the log and the settings so can access our state
        this.log = Log.CreateLog(this);
        this.settings = new Settings(this);
        this.input = new InputSelector(this);

        Log.debug("Application initialised...");
    }

    public InputSelector getInputSelector() {
        return this.input;
    }

    @Override
    public void onTerminate() {
        // close things down
        this.settings.commitChanges();
        this.input.disconnect();

        Log.debug("Application terminated...");
        // set everything to null, no longer around
        this.settings = null;
        this.input = null;
        this.log = null;

        // and terminate the app
        super.onTerminate();
    }

    public Settings getSettings() {
        // return the settings (exist as long as the application does)
        return this.settings;
    }
}

package uk.co.darkerwaters.staveinvaders.input;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputKeys extends Input {

    public InputKeys(Application application) {
        super(application);
        Log.debug("input type keys initialised");
    }

    @Override
    public void shutdown() {
        Log.debug("input type keys shutdown");
    }
}

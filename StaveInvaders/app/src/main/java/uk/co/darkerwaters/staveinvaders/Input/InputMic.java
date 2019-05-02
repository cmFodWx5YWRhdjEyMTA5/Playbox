package uk.co.darkerwaters.staveinvaders.input;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputMic extends Input {

    public InputMic(Application application) {
        super(application);
        Log.debug("input type microphone initialised");
    }

    @Override
    public void shutdown() {
        Log.debug("input type microphone shutdown");
    }
}

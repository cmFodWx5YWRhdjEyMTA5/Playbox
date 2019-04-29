package uk.co.darkerwaters.staveinvaders.Input;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputBluetooth extends InputMidi {

    public InputBluetooth(Application application) {
        super(application);
        // constructor for the input type, set everything up here
        Log.debug("input type bluetooth initialised");
    }

    @Override
    public void shutdown() {
        Log.debug("input type bluetooth shutdown");

    }
}

package uk.co.darkerwaters.staveinvaders.Input;

import uk.co.darkerwaters.staveinvaders.Application;

public class InputBluetooth extends InputMidi {

    public InputBluetooth(Application application) {
        super(application);
        // constructor for the input type, set everything up here
    }

    @Override
    public void shutdown() {

    }
}

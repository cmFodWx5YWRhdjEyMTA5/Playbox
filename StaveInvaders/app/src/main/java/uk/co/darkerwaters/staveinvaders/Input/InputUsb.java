package uk.co.darkerwaters.staveinvaders.Input;

import uk.co.darkerwaters.staveinvaders.Application;

public class InputUsb extends InputMidi {

    public InputUsb(Application application) {
        super(application);
    }

    @Override
    public void shutdown() {

    }
}

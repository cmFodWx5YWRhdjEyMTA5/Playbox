package uk.co.darkerwaters.staveinvaders.Input;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputUsb extends InputMidi {

    public InputUsb(Application application) {
        super(application);
        Log.debug("input type usb initialised");
    }

    @Override
    public void shutdown() {
        Log.debug("input type usb shutdown");
    }
}

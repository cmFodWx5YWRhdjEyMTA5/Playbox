package uk.co.darkerwaters.staveinvaders.application;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.Input.Input;
import uk.co.darkerwaters.staveinvaders.Input.InputBluetooth;
import uk.co.darkerwaters.staveinvaders.Input.InputKeys;
import uk.co.darkerwaters.staveinvaders.Input.InputMic;
import uk.co.darkerwaters.staveinvaders.Input.InputUsb;

public class InputSelector {

    private final Application application;
    private Settings.InputType activeInputType;
    private Input activeInput = null;

    public InputSelector(Application application) {
        this.application = application;

        // get the current active input type, and initialise it
        this.activeInputType = this.application.getSettings().getActiveInput();
        createInputClass();
    }

    public void disconnect() {
        // shut it all down
        shutdownActiveInput();
    }

    private void shutdownActiveInput() {
        if (null != this.activeInput) {
            this.activeInput.shutdown();
            Log.debug("input type " + this.activeInputType.toString() + " shutdown");
        }
        this.activeInput = null;
    }

    private void createInputClass() {
        // shutdown the current input
        shutdownActiveInput();
        // and create the new one
        switch (this.activeInputType) {
            case bt:
                this.activeInput = new InputBluetooth(this.application);
                break;
            case mic:
                this.activeInput = new InputMic(this.application);
                break;
            case usb:
                this.activeInput = new InputUsb(this.application);
                break;
            case keys:
            default:
                this.activeInput = new InputKeys(this.application);
                break;
        }
        Log.debug("input type " + this.activeInputType.toString() + " initialised");
    }
}

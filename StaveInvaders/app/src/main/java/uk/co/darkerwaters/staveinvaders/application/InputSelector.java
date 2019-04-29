package uk.co.darkerwaters.staveinvaders.application;

import java.util.ArrayList;

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

    private final ArrayList<InputTypeListener> listeners;

    public interface InputTypeListener {
        public void onInputTypeChanged(Settings.InputType newType);
    }

    public InputSelector(Application application) {
        this.application = application;
        this.listeners = new ArrayList<InputTypeListener>();

        // get the current active input type, and initialise it
        this.activeInputType = this.application.getSettings().getActiveInput();
        createInputClass();
    }

    public void disconnect() {
        // clear the list of listeners
        synchronized (this.listeners) {
            // inform all listeners that the connection is gone
            for (InputTypeListener listener : this.listeners) {
                listener.onInputTypeChanged(null);
            }
            this.listeners.clear();
        }
        // shut it all down
        shutdownActiveInput();
    }

    private void informListenersOfChange() {
        synchronized (this.listeners) {
            for (InputTypeListener listener : this.listeners) {
                listener.onInputTypeChanged(this.activeInputType);
            }
        }
    }

    public boolean addListener(InputTypeListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(InputTypeListener listener) {
        synchronized (this.listeners) {
            return this.listeners.remove(listener);
        }
    }

    private void shutdownActiveInput() {
        if (null != this.activeInput) {
            this.activeInput.shutdown();
        }
        this.activeInput = null;
    }

    public void changeInputType(Settings.InputType newType) {
        // set this on the settings
        this.application.getSettings().setActiveInput(newType).commitChanges();
        // and change here
        this.activeInputType = newType;
        // and create the corresponding class for this
        createInputClass();
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
        // store this change in the settings
        this.application.getSettings().setActiveInput(this.activeInputType).commitChanges();
    }
}

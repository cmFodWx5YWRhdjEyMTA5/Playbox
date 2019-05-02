package uk.co.darkerwaters.staveinvaders.application;

import java.util.ArrayList;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.input.Input;
import uk.co.darkerwaters.staveinvaders.input.InputBluetooth;
import uk.co.darkerwaters.staveinvaders.input.InputKeys;
import uk.co.darkerwaters.staveinvaders.input.InputMic;
import uk.co.darkerwaters.staveinvaders.input.InputUsb;
import uk.co.darkerwaters.staveinvaders.notes.Chord;

public class InputSelector {

    private final Application application;
    private Settings.InputType activeInputType;
    private Input activeInput = null;

    private final ArrayList<InputTypeListener> inputTypeListeners;
    private final ArrayList<InputListener> inputListeners;

    public interface InputTypeListener {
        public void onInputTypeChanged(Settings.InputType newType);
    }

    public interface InputListener {
        public void onNoteDetected(Settings.InputType type, Chord chord, boolean isDetection, float probability);
    }

    public InputSelector(Application application) {
        this.application = application;
        this.inputTypeListeners = new ArrayList<InputTypeListener>();
        this.inputListeners = new ArrayList<InputListener>();

        // get the current active input type, and initialise it
        this.activeInputType = this.application.getSettings().getActiveInput();
        createInputClass();
    }

    public void disconnect() {
        // clear the list of inputTypeListeners
        synchronized (this.inputTypeListeners) {
            // inform all inputTypeListeners that the connection is gone
            for (InputTypeListener listener : this.inputTypeListeners) {
                listener.onInputTypeChanged(null);
            }
            this.inputTypeListeners.clear();
        }
        synchronized (this.inputListeners) {
            this.inputListeners.clear();
        }
        // shut it all down
        shutdownActiveInput();
    }

    private void informListenersOfChange() {
        synchronized (this.inputTypeListeners) {
            for (InputTypeListener listener : this.inputTypeListeners) {
                listener.onInputTypeChanged(this.activeInputType);
            }
        }
    }

    public boolean addListener(InputTypeListener listener) {
        synchronized (this.inputTypeListeners) {
            return this.inputTypeListeners.add(listener);
        }
    }

    public boolean removeListener(InputTypeListener listener) {
        synchronized (this.inputTypeListeners) {
            return this.inputTypeListeners.remove(listener);
        }
    }

    public void onNoteDetected(Input input, Chord chord, boolean isPressed, float probability) {
        synchronized (this.inputListeners) {
            for (InputListener listener : this.inputListeners) {
                // inform the listener of this
                listener.onNoteDetected(this.activeInputType, chord, isPressed, probability);
            }
        }
    }

    public boolean addListener(InputListener listener) {
        synchronized (this.inputListeners) {
            return this.inputListeners.add(listener);
        }
    }

    public boolean removeListener(InputListener listener) {
        synchronized (this.inputListeners) {
            return this.inputListeners.remove(listener);
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

    public Input getActiveInput() {
        return this.activeInput;
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

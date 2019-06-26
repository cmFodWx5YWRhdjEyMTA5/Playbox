package uk.co.darkerwaters.scorepal.controllers;

import java.util.ArrayList;
import java.util.List;

public class Controller {

    public enum InputType {
        doubleTap
    }

    public interface ControllerListener {
        void onControllerInput(InputType type);
    }

    private final List<ControllerListener> listeners;

    public Controller() {
        this.listeners = new ArrayList<ControllerListener>();
    }

    public boolean addListener(ControllerListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    public boolean removeListener(ControllerListener listener) {
        synchronized (this.listeners) {
            return this.listeners.add(listener);
        }
    }

    protected void informControllerListeners(InputType type) {
        synchronized (this.listeners) {
            for (ControllerListener listener : this.listeners) {
                listener.onControllerInput(type);
            }
        }
    }
}

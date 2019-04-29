package uk.co.darkerwaters.staveinvaders.Input;

import uk.co.darkerwaters.staveinvaders.Application;

public abstract class Input {
    protected final Application application;

    public Input(Application application) {
        this.application = application;
    }

    public abstract void shutdown();
}

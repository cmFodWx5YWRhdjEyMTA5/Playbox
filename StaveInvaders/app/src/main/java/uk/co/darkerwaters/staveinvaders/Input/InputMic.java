package uk.co.darkerwaters.staveinvaders.input;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputMic extends Input {

    public InputMic(Application application) {
        super(application);
        Log.debug("input type microphone initialised");
    }

    @Override
    public void initialiseConnection() {
        super.initialiseConnection();
        // not implemented yet
        setStatus(InputSelector.Status.error);
    }

    @Override
    public int getStatusDrawable(InputSelector.Status status) {
        // if we are connected and all okay then return our icon
        switch (status) {
            case connected:
                return R.drawable.ic_baseline_mic_24px;
            case disconnected:
                return R.drawable.ic_baseline_mic_off_24px;
        }
        // else let the base class do its thing
        return super.getStatusDrawable(status);
    }

    @Override
    public void shutdown() {
        Log.debug("input type microphone shutdown");
    }
}

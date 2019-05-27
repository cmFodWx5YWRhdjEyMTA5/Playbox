package uk.co.darkerwaters.staveinvaders.input;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Log;

public class InputKeys extends Input {

    public InputKeys(Application application) {
        super(application);
        Log.debug("input type keys initialised");
    }

    @Override
    public void initialiseConnection() {
        super.initialiseConnection();
        // can only be initialised okay
        setStatus(InputSelector.Status.connected);
    }

    @Override
    public int getStatusDrawable(InputSelector.Status status) {
        // if we are connected and all okay then return our icon
        if (status == InputSelector.Status.connected) {
            if (this.application.getSettings().getIsKeyInputPiano()) {
                return R.drawable.ic_baseline_keyboard_24px;
            }
            else {
                return R.drawable.ic_baseline_az_24px;
            }
        }
        // else let the base class do its thing
        return super.getStatusDrawable(status);
    }

    @Override
    public void shutdown() {
        Log.debug("input type keys shutdown");
        // and we are disconnected
        setStatus(InputSelector.Status.disconnected);
    }
}

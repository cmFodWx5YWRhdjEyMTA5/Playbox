package uk.co.darkerwaters.staveinvaders;

import android.app.Application;

public class MainApplication extends Application {

    public static final String TAG = "StaveInvaders";

    private SigninState signinState = null;


    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onTerminate() {
        // disconnect our API access
        if (null != signinState) {
            this.signinState.disconnectApiAccess();
            this.signinState.signout();
            this.signinState = null;
        }
        super.onTerminate();
    }

    public SigninState getSigninState() {
        if (this.signinState == null) {
            this.signinState = new SigninState();
        }
        return this.signinState;
    }

}

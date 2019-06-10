package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;

import uk.co.darkerwaters.scorepal.Application;

public class BaseActivity extends AppCompatActivity {

    protected Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setup our pointers to each other so we an short-cut about the app
        this.application = (Application)getApplication();
        // set this on the application
        this.application.setActiveActivity(this);

    }

    @Override
    protected void onDestroy() {
        // tell the application this
        this.application.activityDestroyed(this);
        // and destroy us
        super.onDestroy();
    }
}

package uk.co.darkerwaters.staveinvaders.actvities.actors;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.actvities.MainActivity;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Settings;

public class NavigationListener extends ActivityActor implements NavigationView.OnNavigationItemSelectedListener, InputSelector.InputTypeListener {

    public NavigationListener(MainActivity parent) {
        super(parent);

        // want to listen to changes in the input to show this status on the menu
        InputSelector inputSelector = this.application.getInputSelector();
        if (null != inputSelector) {
            inputSelector.addListener(this);
        }
    }

    @Override
    public void close() {
        // shutdown this class, remove listeners
        InputSelector inputSelector = this.application.getInputSelector();
        if (null != inputSelector) {
            inputSelector.removeListener(this);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_camera :
                break;
            case R.id.nav_gallery:
                break;
            case R.id.nav_slideshow:
                break;
            case R.id.nav_manage:
                break;
            case R.id.input_keys:
                // change the input type
                setInputType(Settings.InputType.keys);
                break;
            case R.id.input_mic:
                // change the input type
                setInputType(Settings.InputType.mic);
                break;
            case R.id.input_bt:
                // change the input type
                setInputType(Settings.InputType.bt);
                break;
            case R.id.input_usb:
                // change the input type
                setInputType(Settings.InputType.usb);
                break;
        }

        // close the drawer
        DrawerLayout drawer = (DrawerLayout) this.parent.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setInputType(Settings.InputType type) {
        // set the new input type
        this.application.getInputSelector().changeInputType(type);
    }

    @Override
    public void onInputTypeChanged(Settings.InputType newType) {
        // called when the input type changes
        //TODO show the current active input type on the navigation drawer somehow
    }
}

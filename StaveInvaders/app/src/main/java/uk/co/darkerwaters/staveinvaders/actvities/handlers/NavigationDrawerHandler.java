package uk.co.darkerwaters.staveinvaders.actvities.handlers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.InputSelector;
import uk.co.darkerwaters.staveinvaders.application.Settings;

public class NavigationDrawerHandler extends ActionBarDrawerToggle implements InputSelector.InputTypeListener {

    private final NavigationView navigationView;
    private final Application application;
    private final Activity parent;

    public NavigationDrawerHandler(Activity parent, DrawerLayout drawer, Toolbar toolbar) {
        // setup this handler to manage the app drawer
        super(parent, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        // remember the app
        this.parent = parent;
        this.application = (Application) parent.getApplication();

        // add a listener to the draw so we can respond to button presses
        drawer.addDrawerListener(this);

        // find the view that we act upon and listen for selection changes
        this.navigationView = parent.findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // update the selection
                updateNavSelection(menuItem);
                return true;
            }
        });

        // want to listen to changes in the input to show this status on the menu
        InputSelector inputSelector = this.application.getInputSelector();
        if (null != inputSelector) {
            inputSelector.addListener(this);
        }

        // sync the state of the buttons
        syncState();
    }



    @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);

        // update the selection of the buttons
        setNavButtonSelection(application.getSettings().getActiveInput());
    }

    private void updateNavSelection(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
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

        // close the drawer now the item is selected
        DrawerLayout drawer = (DrawerLayout) this.parent.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void setInputType(Settings.InputType type) {
        // set the new input type
        this.application.getInputSelector().changeInputType(type);
    }

    @Override
    public void onInputTypeChanged(Settings.InputType newType) {
        // called when the input type changes, update the selection to show this new type
        setNavButtonSelection(newType);
    }

    public void setNavButtonSelection(Settings.InputType newType) {
        switch (newType) {
            case keys:
                this.navigationView.setCheckedItem(R.id.input_keys);
                break;
            case mic:
                this.navigationView.setCheckedItem(R.id.input_mic);
                break;
            case usb:
                this.navigationView.setCheckedItem(R.id.input_usb);
                break;
            case bt:
                this.navigationView.setCheckedItem(R.id.input_bt);
                break;
        }
    }
}

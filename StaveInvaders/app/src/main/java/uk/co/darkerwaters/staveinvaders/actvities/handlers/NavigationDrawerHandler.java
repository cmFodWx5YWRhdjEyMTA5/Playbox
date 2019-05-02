package uk.co.darkerwaters.staveinvaders.actvities.handlers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.actvities.UsbSetupActivity;
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

    private Settings.InputType ButtonToType(int id) {
        switch (id) {
            case R.id.input_keys :
                return Settings.InputType.keys;
            case R.id.input_mic :
                return Settings.InputType.mic;
            case R.id.input_bt :
                return Settings.InputType.bt;
            case R.id.input_usb :
                return Settings.InputType.usb;
        }
        return Settings.InputType.keys;
    }

    private int TypeToButton(Settings.InputType type) {
        switch (type) {
            case keys :
                return R.id.input_keys;
            case mic :
                return R.id.input_mic;
            case bt :
                return R.id.input_bt;
            case usb :
                return R.id.input_usb;
        }
        return R.id.input_keys;
    }

    private void showSettingsPage(Settings.InputType inputType) {
        //TODO show the correct settings for this type of input
        switch(inputType) {
            case keys:
                // No settings for this
                break;
            case mic:
                Toast.makeText(parent, "Settings page to do for " + inputType.toString(), Toast.LENGTH_LONG).show();
                break;
            case usb:
                Intent myIntent = new Intent(this.parent, UsbSetupActivity.class);
                //myIntent.putExtra("instrument", item.getName()); //Optional parameters
                this.parent.startActivity(myIntent);
                break;
            case bt:
                Toast.makeText(parent, "Settings page to do for " + inputType.toString(), Toast.LENGTH_LONG).show();
                break;
        }
        // close the drawer now the item is selected
        closeDrawer();
    }


    @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);

        // update the selection of the buttons
        int activeButtonId = TypeToButton(application.getSettings().getActiveInput());
        this.navigationView.setCheckedItem(activeButtonId);

        // setup the action menu item selections
        Menu menu = this.navigationView.getMenu();
        if (null != menu) {
            for (int i = 0; i < menu.size(); ++i) {
                MenuItem item = menu.getItem(i);
                ActionProvider actionProvider = MenuItemCompat.getActionProvider(item);
                if (null != actionProvider && actionProvider instanceof InputOptionSettingsHandler) {
                    // handle the clicking of this settings button
                    final Settings.InputType selectedType = ButtonToType(item.getItemId());
                    View actionView = item.getActionView();
                    InputOptionSettingsHandler inputOptionSettingsHandler = (InputOptionSettingsHandler)actionProvider;
                    inputOptionSettingsHandler.setOnClickListener(actionView, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // input keys settings shown
                            showSettingsPage(selectedType);
                        }
                    });
                    if (activeButtonId == item.getItemId()) {
                        // this is the active button, check this status
                        //TODO when this is searching, show an animation, else show a tick or something
                    }
                    else {
                        // not active, hide the animation
                        inputOptionSettingsHandler.hideProgress(actionView);
                    }
                }
            }
        }
    }

    private void updateNavSelection(MenuItem item) {
        // Handle navigation view item clicks here.
        Settings.InputType selectedType = ButtonToType(item.getItemId());
        // set this as the active type to use
        this.application.getInputSelector().changeInputType(selectedType);
        // close the drawer now the item is selected
        closeDrawer();
    }

    private void closeDrawer() {
        // close the drawer
        DrawerLayout drawer = (DrawerLayout) this.parent.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onInputTypeChanged(Settings.InputType newType) {
        // called when the input type changes, update the selection to show this new type
        int buttonId = TypeToButton(newType);
        // set this as the checked button
        this.navigationView.setCheckedItem(buttonId);
    }
}

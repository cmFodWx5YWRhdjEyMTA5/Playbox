package uk.co.darkerwaters.scorepal.activities.handlers;

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

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.MainActivity;
import uk.co.darkerwaters.scorepal.activities.SettingsActivity;
import uk.co.darkerwaters.scorepal.application.Settings;

public class NavigationDrawerHandler extends ActionBarDrawerToggle {

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

        // sync the state of the buttons
        syncState();
    }

    private void updateNavSelection(MenuItem item) {
        Intent myIntent;
        switch (item.getItemId()) {
            case R.id.nav_home:
                // go home
                myIntent = new Intent(this.parent, MainActivity.class);
                this.parent.startActivity(myIntent);
                break;
            case R.id.nav_settings:
               myIntent = new Intent(this.parent, SettingsActivity.class);
                this.parent.startActivity(myIntent);
                break;
            default:
                // Handle navigation view item clicks here.
                break;
        }
        // close the drawer now the item is selected
        closeDrawer();
    }

    private void closeDrawer() {
        // close the drawer
        DrawerLayout drawer = this.parent.findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}

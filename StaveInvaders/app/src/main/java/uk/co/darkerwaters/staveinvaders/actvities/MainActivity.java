package uk.co.darkerwaters.staveinvaders.actvities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.actvities.handlers.GameParentRecyclerAdapter;
import uk.co.darkerwaters.staveinvaders.actvities.handlers.NavigationDrawerHandler;
import uk.co.darkerwaters.staveinvaders.games.GameList;

public class MainActivity extends AppCompatActivity {

    public static final String K_OPEN_DRAWER = "open_drawer";

    private NavigationDrawerHandler navigationActor = null;
    private GridLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set this on the application
        this.application = (Application)getApplication();
        application.setMainActivity(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create the nav listener
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.navigationActor = new NavigationDrawerHandler(this, drawer, toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        int span = 1;
        switch(getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                span = 1;
                break;

        }
        layoutManager = new GridLayoutManager(MainActivity.this, span);
        recyclerView.setLayoutManager(layoutManager);

        // if we are sent a message to open the drawer, open it
        boolean isOpenDrawer = this.getIntent().getBooleanExtra(K_OPEN_DRAWER, false);
        if (isOpenDrawer) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    drawer.openDrawer(GravityCompat.START, true);
                }
            }, 250);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // update the cards from the possible settings change
        recyclerView.setAdapter(new GameParentRecyclerAdapter(application, GameList.loadGamesFromAssets(application, this)));
    }

    @Override
    protected void onDestroy() {
        // clear this on the application
        ((Application)getApplication()).setMainActivity(null);
        // and destroy us
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (((Application)this.getApplication()).getSettings().getShowMenuRight()) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

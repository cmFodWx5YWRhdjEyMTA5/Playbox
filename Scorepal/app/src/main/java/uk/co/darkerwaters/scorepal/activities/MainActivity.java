package uk.co.darkerwaters.scorepal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.widget.Toolbar;
import android.view.Menu;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.GameRecyclerAdapter;
import uk.co.darkerwaters.scorepal.activities.handlers.NavigationDrawerHandler;
import uk.co.darkerwaters.scorepal.score.MatchPersistanceManager;

public class MainActivity extends ListedActivity {

    public static final String K_OPEN_DRAWER = "open_drawer";

    private NavigationDrawerHandler navigationActor = null;

    private FloatingActionButton fabPlay;
    private GameRecyclerAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create the nav listener
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        this.navigationActor = new NavigationDrawerHandler(this, drawer, toolbar);

        this.fabPlay = findViewById(R.id.fab_play);
        this.listAdapter = new GameRecyclerAdapter(application);
        setupRecyclerView(R.id.recyclerView, this.listAdapter);

        this.fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // user wants to play
                Intent myIntent = new Intent(MainActivity.this, SelectSportActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

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

    private File[] getMatchList() {
        File[] matchList = new MatchPersistanceManager(this).listMatches();
        Arrays.sort(matchList, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                // sort in reverse filename order to put the latest at the top
                return -(file1.getName().compareTo(file2.getName()));
            }
        });
        return matchList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // setup the list to show each time we are shown in case another one appeared
        this.listAdapter.setMatches(getMatchList());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(myIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

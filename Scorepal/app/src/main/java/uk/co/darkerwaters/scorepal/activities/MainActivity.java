package uk.co.darkerwaters.scorepal.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.MatchRecyclerAdapter;
import uk.co.darkerwaters.scorepal.activities.handlers.NavigationDrawerHandler;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistanceManager;
import uk.co.darkerwaters.scorepal.score.Sport;

public class MainActivity extends ListedActivity implements MatchRecyclerAdapter.MatchFileListener {

    public static final String K_OPEN_DRAWER = "open_drawer";

    private static final int MY_PERMISSIONS_REQUEST_READ_FILES = 102;

    private NavigationDrawerHandler navigationActor = null;

    private FloatingActionButton fabPlay;
    private MatchRecyclerAdapter listAdapter;
    private File matchFileToShare;
    private Match matchToShare;

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
        this.listAdapter = new MatchRecyclerAdapter(this);
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

        // let's just quickly resolve the sport IDs to strings for nice
        Sport.ResolveSportTitles(this);
    }

    private File[] getMatchList() {
        return new MatchPersistanceManager(this).listMatches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // setup the list to show each time we are shown in case another one appeared
        this.listAdapter.updateMatches(getMatchList());
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
        if (getScorepalApplication().getSettings().getShowMenuRight()) {
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

    @Override
    public Application getScorepalApplication() {
        return this.application;
    }

    @Override
    public void deleteMatchFile(final Match match, final File matchFile) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked, delete this data!
                        if (matchFile.delete()) {
                            // also remove from the list
                            listAdapter.remove(matchFile);
                        }
                        else {
                            Toast.makeText(MainActivity.this, R.string.delete_failure, Toast.LENGTH_LONG).show();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        // show the dialog to check for totally sure
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.matchDeleteConfirmation)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show();
    }

    private void requestFileAccess(final Match match, final File matchFile) {
        // need to request permission to access files for the sharing of match data
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (this.application.getSettings().getIsRequestFileAccessPermission()) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            MY_PERMISSIONS_REQUEST_READ_FILES);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked, just send the data
                                    shareMatchData(match, matchFile, false);
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.file_access_explanation).setPositiveButton(R.string.yes, dialogClickListener)
                            .setNegativeButton(R.string.no, dialogClickListener).show();
                } else {
                    // No explanation needed; request the permission, first remember the file we will share
                    this.matchFileToShare = matchFile;
                    // and request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_FILES);

                    // MY_PERMISSIONS_REQUEST_READ_FILES is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        } else {
            // Permission has already been granted
            shareMatchData(match, matchFile, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_FILES: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    shareMatchData(matchToShare, matchFileToShare, true);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    shareMatchData(matchToShare, matchFileToShare, false);
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void shareMatchFile(Match match, File matchFile) {
        // Check whether this app has write external storage permission or not.
        this.matchToShare = match;
        this.matchFileToShare = matchFile;
        // request file access, this will come back to shareMatchData once that is resolved
        requestFileAccess(match, matchFile);
    }

    private void shareMatchData(Match match, final File matchFile, final boolean isSendFile) {
        // create the intent for sharing the data
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        // put all this in the intent
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, match.getDescriptionLong(this));

        if (isSendFile) {
            // copy the file somewhere to where we can send it from
            File destination = new File(Environment.getExternalStorageDirectory(), matchFile.getName());
            try {
                InputStream in = new FileInputStream(matchFile);
                OutputStream out = new FileOutputStream(destination);
                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                // close the streams
                in.close();
                out.close();
                // get the file URI in a shareable format
                Uri fileUri = FileProvider.getUriForFile(this,
                        getString(R.string.file_provider_authority),
                        destination);
                // put this in the intent to share it
                //sharingIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                sharingIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                // and delete the file
                destination.deleteOnExit();
            }
            catch (IOException e) {
                Log.error("Failed to copy file to external directory", e);
            }
            catch (Throwable e) {
                Log.error("Failed to share the file " + e.getMessage());
            }
        }
        // and start the intent
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}

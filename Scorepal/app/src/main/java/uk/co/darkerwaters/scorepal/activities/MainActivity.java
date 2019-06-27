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
import uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistanceManager;
import uk.co.darkerwaters.scorepal.score.Sport;

import static uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler.MY_PERMISSIONS_REQUEST_READ_CONTACTS;
import static uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler.MY_PERMISSIONS_REQUEST_READ_FILES;

public class MainActivity extends ListedActivity implements MatchRecyclerAdapter.MatchFileListener {

    public static final String K_OPEN_DRAWER = "open_drawer";

    private NavigationDrawerHandler navigationActor = null;

    private FloatingActionButton fabPlay;
    private MatchRecyclerAdapter listAdapter;
    private File matchFileToShare;
    private Match matchToShare;
    private PermissionHandler permissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // setup our data
        this.permissionHandler = null;

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

    private void requestFileAccess(Match match, File matchFile) {
        // need to request permission to access files for the sharing of match data
        this.matchToShare = match;
        this.matchFileToShare = matchFile;
        // request permission to share the file
        if (null == this.permissionHandler) {
            // there is no handler, make one here
            this.permissionHandler = new PermissionHandler(this,
                    R.string.file_access_explanation,
                    MY_PERMISSIONS_REQUEST_READ_FILES,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    new PermissionHandler.PermissionsHandlerConstructor() {
                        @Override
                        public boolean getIsRequestPermission() {
                            return application.getSettings().getIsRequestFileAccessPermission();
                        }

                        @Override
                        public void onPermissionsDenied(String[] permissions) {
                            application.getSettings().setIsRequestFileAccessPermission(false);
                            shareMatchData(matchToShare, matchFileToShare, false);
                        }

                        @Override
                        public void onPermissionsGranted(String[] permissions) {
                            shareMatchData(matchToShare, matchFileToShare, true);
                        }
                    });
        }
        // check / request access to file writing to subsequently share the file
        this.permissionHandler.requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // pass this message to our handler
        if (!this.permissionHandler.processPermissionsResult(requestCode, permissions, grantResults)) {
            // the handler didn't do anything, pass it on
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

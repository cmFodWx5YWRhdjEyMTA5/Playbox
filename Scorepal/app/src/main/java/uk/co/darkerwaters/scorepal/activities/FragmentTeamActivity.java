package uk.co.darkerwaters.scorepal.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.activities.handlers.ContactListAdapter;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.R;

public abstract class FragmentTeamActivity extends FragmentActivity implements FragmentTeam.OnFragmentInteractionListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    protected Application application;

    protected FragmentTeam teamOneFragment;
    protected FragmentTeam teamTwoFragment;
    private ContactListAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup our pointers to each other so we an short-cut about the app
        this.application = (Application)getApplication();

        // check / request access to contacts and setup the editing controls accordingly
        requestContactAccess();
    }

    @Override
    public void onAttachFragment(FragmentTeam fragmentTeam) {
        // called as a fragment is attached
        switch (fragmentTeam.getId()) {
            case R.id.team_one_fragment:
                // this is team one
                this.teamOneFragment = fragmentTeam;
                this.teamOneFragment.setLabels(1);
                break;
            case R.id.team_two_fragment:
                this.teamTwoFragment = fragmentTeam;
                this.teamTwoFragment.setLabels(2);
                break;
        }
        // setup any adapters we have created here
        setupAdapters();
    }

    private void requestContactAccess() {
        // need to request permission to access contacts for the auto-completion stuff
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (this.application.getSettings().getIsRequestContactsPermission()) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    ActivityCompat.requestPermissions(FragmentTeamActivity.this,
                                            new String[]{Manifest.permission.READ_CONTACTS},
                                            MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    createAlternativeContactList();
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.contact_access_explanation).setPositiveButton(R.string.yes, dialogClickListener)
                            .setNegativeButton(R.string.no, dialogClickListener).show();
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        } else {
            // Permission has already been granted
            createContactsAdapter();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    createContactsAdapter();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    createAlternativeContactList();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void createAlternativeContactList() {
        // if here then they said 'no' to contacts - stop asking already!
        this.application.getSettings().setIsRequestContactsPermission(false);
        // and create an alternative adapter to do the auto-completion of names
        //TODO create an auto-completion adapter for all the players we played previously
    }

    private void createContactsAdapter() {
        this.contactAdapter = new ContactListAdapter(this);
        //TODO add the auto-completion answers for all the players we played previously?
        setupAdapters();
    }

    private void setupAdapters() {
        if (null != teamOneFragment) {
            this.teamOneFragment.setAutoCompleteAdapter(this.contactAdapter);
        }
        if (null != teamTwoFragment) {
            this.teamTwoFragment.setAutoCompleteAdapter(this.contactAdapter);
        }
    }
}

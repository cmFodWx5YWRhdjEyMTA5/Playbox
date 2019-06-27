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
import uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler;

import static uk.co.darkerwaters.scorepal.activities.handlers.PermissionHandler.MY_PERMISSIONS_REQUEST_READ_CONTACTS;

public abstract class FragmentTeamActivity extends BaseFragmentActivity implements FragmentTeam.FragmentTeamInteractionListener {

    protected FragmentTeam teamOneFragment;
    protected FragmentTeam teamTwoFragment;
    private ContactListAdapter contactAdapter;

    private PermissionHandler permissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // we need to be sure to have permission to access contacts here
        this.permissionHandler = new PermissionHandler(this,
                R.string.contact_access_explanation,
                MY_PERMISSIONS_REQUEST_READ_CONTACTS,
                Manifest.permission.READ_CONTACTS,
                new PermissionHandler.PermissionsHandlerConstructor() {
                    @Override
                    public boolean getIsRequestPermission() {
                        return application.getSettings().getIsRequestContactsPermission();
                    }
                    @Override
                    public void onPermissionsDenied(String[] permissions) {
                        application.getSettings().setIsRequestContactsPermission(false);
                        createAlternativeContactList();
                    }
                    @Override
                    public void onPermissionsGranted(String[] permissions) {
                        createContactsAdapter();
                    }
                });
        // check / request access to contacts and setup the editing controls accordingly
        this.permissionHandler.requestPermission();
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

    @Override
    public void onAnimationUpdated(Float value) {
        // nothing to do
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // pass this message to our handler
        if (!this.permissionHandler.processPermissionsResult(requestCode, permissions, grantResults)) {
            // the handler didn't do anything, pass it on
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        // and remove duplicates from the contact list as we get and show it
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

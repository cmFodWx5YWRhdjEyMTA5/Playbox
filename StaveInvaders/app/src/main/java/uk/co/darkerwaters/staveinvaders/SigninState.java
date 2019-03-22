package uk.co.darkerwaters.staveinvaders;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.Task;

public class SigninState implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient = null;
    private GoogleSignInClient mGoogleSignInClient = null;
    private GoogleSignInAccount account = null;

    public static final int REQUEST_CODE_RESOLUTION = 321;
    public static final int REQUEST_CODE_SIGN_IN = 320;

    private Activity activity;

    public void create(Activity parent) {
        this.activity = parent;
        // sign in with the default account options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        this.mGoogleSignInClient = GoogleSignIn.getClient(this.activity, gso);
    }

    public void signin() {
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        this.account = GoogleSignIn.getLastSignedInAccount(this.activity);
        if (null == this.account) {
            // need to get them to do it properly
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            this.activity.startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
        }
        else {
            // are signed in, sign in for API access
            connectApiAccess();
        }
    }

    public void signout() {
        if (this.mGoogleApiClient != null) {
            disconnectApiAccess();
        }
        if (this.account != null) {
            this.mGoogleSignInClient.signOut();
            this.account = null;
        }
    }

    public void connectApiAccess() {
        if (this.mGoogleApiClient == null) {
            /**
             * Create the API client and bind it to an instance variable.
             * We use this instance as the callback for connection and connection failures.
             * Since no account name is passed, the user is prompted to choose.
             */
            this.mGoogleApiClient = new GoogleApiClient.Builder(this.activity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // connect to the API client so we can do our file things
        this.mGoogleApiClient.connect();
    }

    public void disconnectApiAccess() {
        if (mGoogleApiClient != null) {
            // disconnect Google Android Drive API connection.
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this.activity.getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this.activity.getApplicationContext(), "Disonnected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Called whenever the API client fails to connect.
        Toast.makeText(this.activity.getApplicationContext(), "Connection failed", Toast.LENGTH_LONG).show();

        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this.activity, connectionResult.getErrorCode(), 0).show();
            return;
        }

        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */
        try {
            // start the resolution
            connectionResult.startResolutionForResult(this.activity, REQUEST_CODE_RESOLUTION);

        } catch (IntentSender.SendIntentException e) {
            // log this error
            Log.e(MainApplication.TAG, "Exception while starting resolution activity", e);
        }
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            // this is the account signed into
            this.account = completedTask.getResult(ApiException.class);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(MainApplication.TAG, "signInResult:failed code=" + e.getStatusCode() + " " + e.getMessage());

        }
    }

    public boolean isSignedin() {
        return this.account != null;
    }

    public Uri getPhotoUrl() {
        return this.account == null ? Uri.EMPTY : this.account.getPhotoUrl();
    }

    public String getDisplayName() {
        return this.account == null ? "" : this.account.getDisplayName();
    }
}

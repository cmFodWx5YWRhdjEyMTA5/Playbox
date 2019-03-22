package uk.co.darkerwaters.staveinvaders;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.Task;

import uk.co.darkerwaters.staveinvaders.utils.DownloadImageTask;

public class MainSigninActivity extends MainNavigatingActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_RESOLUTION = 321;
    private static final int RC_SIGN_IN = 320;

    private SignInButton signInButton;
    private ImageView userImage;
    private TextView userTitle;

    private GoogleApiClient mGoogleApiClient = null;
    private GoogleSignInClient mGoogleSignInClient = null;
    private GoogleSignInAccount account = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the controls
        userImage = (ImageView) this.navigationHeader.findViewById(R.id.userImageView);
        userTitle = (TextView) this.navigationHeader.findViewById(R.id.userName);
        this.signInButton = (SignInButton) this.navigationHeader.findViewById(R.id.sign_in_button);

        // Set the dimensions of the sign-in button and handle the click
        this.signInButton.setSize(SignInButton.SIZE_STANDARD);
        this.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        // sign in to the default account
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        this.mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        updateControls();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        this.account = GoogleSignIn.getLastSignedInAccount(this);
        if (null != this.account) {
            Log.i(TAG, "Google already signed in: " + account.getDisplayName());
            updateControls();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.mGoogleApiClient == null) {
            /**
             * Create the API client and bind it to an instance variable.
             * We use this instance as the callback for connection and connection failures.
             * Since no account name is passed, the user is prompted to choose.
             */
            this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // connect to the API client so we can do our file things
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // disconnect any APIs
        if (mGoogleApiClient != null) {
            // disconnect Google Android Drive API connection.
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(), "Connection suspended", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + connectionResult.toString());

        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }

        /**
         *  The failure has a resolution. Resolve it.
         *  Called typically when the app is not yet authorized, and an  authorization
         *  dialog is displayed to the user.
         */

        try {

            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);

        } catch (IntentSender.SendIntentException e) {

            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        switch (requestCode) {
            case ConnectionResult.SIGN_IN_REQUIRED:
                //signIn();
                break;
            case RC_SIGN_IN:
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
                break;
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            this.account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateControls();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode() + " " + e.getMessage());
            updateControls();
        }
    }

    private void updateControls() {
        // reset the image to default
        this.userImage.setImageResource(R.drawable.ic_baseline_account_circle_24px);
        // set the user etc based on the logged in account
        if (null == this.account) {
            this.userTitle.setVisibility(View.GONE);
            this.signInButton.setVisibility(View.VISIBLE);
        }
        else {
            // show the image data
            DownloadImageTask task = new DownloadImageTask(this.userImage, this.account.getPhotoUrl().toString());
            if (task.isImageDifferent()) {
                // this is a change, execute it to load the image
                task.execute();
            }
            this.userTitle.setText(this.account.getDisplayName());
            // and show the title and hide the sign-in buttons
            this.userTitle.setVisibility(View.VISIBLE);
            this.signInButton.setVisibility(View.GONE);
        }

    }
}

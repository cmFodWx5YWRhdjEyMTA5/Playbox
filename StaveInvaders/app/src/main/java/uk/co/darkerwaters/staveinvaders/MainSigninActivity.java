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

public class MainSigninActivity extends MainNavigatingActivity {


    private SignInButton signInButton;
    private ImageView userImage;
    private TextView userTitle;

    private SigninState account = null;

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
                if (null != MainSigninActivity.this.account) {
                    MainSigninActivity.this.account.signin();
                }
            }
        });
        // create our accounts
        this.account = ((MainApplication)getApplication()).getSigninState();
        this.account.create(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // sign in to our account when we start up
        this.account.signin();
        // update our controls accordingly
        updateControls();
    }

    @Override
    protected void onStop() {
        // DO NOT SIGN OUT - APPLICATION WILL FOR US
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        switch (requestCode) {
            case ConnectionResult.SIGN_IN_REQUIRED:
                //signIn();
                break;
            case SigninState.REQUEST_CODE_SIGN_IN:
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                this.account.handleSignInResult(task);
                break;
        }
        // update our controls now then
        updateControls();
    }

    private void updateControls() {
        // reset the image to default
        this.userImage.setImageResource(R.drawable.ic_baseline_account_circle_24px);
        // set the user etc based on the logged in account
        if (null == this.account || this.account.isSignedin() == false) {
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

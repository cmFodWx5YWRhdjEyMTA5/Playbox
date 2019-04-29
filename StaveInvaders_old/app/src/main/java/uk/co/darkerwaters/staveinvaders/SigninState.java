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
import com.google.android.gms.common.api.ApiException;

import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SigninState  {

    private GoogleSignInClient mGoogleSignInClient = null;
    private GoogleSignInAccount account = null;

    public static final int REQUEST_CODE_SIGN_IN = 320;
    public static final int REQUEST_CODE_DRIVE_ACCESS = 321;

    private Activity activity;
    private DriveServiceHelper mDriveServiceHelper = null;
    private final Executor mDriveExecutor = Executors.newSingleThreadExecutor();

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

        Scope SCOPE_DRIVE_ACCESS = new Scope("https://www.googleapis.com/auth/drive.appdata");

        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(this.activity),
                SCOPE_DRIVE_ACCESS)) {
            GoogleSignIn.requestPermissions(this.activity,
                    REQUEST_CODE_DRIVE_ACCESS,
                    GoogleSignIn.getLastSignedInAccount(this.activity),
                    SCOPE_DRIVE_ACCESS);
        } else {
            getDriveFiles();
        }
    }

    public void signout() {

        if (this.account != null) {
            this.mGoogleSignInClient.signOut();
            this.account = null;
        }
    }

    public void getDriveFiles() {
        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        this.activity, Collections.singleton(DriveScopes.DRIVE_APPDATA));
        credential.setSelectedAccount(account.getAccount());
        final com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName(MainApplication.TAG)
                        .build();

        Tasks.call(mDriveExecutor, new Callable<File>(){
            @Override
            public File call() throws Exception {
                File fileMetadata = new File();
                try {
                    fileMetadata.setName("config.json");
                    fileMetadata.setParents(Collections.singletonList("appDataFolder"));
                    java.io.File filePath = new java.io.File("files/config.json");
                    FileContent mediaContent = new FileContent("application/json", filePath);
                    File file = googleDriveService.files().create(fileMetadata, mediaContent)
                            .setFields("id")
                            .execute();
                    System.out.println("File ID: " + file.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return fileMetadata;
            }});
        Tasks.call(mDriveExecutor, new Callable<List<File>>() {
            @Override
            public List<File> call() throws Exception {
                List<File> returnList = new ArrayList<File>();
                try {
                    FileList files = googleDriveService.files().list()
                            .setSpaces("appDataFolder")
                            .setFields("nextPageToken, files(id, name)")
                            .setPageSize(10)
                            .execute();
                    for (File file : files.getFiles()) {
                        System.out.printf("Found file: %s (%s)\n",
                                file.getName(), file.getId());
                        returnList.add(file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return returnList;
            }});
    }

    public void disconnectApiAccess() {

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

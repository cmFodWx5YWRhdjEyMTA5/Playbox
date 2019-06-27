package uk.co.darkerwaters.scorepal.activities.handlers;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import uk.co.darkerwaters.scorepal.R;

public class PermissionHandler {

    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 101;
    public static final int MY_PERMISSIONS_REQUEST_READ_FILES = 102;
    public static final int MY_PERMISSIONS_REQUEST_BLUETOOTH = 103;

    public interface PermissionsHandlerConstructor {
        boolean getIsRequestPermission();
        void onPermissionsDenied(String[] permissions);
        void onPermissionsGranted(String[] permissions);
    }

    private final Activity context;
    private final int explanationStringId;
    private final int requestId;
    private final String[] permissionsIds;
    private final PermissionsHandlerConstructor constructor;

    private boolean isRequestFromDialog = false;

    public PermissionHandler(Activity context,
                             int explanationStringId,
                             int requestId,
                             String permissionId,
                             PermissionsHandlerConstructor constructor) {
        // setup the members
        this.context = context;
        this.explanationStringId = explanationStringId;
        this.requestId = requestId;
        this.permissionsIds = new String[] {permissionId};
        this.constructor = constructor;
    }

    public PermissionHandler(Activity context,
                             int explanationStringId,
                             int requestId,
                             String[] permissionsIds,
                             PermissionsHandlerConstructor constructor) {
        // setup the members
        this.context = context;
        this.explanationStringId = explanationStringId;
        this.requestId = requestId;
        this.permissionsIds = permissionsIds;
        this.constructor = constructor;
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        boolean isMessageNeeded = false;
        if (this.context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (constructor.getIsRequestPermission()
                    || this.context.shouldShowRequestPermissionRationale(permission))
                isMessageNeeded = true;
        }
        return isMessageNeeded;
    }

    public void requestPermission() {
        // need to request permission for the things we need permissions for
        final List<String> permissionsNeeded = new ArrayList<String>();
        boolean messageNeeded = false;
        for (String permissionId : this.permissionsIds) {
            if (addPermission(permissionsNeeded, permissionId)) {
                messageNeeded = true;
            }
        }
        if (permissionsNeeded.size() > 0) {
            if (messageNeeded) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked,
                                isRequestFromDialog = true;
                                ActivityCompat.requestPermissions(PermissionHandler.this.context,
                                        permissionsNeeded.toArray(new String[0]),
                                        PermissionHandler.this.requestId);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                PermissionHandler.this.constructor.onPermissionsDenied(permissionsIds);
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(PermissionHandler.this.context);
                builder.setMessage(explanationStringId).setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();
            }
            else {
                // No explanation needed; request the permission
                this.isRequestFromDialog = false;
                ActivityCompat.requestPermissions(PermissionHandler.this.context,
                        permissionsNeeded.toArray(new String[0]),
                        PermissionHandler.this.requestId);
                // the requestId is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else {
            // Permissions have already all been granted
            this.constructor.onPermissionsGranted(this.permissionsIds);
        }
    }

    public boolean processPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean isProcessed = false;
        if (requestCode == this.requestId) {
            // this is the correct ID, process this
            // If request is cancelled, the result arrays are empty.
            List<String> grantedPermissions = new ArrayList<String>();
            List<String> deniedPermissions = new ArrayList<String>();
            for (int i = 0; i < permissions.length && i < grantResults.length; ++i) {
                // create two lists - one of permissions granted and one of denied
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // granted
                    grantedPermissions.add(permissions[i]);
                }
                else {
                    // denied
                    deniedPermissions.add(permissions[i]);
                }
            }
            if (isRequestFromDialog) {
                // I want to explain why they were not asked for permission after saying 'yes'
                for (String deniedPermission : deniedPermissions) {
                    if (PermissionChecker.checkCallingOrSelfPermission(context, deniedPermission) != PackageManager.PERMISSION_GRANTED) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PermissionHandler.this.context);
                        builder.setMessage(R.string.permission_denied_neverask).setNeutralButton(R.string.ok, null).show();
                    }
                }
            }
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay!
                this.constructor.onPermissionsGranted(grantedPermissions.toArray(new String[0]));
            } else {
                // permission denied, boo!
                this.constructor.onPermissionsDenied(deniedPermissions.toArray(new String[0]));
            }
            // we processed this then
            isProcessed = true;
        }
        return isProcessed;
    }
}

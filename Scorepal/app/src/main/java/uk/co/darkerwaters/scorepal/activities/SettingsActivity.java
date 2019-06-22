package uk.co.darkerwaters.scorepal.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch mute;
    private Switch useContacts;

    private Switch dataWipeSwitch;
    private Button dataWipeButton;
    private View dataWipeExtraLayout;

    private Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.application = (Application) getApplication();

        this.mute = findViewById(R.id.muteSwitch);
        this.useContacts = findViewById(R.id.useContactSwitch);

        this.dataWipeButton = findViewById(R.id.dataWipeButton);
        this.dataWipeSwitch = findViewById(R.id.dataWipeSwitch);
        this.dataWipeExtraLayout = findViewById(R.id.dataWipeExtraLayout);

        this.mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                application.getSettings().setIsMuted(b);
                //TODO - put different settings in for speaking points and messages
                application.getSettings().setIsSpeakingPoints(!b);
                application.getSettings().setIsSpeakingMessages(!b);
            }
        });
        this.useContacts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                application.getSettings().setIsRequestContactsPermission(b);
            }
        });
        this.dataWipeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    dataWipeExtraLayout.setVisibility(View.VISIBLE);
                }
                else {
                    dataWipeExtraLayout.setVisibility(View.GONE);
                }
            }
        });
        this.dataWipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                application.getSettings().wipeAllSettings();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                        // reset the data (will hide the clear option)
                        setDataFromApp();
                    }
                };
                // show the dialog to check for totally sure
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setMessage(R.string.areYouSure).setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();

            }
        });

        // set the data here
        setDataFromApp();
    }

    private void setDataFromApp() {
        this.dataWipeSwitch.setChecked(false);
        this.mute.setChecked(application.getSettings().getIsMuted());
        this.useContacts.setChecked(application.getSettings().getIsRequestContactsPermission());
    }
}

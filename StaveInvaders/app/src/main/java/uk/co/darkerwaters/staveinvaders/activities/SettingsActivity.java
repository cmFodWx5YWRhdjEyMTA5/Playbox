package uk.co.darkerwaters.staveinvaders.activities;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.notes.Clef;

public class SettingsActivity extends AppCompatActivity {

    private Switch hideTreble;
    private Switch hideBass;
    private Switch mute;

    private Switch dataWipeSwitch;
    private Button dataWipeButton;
    private View dataWipeExtraLayout;

    private Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.application = (Application) getApplication();

        this.hideTreble = findViewById(R.id.hideTrebleSwitch);
        this.hideBass = findViewById(R.id.hideBassSwitch);
        this.mute = findViewById(R.id.muteSwitch);

        this.dataWipeButton = findViewById(R.id.dataWipeButton);
        this.dataWipeSwitch = findViewById(R.id.dataWipeSwitch);
        this.dataWipeExtraLayout = findViewById(R.id.dataWipeExtraLayout);

        // listen for changes here
        this.hideTreble.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                application.getSettings().setIsHideClef(Clef.treble, b).commitChanges();
                if (b && hideBass.isChecked()) {
                    // cannot hide both
                    hideBass.setChecked(false);
                }
            }
        });
        this.hideBass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                application.getSettings().setIsHideClef(Clef.bass, b).commitChanges();
                if (b && hideTreble.isChecked()) {
                    // cannot hide both
                    hideTreble.setChecked(false);
                }
            }
        });
        this.mute.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                application.getSettings().setIsMuted(b).commitChanges();
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
                                application.getScores().wipeAllScores();
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
        this.hideTreble.setChecked(application.getSettings().getIsHideClef(Clef.treble));
        this.hideBass.setChecked(application.getSettings().getIsHideClef(Clef.bass));
        this.mute.setChecked(application.getSettings().getIsMuted());
    }
}

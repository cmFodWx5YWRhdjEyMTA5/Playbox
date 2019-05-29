package uk.co.darkerwaters.staveinvaders.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.notes.Clef;

public class SettingsActivity extends AppCompatActivity {

    private Switch hideTreble;
    private Switch hideBass;
    private Switch mute;

    private Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.application = (Application) getApplication();

        this.hideTreble = findViewById(R.id.hideTrebleSwitch);
        this.hideBass = findViewById(R.id.hideBassSwitch);
        this.mute = findViewById(R.id.muteSwitch);

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

        // set the data here
        this.hideTreble.setChecked(application.getSettings().getIsHideClef(Clef.treble));
        this.hideBass.setChecked(application.getSettings().getIsHideClef(Clef.bass));
        this.mute.setChecked(application.getSettings().getIsMuted());
    }
}

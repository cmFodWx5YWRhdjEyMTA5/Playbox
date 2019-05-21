package uk.co.darkerwaters.staveinvaders.actvities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.views.MusicView;

public class SettingsActivity extends AppCompatActivity {

    private Switch hideTreble;
    private Switch hideBass;

    private Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.application = (Application) getApplication();

        this.hideTreble = findViewById(R.id.hideTrebleSwitch);
        this.hideBass = findViewById(R.id.hideBassSwitch);

        // listen for changes here
        this.hideTreble.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                application.getSettings().setIsHideClef(MusicView.Clefs.treble, b);
                if (b && hideBass.isChecked()) {
                    // cannot hide both
                    hideBass.setChecked(false);
                }
            }
        });
        this.hideBass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                application.getSettings().setIsHideClef(MusicView.Clefs.bass, b);
                if (b && hideTreble.isChecked()) {
                    // cannot hide both
                    hideTreble.setChecked(false);
                }
            }
        });

        // set the data here
        this.hideTreble.setChecked(application.getSettings().getIsHideClef(MusicView.Clefs.treble));
        this.hideBass.setChecked(application.getSettings().getIsHideClef(MusicView.Clefs.bass));
    }
}

package uk.co.darkerwaters.scorepal.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.application.Settings;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class TennisSetupActivity extends FragmentTeamActivity {

    private Switch singlesDoublesSwitch;
    private TextView setsText;
    private ImageButton setsLessButton;
    private ImageButton setsMoreButton;

    private FloatingActionButton fabPlay;

    private TennisSets currentSets;
    private boolean isDoubles;

    public TennisSetupActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_setup);

        this.setsText = findViewById(R.id.setsNumberText);
        this.setsMoreButton = findViewById(R.id.setsMoreImageButton);
        this.setsLessButton = findViewById(R.id.setsLessImageButton);
        this.singlesDoublesSwitch = findViewById(R.id.switchSinglesDoubles);

        this.setsMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentSets = currentSets.next();
                setActivityDataShown();
            }
        });
        this.setsLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentSets = currentSets.prev();
                setActivityDataShown();
            }
        });
        this.singlesDoublesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                TennisSetupActivity.this.isDoubles = b;
                setActivityDataShown();
            }
        });
        // get the default to start with
        this.currentSets = this.application.getSettings().getTennisSets();
        this.isDoubles = this.application.getSettings().getIsDoubles();

        this.fabPlay = findViewById(R.id.fab_play);
        this.fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // user wants to play
                Intent myIntent = new Intent(TennisSetupActivity.this, TennisPlayActivity.class);
                TennisSetupActivity.this.startActivity(myIntent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Settings settings = this.application.getSettings();
        settings.setIsDoubles(this.isDoubles);
        settings.setTennisSets(this.currentSets);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // set this data on the activity
                setActivityDataShown();
            }
        }, 500);
    }

    @Override
    public void onAnimationUpdated(Float value) {
        // re-arrange the layout
    }

    private void setActivityDataShown() {
        this.singlesDoublesSwitch.setChecked(this.isDoubles);
        switch (this.currentSets) {
            case ONE:
                this.setsText.setText(R.string.one_sets);
                break;
            case THREE:
                this.setsText.setText(R.string.three_sets);
                break;
            case FIVE:
                this.setsText.setText(R.string.five_sets);
                break;
        }
        this.teamOneFragment.setIsDoubles(this.isDoubles, false);
        this.teamTwoFragment.setIsDoubles(this.isDoubles, false);
    }
}

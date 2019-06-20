package uk.co.darkerwaters.scorepal.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.application.Settings;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class TennisSetupActivity extends FragmentTeamActivity {

    private Switch singlesDoublesSwitch;
    private TextView setsText;
    private ImageButton setsLessButton;
    private ImageButton setsMoreButton;

    private View doublesCard;
    private View summaryCard;
    private TextView matchSummary;
    private Button resetButton;

    private FloatingActionButton fabPlay;

    private Match currentMatch;

    public TennisSetupActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_setup);

        // get the current match we are settingup
        this.currentMatch = this.application.getActiveMatch();

        this.setsText = findViewById(R.id.setsNumberText);
        this.setsMoreButton = findViewById(R.id.setsMoreImageButton);
        this.setsLessButton = findViewById(R.id.setsLessImageButton);
        this.singlesDoublesSwitch = findViewById(R.id.switchSinglesDoubles);

        this.doublesCard = findViewById(R.id.doublesCard);
        this.summaryCard = findViewById(R.id.summaryCard);
        this.matchSummary = findViewById(R.id.match_summary_text);
        this.resetButton = findViewById(R.id.match_reset_button);

        // set the click handler for resetting the match
        this.resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                currentMatch.resetMatch();
                                setActivityDataShown();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };
                // show the dialog to check for totally sure
                AlertDialog.Builder builder = new AlertDialog.Builder(TennisSetupActivity.this);
                builder.setMessage(R.string.matchWipeConfirmation)
                        .setPositiveButton(R.string.yes, dialogClickListener)
                        .setNegativeButton(R.string.no, dialogClickListener).show();
            }
        });

        this.setsMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSets(+1);
            }
        });
        this.setsLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSets(-1);
            }
        });
        this.singlesDoublesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                TennisSetupActivity.this.currentMatch.setIsDoubles(b);
                setActivityDataShown();
            }
        });
        // get the default to start with
        Settings settings = this.application.getSettings();
        setCurrentSets(settings.getTennisSets());
        this.currentMatch.setIsDoubles(settings.getIsDoubles());

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
    public void onTeamNameChanged(FragmentTeam fragmentTeam) {
        // as the names change we need to set them on the active match
        if (null != this.currentMatch) {
            if (this.teamOneFragment == fragmentTeam) {
                // set the team one names
                this.currentMatch.setPlayerOneName(this.teamOneFragment.getPlayerName());
                this.currentMatch.setPlayerOnePartnerName(this.teamOneFragment.getPlayerPartnerName());
                this.currentMatch.setTeamOneName(this.teamOneFragment.getTeamName());
                // change the mode of the other fragment to match the mode of this one
                this.teamTwoFragment.setTeamNameMode(this.teamOneFragment.getTeamNameMode());
            } else {
                // set the team two names
                this.currentMatch.setPlayerTwoName(this.teamTwoFragment.getPlayerName());
                this.currentMatch.setPlayerTwoPartnerName(this.teamTwoFragment.getPlayerPartnerName());
                this.currentMatch.setTeamTwoName(this.teamTwoFragment.getTeamName());
                // change the mode of the other fragment to match the mode of this one
                this.teamOneFragment.setTeamNameMode(this.teamTwoFragment.getTeamNameMode());
            }
        }
    }

    private TennisSets getCurrentSets() {
        return TennisSets.fromValue(this.currentMatch.getScoreGoal());
    }

    private void setCurrentSets(TennisSets sets) {
        this.currentMatch.setScoreGoal(sets.val);
    }

    private void changeSets(int delta) {
        // get the current set
        TennisSets currentSets = getCurrentSets();
        // and move it on, setting it on the match
        if (delta > 0) {
            setCurrentSets(currentSets.next());
        }
        else {
            setCurrentSets(currentSets.prev());
        }
        // and update the screen
        setActivityDataShown();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // set the names on the teams, even when the user didn't change anything
        onTeamNameChanged(this.teamOneFragment);
        onTeamNameChanged(this.teamTwoFragment);

        // and update the settings
        Settings settings = this.application.getSettings();
        // set the default for is doubles or not
        settings.setIsDoubles(this.currentMatch.getIsDoubles());
        // the tennis sets
        settings.setTennisSets(getCurrentSets());
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

    private void setActivityDataShown() {
        boolean isDoubles = this.currentMatch.getIsDoubles();
        this.singlesDoublesSwitch.setChecked(isDoubles);
        switch (getCurrentSets()) {
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
        this.teamOneFragment.setIsDoubles(isDoubles, false);
        this.teamTwoFragment.setIsDoubles(isDoubles, false);

        // we need to show the summary of the current match
        if (this.currentMatch.isMatchStarted()) {
            // show the summary and the card
            this.matchSummary.setText(this.currentMatch.getMatchSummary(this));
            this.summaryCard.setVisibility(View.VISIBLE);
            // can't change between doubles and singles any more - already started
            this.doublesCard.setVisibility(View.GONE);
        }
        else {
            // hide the card - this is a new game
            this.summaryCard.setVisibility(View.GONE);
            this.doublesCard.setVisibility(View.VISIBLE);
        }
    }
}

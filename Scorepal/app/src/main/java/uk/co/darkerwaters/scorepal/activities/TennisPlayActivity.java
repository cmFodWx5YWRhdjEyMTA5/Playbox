package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import uk.co.darkerwaters.scorepal.activities.fragments.FragmentPreviousSets;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class TennisPlayActivity extends FragmentTeamActivity implements
        FragmentTeam.FragmentTeamInteractionListener,
        FragmentPreviousSets.FragmentPreviousSetsInteractionListener {

    private int teamTwoHeight = 0;
    private float teamTwoY = 0f;

    private FragmentPreviousSets previousSets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_play);

        // make the names read-only
        this.teamOneFragment.setIsReadOnly(true);
        this.teamTwoFragment.setIsReadOnly(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // store the results of the match?
    }

    @Override
    protected void onResume() {
        super.onResume();
        // animate the hiding of the partner in singles
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // set this data on the activity
                setupMatch();
            }
        }, 500);
    }

    @Override
    public void onAttachFragment(FragmentPreviousSets fragment) {
        this.previousSets = fragment;
    }

    @Override
    public void onAnimationUpdated(Float value) {
        // re-arrange the layout for singles to put the name at the bottom of the screen
        View view = this.teamTwoFragment.getView();
        if (this.teamTwoHeight <= 0) {
            // need to remember the first height
            this.teamTwoHeight = view.getHeight();
            this.teamTwoY = view.getY();
        }
        // move the view down the amount it is shrunk by
        view.setY(this.teamTwoY - value);

    }

    private void setupMatch() {
        // setup the controls on the screen
        TennisSets sets = this.application.getSettings().getTennisSets();
        boolean isDoubles = this.application.getSettings().getIsDoubles();

        this.teamOneFragment.setIsDoubles(isDoubles, false);
        this.teamTwoFragment.setIsDoubles(isDoubles, false);

        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 5; ++j) {
                this.previousSets.setSetValue(i,j,i + j);
            }
        }
        for (int k = 0; k < 10; ++k) {
            for (int i = 0; i < 2; ++i) {
                for (int j = 0; j < 5; ++j) {
                    final int value = k;
                    final int teamIndex = i;
                    final int setIndex = j;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            previousSets.setSetValue(teamIndex, setIndex, value);
                        }
                    }, 1000 * (j * value));

                }
            }
        }

    }
}

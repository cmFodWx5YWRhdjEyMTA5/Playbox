package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class TennisPlayActivity extends FragmentTeamActivity implements FragmentTeam.OnFragmentInteractionListener {

    private int teamTwoHeight = 0;
    private float teamTwoY = 0f;

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
    }
}

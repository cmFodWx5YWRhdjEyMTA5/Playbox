package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.os.Handler;

import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class TennisPlayActivity extends FragmentTeamActivity implements FragmentTeam.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_play);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // store the results of the match?
    }

    @Override
    protected void onResume() {
        super.onResume();
        // just setup the controls straight away here
        setupMatch();
    }

    @Override
    public void onAnimationUpdated(Float value) {
        //TODO re-arrange the layout for singles to put the name at the bottom of the screen

    }

    private void setupMatch() {
        // setup the controls on the screen
        TennisSets sets = this.application.getSettings().getTennisSets();
        boolean isDoubles = this.application.getSettings().getIsDoubles();

        this.teamOneFragment.setIsDoubles(isDoubles, true);
        this.teamTwoFragment.setIsDoubles(isDoubles, true);
    }
}

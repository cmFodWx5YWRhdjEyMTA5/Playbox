package uk.co.darkerwaters.scorepal.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import uk.co.darkerwaters.scorepal.FragmentTeam;
import uk.co.darkerwaters.scorepal.R;

public class TennisPlayActivity extends FragmentActivity implements FragmentTeam.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_play);
    }

    @Override
    public void onAttachFragment(FragmentTeam fragmentTeam) {

    }
}

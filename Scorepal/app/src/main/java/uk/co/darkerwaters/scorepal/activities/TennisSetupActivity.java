package uk.co.darkerwaters.scorepal.activities;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import uk.co.darkerwaters.scorepal.FragmentTeam;
import uk.co.darkerwaters.scorepal.R;

public class TennisSetupActivity extends FragmentTeamActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_setup);
    }
}

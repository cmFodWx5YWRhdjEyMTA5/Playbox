package uk.co.darkerwaters.scorepal.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTime;
import uk.co.darkerwaters.scorepal.activities.handlers.DepthPageTransformer;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentPreviousSets;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScore;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.ScreenSliderPagerAdapter;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class TennisPlayActivity extends BaseFragmentActivity implements
        FragmentPreviousSets.FragmentPreviousSetsInteractionListener,
        FragmentScore.FragmentScoreInteractionListener,
        FragmentTime.FragmentTimeInteractionListener {

    private int teamTwoHeight = 0;
    private float teamTwoY = 0f;

    private static final int NUM_PAGES = 2;
    private ViewPager scorePager;
    private PagerAdapter pagerAdapter;

    private Button scoreChangeButton;
    private Button changeButton;

    private FragmentPreviousSets previousSetsFragment;
    private FragmentScore scoreFragment;
    private FragmentTime timeFragment;

    private TextView teamOneText;
    private TextView teamTwoText;

    private Match activeMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_play);

        // get the match for which we are doing things
        this.activeMatch = this.application.getActiveMatch();

        this.changeButton = findViewById(R.id.changeButton);
        this.changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make the text marquee
                scrollTeamText(teamOneText);
                scrollTeamText(teamTwoText);
            }
        });

        this.scoreChangeButton = findViewById(R.id.scoreChangeButton);
        this.scoreChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change pages
                changeScorePages();
            }
        });

        teamOneText = findViewById(R.id.team_one_textView);
        teamTwoText = findViewById(R.id.team_two_textView);

        teamOneText.setText(getTeamOneNames());
        teamTwoText.setText(getTeamTwoNames());

        // Instantiate a ViewPager and a PagerAdapter to transition between scores
        scorePager = (ViewPager) findViewById(R.id.score_pager);
        pagerAdapter = new ScreenSliderPagerAdapter(getSupportFragmentManager(),
                new Fragment[] {
                        new FragmentScore(),
                        new FragmentPreviousSets(),
                        new FragmentTime()
                });
        scorePager.setAdapter(pagerAdapter);
        scorePager.setPageTransformer(true, new DepthPageTransformer());
        scorePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                // nothing to do
            }
            @Override
            public void onPageSelected(int i) {
                setScoreButtonText();
            }
            @Override
            public void onPageScrollStateChanged(int i) {
                // nothing to do
            }
        });

        // be sure the button is correct
        setScoreButtonText();
    }

    private void scrollTeamText(TextView teamText) {
        // reset it back to not scrolling first
        teamText.setSelected(false);
        // reset te marquee limit
        teamText.setMarqueeRepeatLimit(1);
        // and start again
        teamText.setSelected(true);
    }

    private String getTeamOneNames() {
        Team team = this.activeMatch.getTeamOne();
        Player[] players = team.getPlayers();
        StringBuilder builder = new StringBuilder();
        String playerName = players[0].getPlayerName();
        if (playerName == null || playerName.isEmpty()) {
            builder.append(getString(R.string.default_playerOneName));
        }
        else {
            builder.append(playerName);
        }

        if (this.activeMatch.getIsDoubles()) {
            builder.append(" -- ");
            playerName = players[1].getPlayerName();
            if (playerName == null || playerName.isEmpty()) {
                builder.append(getString(R.string.default_playerOnePartnerName));
            }
            else {
                builder.append(playerName);
            }
        }
        builder.append("lots of chars to make the text marquee if it can at all...");
        return builder.toString();
    }

    private String getTeamTwoNames() {
        Team team = this.activeMatch.getTeamTwo();
        Player[] players = team.getPlayers();
        StringBuilder builder = new StringBuilder();
        String playerName = players[0].getPlayerName();
        if (playerName == null || playerName.isEmpty()) {
            builder.append(getString(R.string.default_playerTwoName));
        }
        else {
            builder.append(playerName);
        }

        if (this.activeMatch.getIsDoubles()) {
            builder.append(" -- ");
            playerName = players[1].getPlayerName();
            if (playerName == null || playerName.isEmpty()) {
                builder.append(getString(R.string.default_playerTwoPartnerName));
            }
            else {
                builder.append(playerName);
            }
        }
        builder.append("lots of chars to make the text marquee if it can at all...");
        return builder.toString();
    }

    private void setScoreButtonText() {
        int currentPage = scorePager.getCurrentItem();
        if (currentPage == 0) {
            // button will change to the previous sets
            this.scoreChangeButton.setText(R.string.previous_sets);
            this.scoreChangeButton.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_baseline_keyboard_arrow_right_24px, 0);
        }
        else {
            // button will change to the current score
            this.scoreChangeButton.setText(R.string.current_score);
            this.scoreChangeButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_baseline_keyboard_arrow_left_24px, 0, 0, 0);
        }
    }

    private void changeScorePages() {
        int currentPage = scorePager.getCurrentItem();
        if (currentPage == 0) {
            // change to previous sets
            scorePager.setCurrentItem(1, true);
            this.scoreChangeButton.setText(R.string.current_score);
        }
        else {
            // change to the score
            scorePager.setCurrentItem(0, true);
            this.scoreChangeButton.setText(R.string.previous_sets);
        }
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
        this.previousSetsFragment = fragment;
    }

    @Override
    public void onAttachFragment(FragmentScore fragment) {
        this.scoreFragment = fragment;
    }

    @Override
    public void onAttachFragment(FragmentTime fragment) {
        this.timeFragment = fragment;
    }

    @Override
    public void onTimeChanged() {
        // need to update the match time
        Calendar matchStarted = Calendar.getInstance();
        matchStarted.setTime(this.activeMatch.getMatchPlayedDate());
        Calendar now = Calendar.getInstance();

        // Calculate difference in milliseconds
        long diff = now.getTimeInMillis() - matchStarted.getTimeInMillis();
        long diffMinutes = diff / (60 * 1000);

        // and show the match time
        this.timeFragment.setMatchTime((int)diffMinutes);
    }

    private void setupMatch() {
        // get the data from the match
        TennisSets sets = TennisSets.fromValue(this.activeMatch.getScoreGoal());
        boolean isDoubles = this.activeMatch.getIsDoubles();
        // show if we are doubles etc.

        // and start the match by setting the start date
        this.activeMatch.setMatchPlayedDate(new Date());

    }
}

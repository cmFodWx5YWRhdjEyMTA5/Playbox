package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTime;
import uk.co.darkerwaters.scorepal.activities.handlers.DepthPageTransformer;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentPreviousSets;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScore;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTeam;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.ScreenSliderPagerAdapter;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class TennisPlayActivity extends FragmentTeamActivity implements
        FragmentTeam.FragmentTeamInteractionListener,
        FragmentPreviousSets.FragmentPreviousSetsInteractionListener,
        FragmentScore.FragmentScoreInteractionListener,
        FragmentTime.FragmentTimeInteractionListener {

    private int teamTwoHeight = 0;
    private float teamTwoY = 0f;

    private static final int NUM_PAGES = 2;
    private ViewPager scorePager;
    private PagerAdapter pagerAdapter;

    private Button scoreChangeButton;

    private FragmentPreviousSets previousSetsFragment;
    private FragmentScore scoreFragment;
    private FragmentTime timeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_play);

        // make the names read-only
        this.teamOneFragment.setIsReadOnly(true);
        this.teamTwoFragment.setIsReadOnly(true);

        this.scoreChangeButton = findViewById(R.id.scoreChangeButton);
        this.scoreChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change pages
                changeScorePages();
            }
        });

        // Instantiate a ViewPager and a PagerAdapter to transition between scores
        scorePager = (ViewPager) findViewById(R.id.score_pager);
        pagerAdapter = new ScreenSliderPagerAdapter(getSupportFragmentManager(),
                new Fragment[] {
                        new FragmentScore(),
                        new FragmentPreviousSets()
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

        // setup the number of sets properly
        this.previousSetsFragment.setSets(sets);
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 5; ++j) {
                this.previousSetsFragment.setSetValue(i,j,i * 5 + j);
            }
        }
        this.previousSetsFragment.setTieBreakResult(2, 7, 5);
    }
}

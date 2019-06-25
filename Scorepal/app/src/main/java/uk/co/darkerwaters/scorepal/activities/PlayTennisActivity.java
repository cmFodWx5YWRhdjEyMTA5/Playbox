package uk.co.darkerwaters.scorepal.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import uk.co.darkerwaters.scorepal.activities.fragments.FragmentSounds;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTime;
import uk.co.darkerwaters.scorepal.activities.handlers.DepthPageTransformer;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentPreviousSets;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScore;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.ScreenSliderPagerAdapter;
import uk.co.darkerwaters.scorepal.announcer.SpeakService;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistanceManager;
import uk.co.darkerwaters.scorepal.score.Point;
import uk.co.darkerwaters.scorepal.score.TennisScore;

public class PlayTennisActivity extends PlayTeamActivity implements
        FragmentPreviousSets.FragmentPreviousSetsInteractionListener,
        FragmentScore.FragmentScoreInteractionListener,
        FragmentTime.FragmentTimeInteractionListener {

    private ViewPager scorePager;
    private PagerAdapter pagerAdapter;

    private FragmentPreviousSets previousSetsFragment;
    private FragmentScore scoreFragment;
    private FragmentTime timeFragment;

    private ImageView pageRight;
    private ImageView pageLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_play);

        // setup all the controls on the base
        setupPlayControls();

        // now we can do ours too
        this.pageLeft = findViewById(R.id.viewPageLeftButton);
        this.pageRight = findViewById(R.id.viewPageRightButton);

        // Instantiate a ViewPager and a PagerAdapter to transition between scores
        this.scorePager = (ViewPager) findViewById(R.id.score_pager);
        this.pagerAdapter = new ScreenSliderPagerAdapter(getSupportFragmentManager(),
                new Fragment[] {
                        new FragmentScore(),
                        new FragmentPreviousSets(),
                        new FragmentTime()
                });
        this.scorePager.setAdapter(this.pagerAdapter);
        this.scorePager.setPageTransformer(true, new DepthPageTransformer());
        this.scorePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                // nothing to do
            }
            @Override
            public void onPageSelected(int i) {
                // show the correct images
                setScoreNavigationImages();
                // and be sure the score is set okay
                updateActiveFragment();
            }
            @Override
            public void onPageScrollStateChanged(int i) {
                // nothing to do
            }
        });
        // be sure the button is correct
        setScoreNavigationImages();
    }

    @Override
    protected void onDestroy() {
        // and kill the base
        super.onDestroy();
    }

    private void updateActiveFragment() {
        switch (this.scorePager.getCurrentItem()) {
            case 0:
                showActiveScore();
                break;
            case 1:
                showActivePreviousSets();
                break;
            case 2:
                onTimeChanged();
                break;
        }
    }

    private void showActiveScore() {
        // handle if play is over now
        handlePlayEnded();
        // and update the score on the controls
        if (null != this.scoreFragment) {
            TennisScore score = (TennisScore) this.activeMatch.getScore();
            Team teamOne = this.activeMatch.getTeamOne();
            Team teamTwo = this.activeMatch.getTeamTwo();
            // set the sets - just numbers
            this.scoreFragment.setSetValue(0, Integer.toString(score.getSets(teamOne)));
            this.scoreFragment.setSetValue(1, Integer.toString(score.getSets(teamTwo)));
            // and the games
            this.scoreFragment.setGamesValue(0, Integer.toString(score.getGames(teamOne, -1)));
            this.scoreFragment.setGamesValue(1, Integer.toString(score.getGames(teamTwo, -1)));
            // and the points
            this.scoreFragment.setPointsValue(0, score.getDisplayPoint(teamOne));
            this.scoreFragment.setPointsValue(1, score.getDisplayPoint(teamTwo));
        }
    }

    private void handlePlayEnded() {
        if (!this.activeMatch.isReadOnly()) {
            // we are not started now (undone to the last), clear the start time
            clearStartedPlay();
        }
        // once we are playing, we need to hide the controls that allow the match to be setup
        setupEditingControls();
        // and setup the start button
        setupStopPlayButton();
        // need to get the tennis score, there are things here
        // that we want to be using
        TennisScore score = (TennisScore) this.activeMatch.getScore();
        if (score.isMatchOver()) {
            if (false == isPlayEnded()) {
                // we have no record of when we won, store it here
                setPlayEnded(Calendar.getInstance().getTime());
                // and update the match time displayed on the fragment
                onTimeChanged();
            }
            // show that the match is over
            if (null != this.scoreFragment) {
                // show this message
                this.scoreFragment.showMatchState(FragmentScore.ScoreState.COMPLETED);
                // and speak this message
                setSpokenMessage(this.scoreFragment.getMatchState());
            }
        } else {
            // the match is not over, this might be from an undo, get rid of the time either way
            clearEndedPlay();
        }
    }

    private void showActivePreviousSets() {
        // handle if play is over now
        handlePlayEnded();
        if (null != this.previousSetsFragment) {
            // and update the score on the controls
            TennisScore score = (TennisScore) this.activeMatch.getScore();
            Team teamOne = this.activeMatch.getTeamOne();
            Team teamTwo = this.activeMatch.getTeamTwo();
            // want to do the previous sets
            for (int i = 0; i < score.getPlayedSets(); ++i) {
                int gamesOne = score.getGames(teamOne, i);
                int gamesTwo = score.getGames(teamTwo, i);
                this.previousSetsFragment.setSetValue(0, i, gamesOne, gamesOne > gamesTwo);
                this.previousSetsFragment.setSetValue(1, i, gamesTwo, gamesTwo > gamesOne);
                if (score.isSetTieBreak(i)) {
                    // this set is / was a tie, show the score of this in brackets
                    int[] tiePoints = score.getPoints(i, gamesOne + gamesTwo - 1);
                    this.previousSetsFragment.setTieBreakResult(i, tiePoints[0], tiePoints[1]);
                }
            }
        }
    }

    @Override
    public void onFragmentScorePointsClick(int teamIndex) {
        // clicked on a points button, increment the point
        if (false == isPlayEnded()) {
            // only add points when the match is playing - not after game over...
            switch (teamIndex) {
                case 0:
                    this.activeMatch.incrementPoint(this.activeMatch.getTeamOne());
                    break;
                case 1:
                    this.activeMatch.incrementPoint(this.activeMatch.getTeamTwo());
                    break;
            }
        }
    }

    private void setScoreNavigationImages() {
        int currentPage = scorePager.getCurrentItem();
        switch(currentPage) {
            case 0:
                pageLeft.setVisibility(View.INVISIBLE);
                pageRight.setVisibility(View.VISIBLE);
                break;
            case 1:
                pageLeft.setVisibility(View.VISIBLE);
                pageRight.setVisibility(View.VISIBLE);
                break;
            case 2:
                pageLeft.setVisibility(View.VISIBLE);
                pageRight.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void showScorePage(int pageIndex) {
        int currentPage = this.scorePager.getCurrentItem();
        if (currentPage != pageIndex) {
            // change to this new page
            this.scorePager.setCurrentItem(pageIndex, true);
            // update the images
            setScoreNavigationImages();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start up the screen to set everything up
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // and show this
                updateActiveFragment();
            }
        }, 500);
    }

    @Override
    public void onMatchChanged(Match.MatchChange type) {
        // let the base do its thing
        super.onMatchChanged(type);
        // now handle the tennis things here
        switch (type) {
            case GOAL:
            case PLAYERS:
            case DOUBLES_SINGLES:
            case STARTED:
                // none of this is interesting for tennis specific things
                break;
            case BREAK_POINT:
            case BREAK_POINT_CONVERTED:
                // this might be a little more interesting, either way (at the moment) not a msg
                break;
            case DECREMENT:
            case INCREMENT:
                // points have gone up / down, make this the active fragment
                showScorePage(0);
                if (null != this.scoreFragment && false == isMessageStarted()) {
                    // this is after we showed something and added the point
                    // cancel the state scrolling already
                    this.scoreFragment.cancelMatchState();
                }
                // flow through to show the score
            case RESET:
                // the points have changed, reflect this properly
                showActiveScore();
                showActivePreviousSets();
                break;
            case DECIDING_POINT:
                // inform the players that this is 'sudden death'
                if (this.activeMatch.isReadOnly()
                        && false == isMessageStarted()
                        && null != this.scoreFragment) {
                    // show this message
                    this.scoreFragment.showMatchState(FragmentScore.ScoreState.DECIDING_POINT);
                    // and speak it
                    setSpokenMessage(this.scoreFragment.getMatchState());
                }
                break;
            case ENDS:
                // this requires a message, so send it
                if (this.activeMatch.isReadOnly()
                        && false == isMessageStarted()
                        && null != this.scoreFragment) {
                    this.scoreFragment.showMatchState(FragmentScore.ScoreState.CHANGE_ENDS);
                    // and speak it
                    setSpokenMessage(this.scoreFragment.getMatchState());
                }
                break;
            case SERVER:
                // change server
                if (this.activeMatch.isReadOnly()
                        && false == isMessageStarted()
                        && null != this.scoreFragment) {
                    // didn't change ends, but we have changed server, show this
                    this.scoreFragment.showMatchState(FragmentScore.ScoreState.CHANGE_SERVER);
                    // and speak it
                    setSpokenMessage(this.scoreFragment.getMatchState());
                }
                break;
        }
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
        // need to update the match time to include this session
        int activityMinutes = getMinutesPlayedInActivity();
        if (activityMinutes >= 0) {
            int minutesPlayed = activityMinutes + this.activeMatch.getMatchMinutesPlayed();
            if (null != this.timeFragment) {
                this.timeFragment.setMatchTime(minutesPlayed);
            }
        }
        // this is a little tick we can rely on - why don't we store the match results
        // in case there is a little crash...
        storeMatchResults(false);
    }
}

package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
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

import uk.co.darkerwaters.scorepal.Application;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTime;
import uk.co.darkerwaters.scorepal.activities.handlers.DepthPageTransformer;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentPreviousSets;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScore;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.ScreenSliderPagerAdapter;
import uk.co.darkerwaters.scorepal.announcer.SpeakService;
import uk.co.darkerwaters.scorepal.application.Log;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.Point;
import uk.co.darkerwaters.scorepal.score.TennisScore;

public class TennisPlayActivity extends BaseFragmentActivity implements
        FragmentPreviousSets.FragmentPreviousSetsInteractionListener,
        FragmentScore.FragmentScoreInteractionListener,
        FragmentTime.FragmentTimeInteractionListener,
        Match.MatchListener {

    private ViewPager scorePager;
    private PagerAdapter pagerAdapter;

    private FragmentPreviousSets previousSetsFragment;
    private FragmentScore scoreFragment;
    private FragmentTime timeFragment;

    private class TeamScene {
        int activeScene = -1;
        Team team;
        ViewGroup root;
        Scene[] scenes;
        Animation inAnimation;
        Animation outAnimation;
    }

    private TeamScene teamOneScene;
    private TeamScene teamTwoScene;

    private ImageView pageRight;
    private ImageView pageLeft;

    private View setupMatchLayout;
    private Button swapTeamStarterButton;
    private Button swapTeamServerButton;
    private Button swapEndsButton;
    private Button undoButton;

    private Match activeMatch;

    private boolean isMessageStarted = false;

    private Date playStarted;
    private Date playEnded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_play);

        // get the match for which we are doing things
        this.activeMatch = this.application.getActiveMatch();
        // we want to listen to this match to show the score as it changes
        this.activeMatch.addListener(this);

        // remember the time we started this session
        this.playStarted = Calendar.getInstance().getTime();
        this.playEnded = null;

        this.pageLeft = findViewById(R.id.viewPageLeftButton);
        this.pageRight = findViewById(R.id.viewPageRightButton);

        this.setupMatchLayout = findViewById(R.id.match_setup_layout);
        this.swapEndsButton = findViewById(R.id.swapEndsButton);
        this.swapTeamStarterButton = findViewById(R.id.swapTeamStarterButton);
        this.swapTeamServerButton = findViewById(R.id.swapTeamServerButton);

        this.undoButton = findViewById(R.id.undoButton);
        this.undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // just undo the last point
                undoLastPoint();
            }
        });

        // listen for swapping ends and servers while we are setting up a match
        this.swapTeamStarterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapStartingTeam();
            }
        });// listen for swapping ends and servers while we are setting up a match
        this.swapTeamServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapServerInTeam();
            }
        });
        this.swapEndsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swapStartingEnds();
            }
        });
        if (false == this.activeMatch.getIsDoubles() || false == application.getIsTrackDoublesServes()) {
            // we are not playing doubles, setting the starting team is like setting the start
            // server, so make the text say that
            this.swapTeamStarterButton.setText(R.string.btn_change_server);
            // and hide the between-team change button
            this.swapTeamServerButton.setVisibility(View.INVISIBLE);
        }

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

        // setup the controls from the active scenes
        setupScenes();
        // transition to the correct ends and show the server icons properly
        setServerIcons();
        // be sure the button is correct
        setScoreNavigationImages();
    }

    private void undoLastPoint() {
        this.activeMatch.undoLastPoint();
    }

    @Override
    protected void onDestroy() {
        // remove us as a listener
        this.activeMatch.removeListener(this);
        // and add the time played in this session to the active match
        this.activeMatch.addMatchMinutesPlayed(getMinutesPlayedInActivity());
        // and kill the base
        super.onDestroy();
    }

    private void setupScenes() {
        // create each class then populate with all the controls that could be there
        this.teamOneScene = new TeamScene();
        this.teamTwoScene = new TeamScene();
        // set the teams here
        this.teamOneScene.team = this.activeMatch.getTeamOne();
        this.teamTwoScene.team = this.activeMatch.getTeamTwo();
        // find the roots to the scenes
        this.teamOneScene.root = findViewById(R.id.team_one_scene);
        this.teamTwoScene.root = findViewById(R.id.team_two_scene);
        // Create the two scenes 
        this.teamOneScene.scenes = new Scene[2];
        this.teamOneScene.scenes[CourtPosition.NORTH.ordinal()] = Scene.getSceneForLayout(this.teamOneScene.root, R.layout.scene_player_north, this);
        this.teamOneScene.scenes[CourtPosition.SOUTH.ordinal()] = Scene.getSceneForLayout(this.teamOneScene.root, R.layout.scene_player_south, this);
        // and team two
        this.teamTwoScene.scenes = new Scene[2];
        this.teamTwoScene.scenes[CourtPosition.NORTH.ordinal()] = Scene.getSceneForLayout(this.teamTwoScene.root, R.layout.scene_player_north, this);
        this.teamTwoScene.scenes[CourtPosition.SOUTH.ordinal()] = Scene.getSceneForLayout(this.teamTwoScene.root, R.layout.scene_player_south, this);

        // Create the Animation objects.
        createInAnimation(this.teamOneScene);
        createOutAnimation(this.teamOneScene);
        createInAnimation(this.teamTwoScene);
        createOutAnimation(this.teamTwoScene);

        // transition these to setup everything well
        setEndScenes();
    }

    private void createOutAnimation(final TeamScene scene) {
        // and the out animation
        scene.outAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        scene.outAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                // after we fade out we need to hide the button permanently
                ImageButton rxButton = scene.root.findViewById(R.id.team_receiverButton);
                rxButton.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void createInAnimation(final TeamScene scene) {
        scene.inAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
        scene.inAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // we want to fade in, we need it to be visible to show up as we do
                ImageButton rxButton = scene.root.findViewById(R.id.team_receiverButton);
                rxButton.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void scrollTeamText(TeamScene scene) {
        TextView title = scene.root.findViewById(R.id.team_textView);
        // reset it back to not scrolling first
        title.setSelected(false);
        // reset te marquee limit
        title.setMarqueeRepeatLimit(1);
        // and start again
        title.setSelected(true);
    }

    private void setEndScenes() {
        CourtPosition t1Position = teamOneScene.team.getCourtPosition();
        CourtPosition t2Position = teamTwoScene.team.getCourtPosition();
        // animate the movement to this court position
        if (teamOneScene.activeScene != t1Position.ordinal() ||
                teamTwoScene.activeScene != t2Position.ordinal()) {
            // change this value
            teamOneScene.activeScene = t1Position.ordinal();
            teamTwoScene.activeScene = t2Position.ordinal();
            // and transition
            TransitionManager.go(teamOneScene.scenes[teamOneScene.activeScene], createTransition(teamOneScene));
            TransitionManager.go(teamTwoScene.scenes[teamTwoScene.activeScene], createTransition(teamTwoScene));
        }
    }

    @Override
    public void onMatchPointsChanged(Match.PointChange[] levelsChanged) {
        // if we are speaking then we need to announce the new score
        if (this.application.getSettings().getIsSpeaking()) {
            // the score of the match changed, need to announce this, find the top level
            // that changed, if we won a set we don't care abou the game...
            Match.PointChange topChange = null;
            for (Match.PointChange change : levelsChanged) {
                // we want to find the highest change
                if (topChange == null || change.level > topChange.level) {
                    // this is the biggest - remember this
                    topChange = change;
                }
            }
            if (null != topChange) {
                TennisScore score = (TennisScore) this.activeMatch.getScore();
                Team teamOne = this.activeMatch.getTeamOne();
                Team teamTwo = this.activeMatch.getTeamTwo();
                String message = null;
                switch (topChange.level) {
                    case TennisScore.LEVEL_POINT:
                        // the points changed, announce the points
                        Point t1Point = score.getDisplayPoint(teamOne);
                        Point t2Point = score.getDisplayPoint(teamTwo);
                        if (t1Point == TennisScore.TennisPoint.ADVANTAGE) {
                            // read advantage team one
                            message = t1Point.speakString(this)
                                    + " "
                                    + teamOne.getTeamName();
                        }
                        else if (t2Point == TennisScore.TennisPoint.ADVANTAGE) {
                            // read advantage team two
                            message = t2Point.speakString(this)
                                    + " "
                                    + teamTwo.getTeamName();
                        }
                        else if (t1Point == TennisScore.TennisPoint.DEUCE
                            && t2Point == TennisScore.TennisPoint.DEUCE) {
                            // read deuce
                            message = t1Point.speakString(this);
                        }
                        else if (t1Point.val() == t2Point.val()) {
                            // they have the same score, use the special "all" values
                            message = t1Point.speakAllString(this);
                        }
                        else {
                            // just read the score, but we want to say the server first
                            // so who is that?
                            if (teamOne.isPlayerInTeam(this.activeMatch.getCurrentServer())) {
                                // team one is serving
                                message = t1Point.speakString(this)
                                        + " "
                                        + t2Point.speakString(this);
                            }
                            else {
                                // team two is serving
                                message = t2Point.speakString(this)
                                        + " "
                                        + t1Point.speakString(this);
                            }

                        }
                        break;
                    case TennisScore.LEVEL_GAME:
                        // the games changed, announce who won the game
                        message = TennisScore.TennisPoint.GAME.speakString(this)
                                + " "
                                + topChange.team.getTeamName();
                        break;
                    case TennisScore.LEVEL_SET:
                        // the sets changed, announce who won the set
                        message = TennisScore.TennisPoint.SET.speakString(this)
                                + " "
                                + topChange.team.getTeamName();
                        break;
                }
                if (null != message) {
                    // speak it, there might be dots in the string (initials) which cause
                    // the speaking to pause too much, remove them here
                    message = message.replaceAll("[.]", "");
                    // we might also be changing ends
                    // or something, get from the score fragment the state it is showing
                    if (null != scoreFragment) {
                        String state = scoreFragment.getMatchState();
                        if (null != state && false == state.isEmpty()) {
                            // there is a state showing, speak it here
                            // we are speaking, say this after the score is announced
                            message += ". " + state;

                        }
                    }
                    // speak what we have made
                    SpeakService.SpeakMessage(this, message, true);
                }
            }
        }
    }

    @Override
    public void onMatchChanged(Match.MatchChange type) {
        switch (type) {
            case GOAL:
            case PLAYERS:
            case DOUBLES_SINGLES:
            case STARTED:
                // none of this is interesting, we are playing not setting up
                // so none should actually be changed unless by us, ignore them
                this.isMessageStarted = false;
                break;
            case DECREMENT:
                // decrement is kind of special, we might have changed sides or ends
                // without knowing about it, set the end scene and server scenes here
                setEndScenes();
                setServerIcons();
            case INCREMENT:
                // points have gone up / down, make this the active fragment
                showScorePage(0);
                if (null != this.scoreFragment && false == this.isMessageStarted) {
                    // this is after we showed something and added the point
                    // cancel the state scrolling already
                    this.scoreFragment.cancelMatchState();
                }
                // flow through to show the score
            case RESET:
                // the points have changed, reflect this properly
                showActiveScore();
                showActivePreviousSets();
                // this is something that requires no message
                this.isMessageStarted = false;
                break;
            case ENDS:
                // change ends
                setEndScenes();
                // this requires a message, so send it
                if (this.activeMatch.isReadOnly()
                        && false == this.isMessageStarted
                        && null != this.scoreFragment) {
                    this.scoreFragment.showMatchState(FragmentScore.ScoreState.CHANGE_ENDS);
                }
                this.isMessageStarted = true;
                break;
            case SERVER:
                // change server
                setServerIcons();
                if (this.activeMatch.isReadOnly()
                        && false == this.isMessageStarted
                        && null != this.scoreFragment) {
                    // didn't change ends, but we have changed server, show this
                    this.scoreFragment.showMatchState(FragmentScore.ScoreState.CHANGE_SERVER);
                }
                this.isMessageStarted = true;
                break;
        }
    }

    private void swapServerInTeam() {
        // the team that is currently serving wants to start with the other player serving
        Team teamServing = this.activeMatch.getTeamServing();
        Player currentServer = teamServing.getServingPlayer();
        // use the other player from the team as the starting server
        for (Player player : teamServing.getPlayers()) {
            if (player != currentServer) {
                // this is the other player
                this.activeMatch.setTeamStartingServer(player);
                break;
            }
        }
    }

    private void swapStartingTeam() {
        // swap over the team that is starting the match
        Team teamStarting = this.activeMatch.getTeamStarting();
        if (teamStarting == this.activeMatch.getTeamOne()) {
            // team one is starting, change this
            this.activeMatch.setTeamStarting(this.activeMatch.getTeamTwo());
        }
        else {
            // team two is starting, change this
            this.activeMatch.setTeamStarting(this.activeMatch.getTeamOne());
        }
    }

    private void swapStartingEnds() {
        // for each team, set their starting end to be the next one from where they currently are
        this.activeMatch.cycleTeamStartingEnds();
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
        // once we are playing, we need to hide the controls that allow the match to be setup
        if (this.activeMatch.isReadOnly()) {
            // we are playing, cannot edit starting params
            this.setupMatchLayout.setVisibility(View.GONE);
        } else {
            // we can edit starting things
            this.setupMatchLayout.setVisibility(View.VISIBLE);
        }
        // need to get the tennis score, there are things here
        // that we want to be using
        TennisScore score = (TennisScore) this.activeMatch.getScore();
        if (score.isMatchOver()) {
            if (null == this.playEnded) {
                // we have no record of when we won, store it here
                this.playEnded = Calendar.getInstance().getTime();
                // and update the match time displayed on the fragment
                onTimeChanged();
            }
            // show that the match is over
            if (null != this.scoreFragment) {
                this.scoreFragment.showMatchState(FragmentScore.ScoreState.COMPLETED);
                this.isMessageStarted = true;
            }
        } else {
            // the match is not over, this might be from an undo, get rid of the time either way
            this.playEnded = null;
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
                this.previousSetsFragment.setSetValue(0, i, gamesOne);
                this.previousSetsFragment.setSetValue(1, i, gamesTwo);
                if (score.isSetTieBreak(i)) {
                    // this set is / was a tie, show the score of this in brackets
                    int[] tiePoints = score.getPoints(teamOne, i, gamesOne + gamesTwo - 1);
                    this.previousSetsFragment.setTieBreakResult(i, tiePoints[0], tiePoints[1]);
                }
            }
        }
    }

    private Transition createTransition(final TeamScene scene) {
        ChangeBounds animator = new ChangeBounds();
        animator.setDuration(3000);
        animator.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                // transition has started, set the title properly
                TextView title = scene.root.findViewById(R.id.team_textView);
                title.setText(scene.team.getTeamName());
                if (activeMatch.getTeamServing() == scene.team) {
                    // this team is currently serving
                    scene.root.findViewById(R.id.team_receiverButton).setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                // when the transition ends, setup the buttons again
                setupTeamButtons(scene);
                // and take the opportinity to marquee the team titles
                scrollTeamText(teamOneScene);
                scrollTeamText(teamTwoScene);
            }
            @Override
            public void onTransitionCancel(@NonNull Transition transition) {
            }
            @Override
            public void onTransitionPause(@NonNull Transition transition) {
            }
            @Override
            public void onTransitionResume(@NonNull Transition transition) {
            }
        });
        return animator;
    }

    private void setupTeamButtons(final TeamScene scene) {
        scene.root.setOnClickListener(createTeamButtonListener(scene));
        // do when click receiver button
        ImageButton button = scene.root.findViewById(R.id.team_receiverButton);
        button.setOnClickListener(createTeamButtonListener(scene));
        // and when click server button
        button = scene.root.findViewById(R.id.team_serverButton);
        button.setOnClickListener(createTeamButtonListener(scene));
    }

    private View.OnClickListener createTeamButtonListener(final TeamScene scene) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // only add a point when play has not ended
                if (null == playEnded) {
                    activeMatch.incrementPoint(scene.team);
                }
            }
        };
    }

    @Override
    public void onFragmentScorePointsClick(int teamIndex) {
        // clicked on a points button, increment the point
        if (null == this.playEnded) {
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

    private void setServerIcons() {
        // change the icons over
        Team servingTeam = this.activeMatch.getTeamServing();
        // which team is serving?
        if (this.activeMatch.getTeamOne() == servingTeam) {
            // team one has the server, remove the receiver icon
            ImageButton rxButton = this.teamOneScene.root.findViewById(R.id.team_receiverButton);
            if (rxButton.getVisibility() != View.INVISIBLE) {
                // this is not gone, need to remove it
                rxButton.startAnimation(this.teamOneScene.outAnimation);
            }
            // and animate in the team two receive button
            rxButton = this.teamTwoScene.root.findViewById(R.id.team_receiverButton);
            if (rxButton.getVisibility() != View.VISIBLE) {
                // this is not shown, bring it in
                rxButton.startAnimation(this.teamTwoScene.inAnimation);
            }
        }
        else {
            // other way around
            ImageButton rxButton = this.teamOneScene.root.findViewById(R.id.team_receiverButton);
            if (rxButton.getVisibility() != View.VISIBLE) {
                // this is not shown, bring it in
                rxButton.startAnimation(this.teamOneScene.inAnimation);
            }
            // and animate out the team two receive button
            rxButton = this.teamTwoScene.root.findViewById(R.id.team_receiverButton);
            if (rxButton.getVisibility() != View.INVISIBLE) {
                // this is not gone, need to remove it
                rxButton.startAnimation(this.teamTwoScene.outAnimation);
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
    protected void onPause() {
        super.onPause();
        //TODO store the results of the match
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO load the last match played to restore the data

        if (this.activeMatch.getMatchPlayedDate() == null) {
            // start the new match by setting the start date
            this.activeMatch.setMatchPlayedDate(new Date());
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // and show this
                updateActiveFragment();
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

    private int getMinutesPlayedInActivity() {
        long playEndedMs;
        if (null == this.playEnded) {
            // play isn't over yet, use now
            playEndedMs = Calendar.getInstance().getTimeInMillis();
        }
        else {
            // use the play ended time
            playEndedMs = this.playEnded.getTime();
        }
        // Calculate difference in milliseconds
        long diff = playEndedMs - this.playStarted.getTime();
        // and add the time played to the active match
        return (int)(diff / 60000L);
    }

    @Override
    public void onTimeChanged() {
        // need to update the match time to include this session
        int minutesPlayed = this.activeMatch.getMatchMinutesPlayed() + getMinutesPlayedInActivity();
        if (null != this.timeFragment) {
            this.timeFragment.setMatchTime(minutesPlayed);
        }
    }
}

package uk.co.darkerwaters.scorepal.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.transition.TransitionInflater;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import uk.co.darkerwaters.scorepal.activities.fragments.FragmentTime;
import uk.co.darkerwaters.scorepal.activities.handlers.DepthPageTransformer;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentPreviousSets;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScore;
import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.handlers.ScreenSliderPagerAdapter;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.TennisScore;
import uk.co.darkerwaters.scorepal.score.TennisSets;

public class TennisPlayActivity extends BaseFragmentActivity implements
        FragmentPreviousSets.FragmentPreviousSetsInteractionListener,
        FragmentScore.FragmentScoreInteractionListener,
        FragmentTime.FragmentTimeInteractionListener,
        Match.MatchListener {

    private ViewPager scorePager;
    private PagerAdapter pagerAdapter;

    private Button scoreChangeButton;
    private Button changeButton;

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

    private Match activeMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tennis_play);

        // get the match for which we are doing things
        this.activeMatch = this.application.getActiveMatch();
        // we want to listen to this match to show the score as it changes
        this.activeMatch.addListener(this);

        this.changeButton = findViewById(R.id.changeButton);
        this.changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make the text marquee
                scrollTeamText(teamOneScene);
                scrollTeamText(teamTwoScene);
            }
        });

        this.pageLeft = findViewById(R.id.viewPageLeftButton);
        this.pageRight = findViewById(R.id.viewPageRightButton);

        this.scoreChangeButton = findViewById(R.id.scoreChangeButton);
        this.scoreChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // change pages
                for (int i = 0; i < 4; ++i) {
                    activeMatch.incrementPoint(activeMatch.getTeamOne());
                }
                setEndScenes();
                setServerIcons();
            }
        });

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

        // setup the controls from the active scenes
        setupScenes();

        setEndScenes();
        setServerIcons();

        // be sure the button is correct
        setScoreButtonText();
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
            // show we are doing this as text
            if (null != this.scoreFragment) {
                this.scoreFragment.showMatchState(FragmentScore.ScoreState.CHANGE_ENDS);
            }
        }
    }

    private void onMatchCompleted() {
        // show this on the score
        if (null != this.scoreFragment) {
            this.scoreFragment.showMatchState(FragmentScore.ScoreState.COMPLETED);
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
                break;
            case RESET:
            case DECREMENT:
            case INCREMENT:
                // the points have changed, reflect this properly
                showActiveScore();
                break;
            case ENDS:
                // change ends
                setEndScenes();
                break;
            case SERVER:
                // change server
                setServerIcons();
                break;
        }
    }

    private void showActiveScore() {
        // need to get the tennis score, there are things here
        // that we want to be using
        TennisScore score = (TennisScore) this.activeMatch.getScore();
        if (score.isMatchOver()) {
            // show that the match is over
            onMatchCompleted();
        }
        Team teamOne = this.activeMatch.getTeamOne();
        Team teamTwo = this.activeMatch.getTeamTwo();
        // set the sets - just numbers
        this.scoreFragment.setSetValue(0, Integer.toString(score.getSets(teamOne)));
        this.scoreFragment.setSetValue(1, Integer.toString(score.getSets(teamTwo)));
        // and the games
        this.scoreFragment.setGamesValue(0, Integer.toString(score.getGames(teamOne, -1)));
        this.scoreFragment.setGamesValue(1, Integer.toString(score.getGames(teamTwo, -1)));
        // and the points
        this.scoreFragment.setPointsValue(0, score.getPointsString(teamOne));
        this.scoreFragment.setPointsValue(1, score.getPointsString(teamTwo));

        // want to do the previous sets too
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

    private Transition createTransition(final TeamScene scene) {
        ChangeBounds animator = new ChangeBounds();
        animator.setDuration(3000);
        animator.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                // transition has started, set the title properly
                TextView title = scene.root.findViewById(R.id.team_textView);
                title.setText(getTeamNames(scene));
            }
            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                // when the transition ends, setup the buttons again
                setupTeamButtons(scene);
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
        // do when click recevier button
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
                activeMatch.incrementPoint(scene.team);
            }
        };
    }

    @Override
    public void onFragmentScorePointsClick(int teamIndex) {
        // clicked on a points button, increment the point
        switch (teamIndex) {
            case 0 :
                this.activeMatch.incrementPoint(this.activeMatch.getTeamOne());
                break;
            case 1:
                this.activeMatch.incrementPoint(this.activeMatch.getTeamTwo());
                break;
        }
    }

    private void setServerIcons() {
        // change the icons over
        Player currentServer = this.activeMatch.getCurrentServer();
        // which team is serving?
        if (this.activeMatch.getTeamOne().isPlayerInTeam(currentServer)) {
            // team one has the server, remove the receiver icon
            ImageButton rxButton = this.teamOneScene.root.findViewById(R.id.team_receiverButton);
            rxButton.startAnimation(this.teamOneScene.outAnimation);
            // and animate in the team two receive button
            rxButton = this.teamTwoScene.root.findViewById(R.id.team_receiverButton);
            rxButton.startAnimation(this.teamTwoScene.inAnimation);
        }
        else {
            // other way around
            ImageButton rxButton = this.teamOneScene.root.findViewById(R.id.team_receiverButton);
            rxButton.startAnimation(this.teamOneScene.inAnimation);
            // and animate out the team two receive button
            rxButton = this.teamTwoScene.root.findViewById(R.id.team_receiverButton);
            rxButton.startAnimation(this.teamTwoScene.outAnimation);
        }
        // show we are doing this as text
        if (null != this.scoreFragment) {
            this.scoreFragment.showMatchState(FragmentScore.ScoreState.CHANGE_SERVER);
        }
    }

    private String getTeamNames(TeamScene scene) {
        Player[] players = scene.team.getPlayers();
        StringBuilder builder = new StringBuilder();
        String playerName = players[0].getPlayerName();
        if (playerName == null || playerName.isEmpty()) {
            // do player one / player two
            if (this.activeMatch.getTeamOne() == scene.team) {
                builder.append(getString(R.string.default_playerOneName));
            }
            else {
                builder.append(getString(R.string.default_playerTwoName));
            }
        }
        else {
            builder.append(playerName);
        }

        if (this.activeMatch.getIsDoubles()) {
            builder.append(" -- ");
            playerName = players[1].getPlayerName();
            if (playerName == null || playerName.isEmpty()) {
                if (this.activeMatch.getTeamOne() == scene.team) {
                    builder.append(getString(R.string.default_playerOnePartnerName));
                }
                else {
                    builder.append(getString(R.string.default_playerTwoPartnerName));
                }
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

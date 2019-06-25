package uk.co.darkerwaters.scorepal.activities;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.transition.ChangeBounds;
import android.support.transition.Scene;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;

public abstract class PlayTeamActivity extends PlayActivity {

    private class TeamScene {
        int teamColor;
        int activeScene = -1;
        Team team;
        ViewGroup root;
        Scene[] scenes;
        Animation inAnimation;
        Animation outAnimation;
    }

    private TeamScene teamOneScene;
    private TeamScene teamTwoScene;

    private View setupMatchLayout;
    private Button swapTeamStarterButton;
    private Button swapTeamServerButton;
    private Button swapEndsButton;

    @Override
    protected void setupPlayControls() {
        super.setupPlayControls();

        this.setupMatchLayout = findViewById(R.id.match_setup_layout);
        this.swapEndsButton = findViewById(R.id.swapEndsButton);
        this.swapTeamStarterButton = findViewById(R.id.swapTeamStarterButton);
        this.swapTeamServerButton = findViewById(R.id.swapTeamServerButton);

        // set the button colours correctly
        BaseActivity.SetIconTint(this.swapEndsButton, Color.WHITE);
        BaseActivity.SetIconTint(this.swapTeamStarterButton, Color.WHITE);
        BaseActivity.SetIconTint(this.swapTeamServerButton, Color.WHITE);

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

        // setup the controls from the active scenes
        setupTeamScenes();
        // transition to the correct ends and show the server icons properly
        setServerIcons();
    }

    @Override
    public void onMatchChanged(Match.MatchChange type) {
        // let the base try
        super.onMatchChanged(type);
        // and handle our team swapping here
        switch (type) {
            case DECREMENT:
                // decrement is kind of special, we might have changed sides or ends
                // without knowing about it, set the end scene and server scenes here
                setEndScenes();
                setServerIcons();
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

    @Override
    protected void onPlayStarted(Date playStarted) {
        // as play is started, setup our editing controls properly
        setupEditingControls();
    }

    protected void setupEditingControls() {
        if (null != this.setupMatchLayout) {
            if (!this.activeMatch.isReadOnly()) {
                // we were playing, but now we are not, put back the editing options
                this.setupMatchLayout.setVisibility(View.VISIBLE);
            }
            if (this.activeMatch.isReadOnly() || isPlayStarted()) {
                // we are playing, cannot edit starting params
                this.setupMatchLayout.setVisibility(View.GONE);
            }
        }
    }

    private void setupTeamScenes() {
        // create each class then populate with all the controls that could be there
        this.teamOneScene = new TeamScene();
        this.teamTwoScene = new TeamScene();
        // set the colours
        this.teamOneScene.teamColor = getColor(R.color.teamOneColor);
        this.teamTwoScene.teamColor = getColor(R.color.teamTwoColor);
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
                if (false == isPlayEnded()) {
                    activeMatch.incrementPoint(scene.team);
                }
            }
        };
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

    private Transition createTransition(final TeamScene scene) {
        ChangeBounds animator = new ChangeBounds();
        animator.setDuration(3000);
        animator.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                // transition has started, set the title properly
                TextView title = scene.root.findViewById(R.id.team_textView);
                title.setText(scene.team.getTeamName());
                title.setTextColor(scene.teamColor);
                // set the colours of the buttons also
                ImageButton rxButton = scene.root.findViewById(R.id.team_receiverButton);
                BaseActivity.SetIconTint(rxButton, scene.teamColor);
                // colour the server too
                ImageButton txButton = scene.root.findViewById(R.id.team_serverButton);
                BaseActivity.SetIconTint(txButton, scene.teamColor);
                // and show / hide the rx button accordingly
                if (activeMatch.getTeamServing() == scene.team) {
                    // this team is currently serving
                    rxButton.setVisibility(View.INVISIBLE);
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
}

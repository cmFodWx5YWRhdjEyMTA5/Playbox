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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import uk.co.darkerwaters.scorepal.R;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentScore;
import uk.co.darkerwaters.scorepal.activities.fragments.FragmentSounds;
import uk.co.darkerwaters.scorepal.announcer.SpeakService;
import uk.co.darkerwaters.scorepal.players.CourtPosition;
import uk.co.darkerwaters.scorepal.players.Player;
import uk.co.darkerwaters.scorepal.players.Team;
import uk.co.darkerwaters.scorepal.score.Match;
import uk.co.darkerwaters.scorepal.score.MatchPersistanceManager;
import uk.co.darkerwaters.scorepal.score.Point;
import uk.co.darkerwaters.scorepal.score.TennisScore;

public abstract class PlayActivity extends BaseFragmentActivity implements
        FragmentSounds.FragmentSoundsInteractionListener,
        Match.MatchListener {

    private FragmentSounds soundsFragment;

    private Button undoButton;
    private Button stopPlayButton;

    protected Match activeMatch;

    private boolean isMessageStarted = false;

    private Date playStarted;
    private Date playEnded;

    private SpeakService speakService = null;
    private String spokenMessage = null;

    protected void setupPlayControls() {

        // get the match for which we are doing things
        this.activeMatch = this.application.getActiveMatch();
        // we want to listen to this match to show the score as it changes
        this.activeMatch.addListener(this);

        // remember the time we started this session
        this.playStarted = null;
        this.playEnded = null;

        // find the controls on this activity
        this.undoButton = findViewById(R.id.undoButton);
        this.stopPlayButton = findViewById(R.id.endMatchButton);

        // be sure these icons are tinted correctly
        BaseActivity.SetIconTint(this.undoButton, Color.WHITE);
        BaseActivity.SetIconTint(this.stopPlayButton, Color.WHITE);

        this.undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // just undo the last point
                undoLastPoint();
            }
        });
        this.stopPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // end / play the match
                stopPlayMatch();
                setupStopPlayButton();
            }
        });

        // initialise the stop / play button
        setupStopPlayButton();
    }

    @Override
    protected void onDestroy() {
        // remove us as a listener
        this.activeMatch.removeListener(this);
        // and kill the base
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop speaking
        this.speakService.close();
        this.speakService = null;

        if (null != this.playStarted) {
            // and add the time played in this session to the active match
            int activityMinutes = getMinutesPlayedInActivity();
            if (activityMinutes > 0) {
                this.activeMatch.addMatchMinutesPlayed(activityMinutes);
            }
            // now we added these minutes, we need to not add them again, reset the
            // play started time to be now
            this.playStarted = Calendar.getInstance().getTime();
        }

        // store these results for sure
        storeMatchResults(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.activeMatch.getMatchPlayedDate() == null) {
            // start the new match by setting the start date
            this.activeMatch.setMatchPlayedDate(new Date());
        }
        // create the thing for speaking
        this.speakService = new SpeakService(this);

    }

    @Override
    public void onAttachFragment(FragmentSounds fragment) {
        this.soundsFragment = fragment;
        this.soundsFragment.setVisibility(View.GONE);
    }

    private void undoLastPoint() {
        this.activeMatch.undoLastPoint();
    }

    private void stopPlayMatch() {
        // if we are playing then end the match, else start the match
        if (null == this.playStarted) {
            // we are not playing, start playing the match
            startPlay();
        }
        else {
            // we are started, end the match now by going
            // back to the main activity and clear the activity history so back
            // doesn't come back here
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // start this main activity now then
            startActivity(intent);
        }
    }

    private void startPlay() {
        this.playStarted = Calendar.getInstance().getTime();
        onPlayStarted(this.playStarted);
        // show the sound controls as we start playing
        showSoundControls(true);
    }

    protected void clearStartedPlay() {
        this.playStarted = null;
        onPlayStarted(null);
        // hide the sound controls as we cease playing
        showSoundControls(false);
    }

    protected void clearEndedPlay() {
        this.playEnded = null;
    }

    protected abstract void onPlayStarted(Date playStarted);

    protected boolean isPlayStarted() {
        return null != this.playStarted;
    }

    protected boolean isPlayEnded() {
        return null != this.playEnded;
    }

    protected void setPlayEnded(Date ended) {
        this.playEnded = ended;
    }

    protected void setSpokenMessage(String message) {
        this.spokenMessage = message;
        this.isMessageStarted = true;
    }

    protected void clearSpokenMessage() {
        this.spokenMessage = null;
    }

    protected boolean isMessageStarted() {
        return this.isMessageStarted;
    }

    private void showSoundControls(boolean isShowControls) {
        if (isShowControls) {
            // show the fragment
            this.soundsFragment.setVisibility(View.VISIBLE);
            // without the butons showing
            this.soundsFragment.hideButtons(true);
        }
        else {
            // hide the fragment
            this.soundsFragment.setVisibility(View.GONE);
        }
    }

    protected void setupStopPlayButton() {
        // if we are started then the button is the stop button
        if (this.activeMatch.isReadOnly() || null != this.playStarted) {
            // we are started
            if (null == this.playStarted) {
                // start tracking that we are playing
                stopPlayMatch();
            }
            this.stopPlayButton.setText(R.string.btn_endMatch);
            this.stopPlayButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_stop_24px, 0, 0, 0);
            // ensure this is tinted properly
            BaseActivity.SetIconTint(this.stopPlayButton, Color.RED);
        }
        else {
            // we are not started, show the start button
            this.stopPlayButton.setText(R.string.btn_startMatch);
            this.stopPlayButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_play, 0, 0, 0);
            // ensure this is tinted properly
            BaseActivity.SetIconTint(this.stopPlayButton, Color.WHITE);
        }
    }

    protected int getMinutesPlayedInActivity() {
        if (null == this.playStarted) {
            return 0;
        }
        else {
            long playEndedMs;
            if (null == this.playEnded) {
                // play isn't over yet, use now
                playEndedMs = Calendar.getInstance().getTimeInMillis();
            } else {
                // use the play ended time
                playEndedMs = this.playEnded.getTime();
            }
            // Calculate difference in milliseconds
            long diff = playEndedMs - this.playStarted.getTime();
            // and add the time played to the active match
            return (int) (diff / 60000L);
        }
    }

    protected void storeMatchResults(boolean storeIfPersisted) {
        // store the results of the match
        MatchPersistanceManager persistanceManager = new MatchPersistanceManager(this);
        if (storeIfPersisted
                || false == persistanceManager.isMatchDataPersisted(this.activeMatch)) {
            // we are forcing a save, or the data is different, so save
            persistanceManager.saveMatchToFile(this.activeMatch, this.activeMatch.getMatchId(this));
        }
    }

    @Override
    public void onMatchPointsChanged(Match.PointChange[] levelsChanged) {
        // need to announce this change in the score, find the top level
        // that changed, if we won a set we don't care about the game...
        Match.PointChange topChange = null;
        for (Match.PointChange change : levelsChanged) {
            // we want to find the highest change
            if (topChange == null || change.level > topChange.level) {
                // this is the biggest - remember this
                topChange = change;
            }
        }
        if (null != topChange) {
            String message = "";
            if (this.application.getSettings().getIsSpeakingPoints()) {
                // we want to say the points, create the phrase to say
                message = createPointsPhrase(topChange);
            }
            if (null != this.spokenMessage && this.application.getSettings().getIsSpeakingMessages()) {
                // we might also be changing ends
                // or something, get from the score fragment the state it is showing
                if (null != this.spokenMessage
                        && false == this.spokenMessage.isEmpty()
                        && false == ((TennisScore)activeMatch.getScore()).isMatchOver()) {
                    // there is a state showing and the match isn't over, speak it here
                    if (false == message.isEmpty()) {
                        // we are speaking, say this after the score is announced
                        message += ". ";
                    }
                    // add the message to what is said
                    message += this.spokenMessage;
                }
            }
            // clear any message to speak, will speak it or will be silent
            clearSpokenMessage();
            if (false == message.isEmpty()) {
                // speak what we have made, overriding everything before it
                this.speakService.speakMessage(message, true);
            }
        }
    }

    private String createPointsPhrase(Match.PointChange change) {
        TennisScore score = (TennisScore) this.activeMatch.getScore();
        Team teamOne = this.activeMatch.getTeamOne();
        Team teamTwo = this.activeMatch.getTeamTwo();
        String message = "";
        switch (change.level) {
            case TennisScore.LEVEL_POINT:
                // the points changed, announce the points
                Point t1Point = score.getDisplayPoint(teamOne);
                Point t2Point = score.getDisplayPoint(teamTwo);
                if (t1Point == TennisScore.TennisPoint.ADVANTAGE) {
                    // read advantage team one
                    message = t1Point.speakString(this)
                            + " "
                            + teamOne.getTeamName();
                } else if (t2Point == TennisScore.TennisPoint.ADVANTAGE) {
                    // read advantage team two
                    message = t2Point.speakString(this)
                            + " "
                            + teamTwo.getTeamName();
                } else if (t1Point == TennisScore.TennisPoint.DEUCE
                        && t2Point == TennisScore.TennisPoint.DEUCE) {
                    // read deuce
                    message = t1Point.speakString(this);
                } else if (t1Point.val() == t2Point.val()) {
                    // they have the same score, use the special "all" values
                    message = t1Point.speakAllString(this);
                } else if (score.isInTieBreak()) {
                    // in a tie-break we read the score with the winner first
                    if (t1Point.val() > t2Point.val()) {
                        // player one has more
                        message = t1Point.speakString(this)
                                + " "
                                + t2Point.speakString(this)
                                + " "
                                + teamOne.getTeamName();
                    } else {
                        // player two has more
                        message = t2Point.speakString(this)
                                + " "
                                + t1Point.speakString(this)
                                + " "
                                + teamTwo.getTeamName();
                    }
                } else {
                    // just read the numbers out, but we want to say the server first
                    // so who is that?
                    if (teamOne.isPlayerInTeam(this.activeMatch.getCurrentServer())) {
                        // team one is serving
                        message = t1Point.speakString(this)
                                + " "
                                + t2Point.speakString(this);
                    } else {
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
                        + ". "
                        + change.team.getTeamName();
                break;
            case TennisScore.LEVEL_SET:
                // the sets changed, announce who won the game and the set
                message = TennisScore.TennisPoint.GAME.speakString(this)
                        + ". "
                        + TennisScore.TennisPoint.SET.speakString(this)
                        + ". ";
                // also match?
                if (score.isMatchOver()) {
                    message += TennisScore.TennisPoint.MATCH.speakString(this)
                            + ". ";
                }
                // add the winner's name
                message += change.team.getTeamName();

                if (score.isMatchOver()) {
                    // we want to also read out the games from each set
                    message += "...";

                    Team winnerTeam;
                    Team loserTeam;
                    if (change.team == teamOne) {
                        // team one is the winner
                        winnerTeam = teamOne;
                        loserTeam = teamTwo;
                    }
                    else {
                        // team two is the winner
                        winnerTeam = teamTwo;
                        loserTeam = teamOne;
                    }
                    for (int i = 0; i < score.getPlayedSets(); ++i) {
                        message += score.getGames(winnerTeam, i)
                                + ", "
                                + score.getGames(loserTeam, i)
                                + ", ";
                    }
                }

                break;
        }
        // there might be dots in the string (initials) which cause
        // the speaking to pause too much, remove them here
        return message.replaceAll("[.]", "");
    }

    @Override
    public void onMatchChanged(Match.MatchChange type) {
        // something changed, reset the message started flag that we were saying before
        this.isMessageStarted = false;
    }
}

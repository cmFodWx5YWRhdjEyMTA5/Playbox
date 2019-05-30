package uk.co.darkerwaters.staveinvaders.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import uk.co.darkerwaters.staveinvaders.Application;
import uk.co.darkerwaters.staveinvaders.R;
import uk.co.darkerwaters.staveinvaders.application.Log;
import uk.co.darkerwaters.staveinvaders.application.Settings;
import uk.co.darkerwaters.staveinvaders.games.Game;
import uk.co.darkerwaters.staveinvaders.games.GameList;
import uk.co.darkerwaters.staveinvaders.games.GamePlayer;
import uk.co.darkerwaters.staveinvaders.games.GameProgress;
import uk.co.darkerwaters.staveinvaders.games.GameScore;
import uk.co.darkerwaters.staveinvaders.notes.Chord;
import uk.co.darkerwaters.staveinvaders.notes.ChordFactory;
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.sounds.SoundPlayer;
import uk.co.darkerwaters.staveinvaders.views.CircleProgressView;
import uk.co.darkerwaters.staveinvaders.views.MusicViewPlaying;
import uk.co.darkerwaters.staveinvaders.views.PianoTouchable;
import uk.co.darkerwaters.staveinvaders.views.SlideInOutAnimator;

import static uk.co.darkerwaters.staveinvaders.activities.fragments.GameParentCardHolder.K_IS_STARTING_HELP_ON;
import static uk.co.darkerwaters.staveinvaders.activities.fragments.GameParentCardHolder.K_SELECTED_CARD_FULL_NAME;
import static uk.co.darkerwaters.staveinvaders.activities.fragments.GameParentCardHolder.K_STARTING_TEMPO;

public class GamePlayActivity extends HidingFullscreenActivity implements GamePlayer.GamePlayerListener, GameProgress.GameProgressListener {

    private static final long K_PLAY_COUNTDOWN = 5000L;
    private Application application;

    private int startingTempo = 60;
    private boolean startingWithHelpOn = true;

    private Game selectedGame;

    private CircleProgressView progressView;
    private MusicViewPlaying musicView;
    private PianoTouchable pianoView;

    private CircleProgressView tempoProgressView;
    private RatingBar livesRatingBar;
    private RatingBar shotsRatingBar;

    private volatile boolean isPerformCountdown = true;
    private GamePlayer gamePlayer;

    private SlideInOutAnimator levelUpLayout;
    private TextView levelUpHead;
    private ImageView levelUpImage;
    private TextView levelUpTail;

    private FloatingActionButton playButton;
    private FloatingActionButton stopButton;
    private FloatingActionButton muteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

        createFullscreenView(findViewById(R.id.fullscreen_layout));

        this.application = (Application) this.getApplication();
        Settings settings = this.application.getSettings();

        this.progressView = findViewById(R.id.circleProgressView);
        this.musicView = findViewById(R.id.musicView);
        this.pianoView = findViewById(R.id.pianoView);

        this.tempoProgressView = findViewById(R.id.tempoProgressDisplay);
        this.livesRatingBar = findViewById(R.id.livesRatingBar);
        this.shotsRatingBar = findViewById(R.id.bulletsRatingBar);

        this.levelUpHead = findViewById(R.id.levelUpHeadText);
        this.levelUpTail = findViewById(R.id.levelUpTailText);
        this.levelUpImage = findViewById(R.id.levelUpImage);
        this.levelUpLayout = new SlideInOutAnimator(findViewById(R.id.levelUpLayout));

        this.playButton = findViewById(R.id.playActionButton);
        this.stopButton = findViewById(R.id.stopActionButton);
        this.muteButton = findViewById(R.id.muteActionButton);

        Intent intent = getIntent();
        String parentGameName = intent.getStringExtra(K_SELECTED_CARD_FULL_NAME);
        this.startingTempo = intent.getIntExtra(K_STARTING_TEMPO, GameScore.K_DEFAULT_BPM);
        this.startingWithHelpOn = intent.getBooleanExtra(K_IS_STARTING_HELP_ON, true);
        this.selectedGame = GameList.findLoadedGame(parentGameName);
        if (null == this.selectedGame) {
            Log.error("Game name of " + parentGameName + " does not correspond to a game");
        }
        else {
            // set our title to be the name of the game parent
            setTitle(selectedGame.name);
        }

        this.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPauseGame();
            }
        });
        this.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endGame();
            }
        });
        this.muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMute();
            }
        });

        // setup the progress
        this.livesRatingBar.setNumStars(GameProgress.K_LIVES);
        this.livesRatingBar.setRating(GameProgress.K_LIVES);
        this.shotsRatingBar.setNumStars(GameProgress.K_SHOTS);
        this.shotsRatingBar.setRating(GameProgress.K_SHOTS);
        this.tempoProgressView.setProgress(0f, "0%");

        // setup the music view
        musicView.setPermittedClefs(settings.getSelectedClefs());
        // create the game player and setup the music view accordingly
        this.gamePlayer = this.musicView.setActiveGame(this.selectedGame);
        // add the listeners to the player
        this.gamePlayer.addListener((GamePlayer.GamePlayerListener)this);
        this.gamePlayer.addListener((GameProgress.GameProgressListener) this);

        // and the piano
        this.pianoView.setNoteRange(this.selectedGame.getNoteRange(settings.getSelectedClefs()));
        this.pianoView.setIsAllowTouch(true);

        // and the music view
        this.musicView.setTempo(this.startingTempo);
        this.musicView.setIsHelpLettersShowing(this.startingWithHelpOn);

        // be sure sound is initialised
        SoundPlayer.initialise(this, this.application);
    }

    @Override
    protected void onDestroy() {
        // remove us as listeners
        this.gamePlayer.removeListener((GamePlayer.GamePlayerListener)this);
        this.gamePlayer.removeListener((GameProgress.GameProgressListener) this);
        // clear out the sound player
        SoundPlayer.close();
        // and destroy
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (false == this.gamePlayer.isGameActive()) {
            // this game is over (we are coming back from the score card, just finish this
            // to jump back another one
            finish();
        }
        // start our countdown
        this.isPerformCountdown = true;
        // be sure the game play is paused
        this.gamePlayer.setPaused(true);
        // and show the progress
        this.progressView.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // perform the countdown
                countDownToStartOfPlay();
            }
        }).start();
        // show / hide the button
        updatePlayPauseButton();
    }


    @Override
    protected void onPause() {
        // be sure the gameplay is paused
        this.gamePlayer.setPaused(true);
        // and stop any countdown we are performing
        this.isPerformCountdown = false;
        synchronized (this) {
            this.notifyAll();
        }
        // clear the off key chord creation
        ChordFactory.ClearOffkeyCreation();
        super.onPause();
    }

    private void playPauseGame() {
        // play / pause the game
        this.gamePlayer.setPaused(!this.gamePlayer.isPaused());
        // update the icon and shown status
        updatePlayPauseButton();
    }

    private void updatePlayPauseButton() {
        // hide the button when counting down
        if (this.isPerformCountdown) {
            this.playButton.hide();
            this.stopButton.hide();
            this.muteButton.hide();
        }
        else {
            this.playButton.show();
            this.stopButton.show();
            this.muteButton.show();
            if (this.gamePlayer.isPaused()) {
                // show the play icon
                this.muteButton.animate().translationY(-getResources().getDimension(R.dimen.standard_63));
                this.stopButton.animate().translationX(-getResources().getDimension(R.dimen.standard_63));
                this.playButton.setImageResource(android.R.drawable.ic_media_play);
            }
            else {
                // show the pause icon
                this.muteButton.animate().translationY(0);
                this.stopButton.animate().translationX(0);
                this.playButton.setImageResource(android.R.drawable.ic_media_pause);
            }

            // set the correct icon
            if (false == this.gamePlayer.isPaused()) {
                this.playButton.setImageResource(android.R.drawable.ic_media_pause);
            }
            else {
                this.playButton.setImageResource(android.R.drawable.ic_media_play);
            }
            if (this.application.getSettings().getIsMuted()) {
                this.muteButton.setImageResource(R.drawable.ic_baseline_volume_up_24px);
            }
            else {
                this.muteButton.setImageResource(R.drawable.ic_baseline_volume_off_24px);
            }
        }
    }

    private void countDownToStartOfPlay() {
        // countdown to play time
        long playTime = System.currentTimeMillis() + K_PLAY_COUNTDOWN;
        while (this.isPerformCountdown) {
            // while we are counting down to play, play
            final float secondsTillPlay =  (playTime - System.currentTimeMillis()) / 1000f;
            if (secondsTillPlay <= 0f) {
                // start the game
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startPlaying();
                    }
                });
                // and quit out of this thread
                break;
            }
            else {
                // show the seconds left on the display
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        float progress = secondsTillPlay / (K_PLAY_COUNTDOWN / 1000f);
                        progressView.setProgress(progress, Integer.toString((int)(secondsTillPlay + 1)));
                        progressView.invalidate();
                    }
                });
            }
            synchronized (this) {
                try {
                    this.wait(100);
                } catch (InterruptedException e) {
                    // whatever
                }
            }
        }
        // no longer counting down
        this.isPerformCountdown = false;
    }

    private void startPlaying() {
        // no longer counting down
        this.isPerformCountdown = false;
        // lose the progress countdown timer
        this.progressView.setVisibility(View.GONE);
        if (this.gamePlayer.isPaused()) {
            // play the game
            playPauseGame();
        }
        else {
            // show / hide the button
            updatePlayPauseButton();
        }

        // setup the input here
        if (Settings.InputType.keys == this.application.getInputSelector().getActiveInputType() &&
                false == this.application.getSettings().getIsKeyInputPiano()) {
            // this is the keyboard and it is in letter mode, need to ignore where on the keyboard
            // the notes are and just test their letters and sharp / flat status
            ChordFactory.InitialiseOffkeyCreation(this.application.getSingleChords());
        }
        else {
            // use the exact chords to compare each time
            ChordFactory.ClearOffkeyCreation();
        }

        // start the game
        if (null != this.gamePlayer) {
            // turn help off, this sets us playing the game
            this.gamePlayer.startNewGame(this.startingTempo, this.startingWithHelpOn);
        }

        // show the level up
        this.levelUpLayout.slideIn();
    }

    private void endGame() {
        // end this game
        this.gamePlayer.endGame();
        // show the activity to summarise the score
        Intent intent = new Intent(GamePlayActivity.this, GameOverActivity.class);
        intent.putExtra(K_SELECTED_CARD_FULL_NAME, selectedGame.getFullName());
        GamePlayActivity.this.startActivity(intent);
    }

    private void toggleMute() {
        Settings settings = this.application.getSettings();
        settings.setIsMuted(!settings.getIsMuted());
        updatePlayPauseButton();
    }

    @Override
    public void onGameClefChanged(Clef clef) {
        // set the range on the piano view accordingly
        this.pianoView.setNoteRange(this.selectedGame.getNoteRange(new Clef[] {clef}));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pianoView.invalidate();
            }
        });
    }

    @Override
    public void onGameProgressChanged(final GameProgress source, GameProgress.Type type, Object data) {
        this.levelUpImage.setVisibility(View.VISIBLE);
        switch (type) {
            case tempoIncrease:
                // show this increase
                this.levelUpHead.setText(R.string.tempo);
                this.levelUpTail.setText(R.string.level_up);
                this.levelUpLayout.slideIn();
                break;
            case lettersDisabled:
                // show that the letters are gone
                this.levelUpHead.setText(R.string.letters);
                this.levelUpTail.setText(R.string.disabled);
                this.levelUpLayout.slideIn();
                break;
            case gameStarted:
                // start the game
                this.levelUpHead.setText(R.string.game);
                this.levelUpTail.setText(R.string.start);
                this.levelUpLayout.slideIn();
                break;
            case gameOver:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        levelUpHead.setText(R.string.game);
                        levelUpTail.setText(R.string.ended);
                        levelUpImage.setVisibility(View.GONE);
                        if (!application.getSettings().getIsMuted()) {
                            SoundPlayer.getINSTANCE().gameOver();
                        }
                        levelUpLayout.slideIn(new Runnable() {
                            @Override
                            public void run() {
                                // the slide of "game over" has ended, show the game over activity
                                endGame();
                            }
                        });
                    }
                });
                break;
            case lifeLost:
                // play the sound for this
                if (!application.getSettings().getIsMuted()) {
                    SoundPlayer.getINSTANCE().missed();
                }
                break;
            case shotLost:
                // play the sound for this
                if (!application.getSettings().getIsMuted()) {
                    SoundPlayer.getINSTANCE().falseFire();
                }
                break;
            case targetHit:
                // the data param is the chord that has been hit
                if (data instanceof Chord) {
                    if (!application.getSettings().getIsMuted()) {
                        SoundPlayer.getINSTANCE().playSound((Chord) data);
                    }
                }
                break;
        }
        // and update the contents of the controls from the source
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateGameProgress(source);
            }
        });
    }

    private void updateGameProgress(GameProgress source) {
        this.livesRatingBar.setRating(source.getLivesLeft());
        this.shotsRatingBar.setRating(source.getShotsLeft());
        // and the progress of this tempo
        float progress = source.getPoints() / (float)this.gamePlayer.getPointLevelGoal();
        this.tempoProgressView.setProgress(progress, (int) (progress * 100f) + "%");
    }
}

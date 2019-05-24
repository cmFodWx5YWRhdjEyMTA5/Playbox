package uk.co.darkerwaters.staveinvaders.actvities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import uk.co.darkerwaters.staveinvaders.notes.Clef;
import uk.co.darkerwaters.staveinvaders.sounds.SoundPlayer;
import uk.co.darkerwaters.staveinvaders.views.CircleProgressView;
import uk.co.darkerwaters.staveinvaders.views.MusicViewPlaying;
import uk.co.darkerwaters.staveinvaders.views.PianoTouchable;
import uk.co.darkerwaters.staveinvaders.views.SlideInOutAnimator;

import static uk.co.darkerwaters.staveinvaders.actvities.fragments.GameParentCardHolder.K_IS_STARTING_HELP_ON;
import static uk.co.darkerwaters.staveinvaders.actvities.fragments.GameParentCardHolder.K_SELECTED_CARD_FULL_NAME;
import static uk.co.darkerwaters.staveinvaders.actvities.fragments.GameParentCardHolder.K_STARTING_TEMPO;

public class GamePlayActivity extends AppCompatActivity implements GamePlayer.GamePlayerListener, GameProgress.GameProgressListener {

    private static final long K_PLAY_COUNTDOWN = 5000l;
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
    private TextView levelUpTail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play);

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
        this.levelUpLayout = new SlideInOutAnimator(findViewById(R.id.levelUpLayout));

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
        this.pianoView.setNoteRange(this.selectedGame.getNoteRange(settings.getSelectedClefs()), true);
        this.pianoView.setIsAllowTouch(true);

        SoundPlayer.initialise(this);
    }

    @Override
    protected void onDestroy() {
        // remove us as listeners
        this.gamePlayer.removeListener((GamePlayer.GamePlayerListener)this);
        this.gamePlayer.removeListener((GameProgress.GameProgressListener) this);
        // and destroy
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // start our countdown
        this.isPerformCountdown = true;
        this.progressView.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // perform the countdown
                countDownToStartOfPlay();
            }
        }).start();
    }

    @Override
    protected void onPause() {
        this.isPerformCountdown = false;
        synchronized (this) {
            this.notifyAll();
        }
        super.onPause();
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
    }

    private void startPlaying() {
        // lose the progress countdown timer
        this.progressView.setVisibility(View.GONE);

        // start the game
        if (null != this.gamePlayer) {
            // turn help off, this sets us playing the game
            this.gamePlayer.startNewGame(this.startingTempo, this.startingWithHelpOn);
        }
        // show the level up
        this.levelUpLayout.slideIn();
    }

    @Override
    public void onGameClefChanged(Clef clef) {
        // set the range on the piano view accordingly
        this.pianoView.setNoteRange(this.selectedGame.getNoteRange(new Clef[] {clef}), true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pianoView.invalidate();
            }
        });
    }

    @Override
    public void onGameProgressChanged(final GameProgress source, GameProgress.Type type, Object data) {
        switch (type) {
            case tempoIncrease:
                // show this increase
                levelUpHead.setText(R.string.tempo);
                levelUpTail.setText(R.string.level_up);
                this.levelUpLayout.slideIn();
                break;
            case lettersDisabled:
                // show that the letters are gone
                levelUpHead.setText(R.string.letters);
                levelUpTail.setText(R.string.disabled);
                this.levelUpLayout.slideIn();
                break;
            case gameStarted:
                // start the game
                levelUpHead.setText(R.string.game);
                levelUpTail.setText(R.string.start);
                this.levelUpLayout.slideIn();
                break;
            case gameOver:
                // TODO handle the game ending
                SoundPlayer.getINSTANCE().gameOver();
                break;
            case lifeLost:
                // play the sound for this
                SoundPlayer.getINSTANCE().missed();
                break;
            case shotLost:
                // play the sound for this
                SoundPlayer.getINSTANCE().falseFire();
                break;
            case targetHit:
                // the data param is the chord that has been hit
                if (null != data && data instanceof Chord) {
                    SoundPlayer.getINSTANCE().playSound((Chord)data);
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
        float progress = source.getPoints() / (float)GameProgress.K_LEVEL_POINTS_GOAL;
        this.tempoProgressView.setProgress(progress, Integer.toString((int)(progress * 100f)) + "%");
    }
}
